# tool-bluetooth-printer-kotlin

Módulo **Android em Kotlin** para comunicação com **impressoras térmicas** (Bluetooth ou integradas em maquininhas POS). Atua como ponte entre a API do SaaS de vendas e o hardware: consulta fila de impressão, formata cupons em 58 mm e confirma o status na API.

---

## O que este projeto faz

- **Foreground Service** em Android que faz **polling** na API do SaaS (ex.: a cada 5 s).  
- Busca o **próximo pedido** a imprimir (`GET /api/orders/next-to-print`).  
- Formata o pedido em **layout de cupom 58 mm** (ReceiptFormatter).  
- Envia para a impressora (via SDK da Stone ou **Mock** para testes).  
- Confirma na API com `PATCH /api/orders/:id/status` → `printed`.

Ou seja: **hardware (impressora) ↔ este app ↔ API do SaaS**.

---

## Desafio técnico: integração de hardware

Integrar impressoras térmicas envolve:

- **Protocolos:** Bluetooth (SPP), USB ou SDK proprietário (ex.: Stone).  
- **Layout:** largura fixa (ex.: 32 caracteres em 58 mm), cortes, enfatizado, código de barras.  
- **Conectividade:** falhas de rede, timeouts, fila local se a API estiver indisponível.  
- **Robustez:** serviço em foreground, reconexão, verificação de rede antes de cada poll.

Este módulo centraliza essa lógica (formatação + chamadas à API) e deixa a camada de impressão plugável (Mock hoje; Stone ou Bluetooth direto depois).

---

## Ponte para o ecossistema SaaS

```
[Bot WhatsApp] → [API SaaS] ←→ [App Expo]
                        ↑
                        │ polling next-to-print
                        │ PATCH status
                        ▼
              [tool-bluetooth-printer-kotlin]
                        │
                        ▼
              [Impressora térmica / Stone]
```

- A **fila** é mantida na API (pedidos `pending` ordenados por `created_at`).  
- Este app **consome** a fila via polling, imprime e **atualiza** o status.  
- O **app Expo** e o **dashboard** refletem o mesmo estado; não há fila duplicada no Android.

---

## Stack

- **Kotlin**  
- **Retrofit** + **OkHttp** (API, `X-API-Key` em interceptor)  
- **Kotlin Coroutines** (polling, I/O)  
- **Foreground Service** (Android 8+)  
- **Gson** com adapters para `total_price` (string/number)

---

## Estrutura principal

```
android/app/src/main/java/.../printer/
  MainActivity.kt           # Inicia serviço, botão Admin
  service/
    OrderPrinterService.kt  # Polling, impressão, confirmação na API
  data/
    api/
      ApiClient.kt          # Retrofit + API Key
      ApiService.kt         # Endpoints (next-to-print, status, etc.)
    model/
      Order.kt, AdminModels.kt, ...
  util/
    ReceiptFormatter.kt     # Formatação cupom 58 mm
    ServiceManager.kt       # Start/stop do service
  stone/
    MockPrinter.kt          # Simulação sem SDK Stone
  ui/
    AdminActivity.kt        # Tela admin (opcional)
```

---

## Configuração

- **Base URL** e **API Key** em `ApiClient.kt` (ou via BuildConfig/env em produção).  
- Mesma **API Key** usada pela API e pelo app Expo.

---

## Modo teste (Mock) vs Stone

- **Mock:** `MockPrinter` simula impressão (logs apenas). Útil para desenvolver sem hardware/Stone.  
- **Stone:** substituir por `PosPrintProvider` (SDK Stone) quando houver token e dispositivo. O `OrderPrinterService` já contém comentários com o fluxo sugerido.

---

## Permissões (AndroidManifest)

- `INTERNET`, `ACCESS_NETWORK_STATE`  
- `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS`, `WAKE_LOCK`

---

## Como rodar

1. Abrir `android/` no Android Studio.  
2. Configurar `BASE_URL` e `API_KEY` em `ApiClient.kt`.  
3. Build & run em dispositivo ou emulador.  
4. O **OrderPrinterService** inicia com a `MainActivity` e faz polling automático.

---

## Licença

Projeto privado. Uso interno ou conforme acordos do produto.
