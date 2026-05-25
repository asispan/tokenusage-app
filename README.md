<<<<<<< HEAD
# tokenusage-app
A singular dashboard in a mobile app for all your LLM and tools token consumption live
=======
# LLM Usage Monitor

An Android-native app that tracks LLM token usage, estimated spend, quotas, and
reset windows across OpenAI, Anthropic, OpenRouter, Gemini, plus any tool you
log manually. Everything stays on device.

## At a glance

- Kotlin · Jetpack Compose · Material 3
- MVVM with hand-rolled DI (no Hilt) — keeps build fast and deps small
- Room for usage history · DataStore for preferences
- Android Keystore (AES/GCM) for API keys — keys never leave the device
- WorkManager for periodic background refresh
- Glance home-screen widget
- Compose-native bar/line charts (no extra chart library)
- Bottom-nav: Dashboard · History · Providers · Alerts · Settings
- Dark-mode-first; supports system/light/dark
- Android 10 (API 29) and up

## Project layout

```
LLMUsageMonitor/
├── build.gradle.kts                    # Root build (plugins via version catalog)
├── settings.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml           # Single source of truth for versions
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                        # Strings, themes, colors, widget XML, icons
        └── java/com/llmusage/monitor/
            ├── LLMUsageApp.kt          # Application + DI container init
            ├── AppContainer.kt         # Hand-rolled dependency container
            ├── MainActivity.kt
            ├── data/
            │   ├── db/                 # Room: entities, DAOs, AppDatabase
            │   ├── prefs/              # DataStore-backed SettingsDataStore
            │   ├── remote/             # Shared OkHttp/Retrofit factory
            │   ├── repository/         # ProviderRepository, UsageRepository, etc.
            │   └── security/           # KeystoreEncryption (AES/GCM helper)
            ├── domain/model/           # ProviderType, UsageData, SyncStatus, …
            ├── providers/              # Provider abstraction
            │   ├── Provider.kt
            │   ├── ProviderResult.kt
            │   ├── ProviderRegistry.kt
            │   ├── api/                # Retrofit interfaces per provider
            │   └── impl/               # Concrete OpenAI/Anthropic/… providers
            ├── ui/
            │   ├── theme/              # Material 3 theme (dark-first)
            │   ├── components/         # QuotaBar, StatCard, ProviderCard, charts
            │   ├── navigation/         # Top-level destinations + routes
            │   ├── AppRoot.kt          # Scaffold + NavHost
            │   ├── dashboard/          # DashboardScreen + ViewModel
            │   ├── history/            # HistoryScreen + charts
            │   ├── providers/          # List + per-provider detail/key form
            │   ├── alerts/             # Threshold alerts CRUD
            │   ├── settings/           # Currency, refresh, theme, reset, CSV export
            │   └── onboarding/         # First-run welcome
            ├── worker/                 # UsageRefreshWorker + UsageSyncManager
            ├── notification/           # NotificationHelper
            ├── widget/                 # Glance home-screen widget
            └── util/                   # TimeUtils, CurrencyFormatter, SimpleFactory
```

## Setup

1. Install Android Studio Koala (2024.1.1) or newer.
2. Open the `LLMUsageMonitor/` directory as an existing Gradle project.
3. Let Gradle sync (first run pulls AGP 8.5, Kotlin 2.0, Compose BOM 2024.09).
4. Run on a physical or emulated device running Android 10+ (API 29).
5. On first launch you'll see the welcome screen, then the empty Dashboard.
   Open **Providers**, tap a provider, paste an API key, and hit **Save key**.

### Permissions requested

- `INTERNET`, `ACCESS_NETWORK_STATE` — talking to provider APIs
- `POST_NOTIFICATIONS` — threshold alerts (asked at runtime on Android 13+)
- `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`
  — keeps the periodic WorkManager refresh resilient across reboots

## Architecture

### MVVM + unidirectional state

Each screen has a `*ViewModel` exposing a single `StateFlow<*UiState>`. The
ViewModel reads from repositories (which read from Room / DataStore / network)
and writes user actions back via suspend functions. Composables only render
state and dispatch intents.

### Dependency container

`AppContainer` is a `lazy`-initialised singleton owned by `LLMUsageApp`. It
wires DAOs → repositories → providers → sync manager. No annotation-processing,
no codegen — easy to read and step through.

### Provider abstraction

```kotlin
interface Provider {
    val type: ProviderType
    val authMethod: AuthMethod
    suspend fun fetchUsage(apiKey: String?, extrasJson: String?): ProviderResult<UsageData>
    suspend fun testConnection(apiKey: String?, extrasJson: String?): ProviderResult<Unit>
}
```

`ProviderResult` has three states: `Success`, `Failure` (transient/auth error),
and `Unsupported` (provider doesn't publish the data; fall back to manual
entry). Implementations live in `providers/impl/` and are registered in
`ProviderRegistry`. Adding a new provider is one new file + one line in the
registry.

### Pricing

A local pricing registry (`PricingRepository`) ships with indicative
per-1M-token rates for the bundled providers. **All costs computed from it are
estimates** — the UI labels them as such, and users can override any row.
Provider responses that already include cost in USD (OpenRouter `auth/key`,
Anthropic `cost_report` when available) are used directly without applying
the pricing registry.

### Encryption

`KeystoreEncryption` uses `AndroidKeyStore` AES-256/GCM/NoPadding. The key is
hardware-backed on devices that support Strongbox. Plaintext API keys exist
only in memory while a request is being assembled; encrypted ciphertext + IV
are stored in Room as Base64 strings.

The app also opts out of cloud backup and device-to-device transfer for
sharedprefs, files, and database (`res/xml/data_extraction_rules.xml`).

### Background refresh

`UsageRefreshWorker` is a `CoroutineWorker` scheduled via WorkManager unique
periodic work (`enqueueUniquePeriodicWork` with `UPDATE` policy). The user
can change the interval in Settings (minimum 15 min — WorkManager's floor).
The worker invokes `UsageSyncManager.syncAllOnce()`, which:

1. Fetches enabled providers' usage
2. Upserts today's `UsageSnapshot`
3. Fires any unfired threshold notifications (one per provider/threshold/day)
4. Asks the Glance widget to re-render

### Widget

The home-screen widget is built with **Jetpack Glance** (Compose for widgets).
It reads from the same `UsageRepository` flow as the dashboard, so it can't
drift out of sync. Refresh happens on every WorkManager run.

## Provider notes & limitations

> The Truth Protocol: provider APIs change frequently and most consumer
> accounts don't get access to admin/usage endpoints. Read this section before
> shipping.

| Provider     | What we fetch                                          | Caveats                                                                                                     |
| ------------ | ------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------- |
| **OpenAI**   | `GET /v1/usage?date=YYYY-MM-DD` + `GET /v1/dashboard/billing/subscription` | The `dashboard/billing/*` endpoints historically required a session cookie rather than an API key; treated as best-effort. If they 401 we still show usage without a hard-cap. |
| **Anthropic**| `GET /v1/organizations/me/cost_report` first; falls back to a 1-token `/v1/messages` ping | Cost report endpoints are admin-key-only and not available on all accounts. When unavailable, the provider returns `Unsupported` and the UI prompts for manual entry. |
| **OpenRouter** | `GET /api/v1/auth/key`                              | Returns usage in USD plus the configured limit — the cleanest consumer endpoint of the four.                |
| **Gemini**   | `GET /v1beta/models` (credential check only)            | Google does not publish a per-API-key usage endpoint. Real usage lives in Google Cloud Billing reports. The provider returns `Unsupported`; log manually. |
| **Manual**   | n/a                                                    | Always Unsupported — by design. Use the "Log usage" form on the provider detail screen.                     |

If a provider returns `Unsupported`, the UI surfaces the hint and switches to
the manual-entry form. The app never crashes on an unknown response shape;
all parsers are lenient (`ignoreUnknownKeys = true`).

## What's deliberately *not* in v1

- **No backend.** All network calls go provider → device directly. There is
  no Anthropic/OpenAI/etc. credential ever leaving the user's phone.
- **No FX conversion.** Settings let the user change the displayed currency
  symbol but the underlying values stay in USD — we'd rather show an honest
  number than a guessed conversion.
- **No multi-account support per provider** (e.g. multiple OpenAI orgs).
  The architecture supports it — `extrasJson` is wide open — but the UI only
  exposes a single key + optional org-id today.
- **No tests.** A test module skeleton would be the obvious first follow-up:
  Room in-memory DB + fake `Provider` implementations for the sync flow.
- **No analytics, no crash reporting, no telemetry.** We don't ship those
  with v1; users who want them can wire Firebase Crashlytics themselves.

## Next steps

1. **Tests**: instrumented Room test + unit tests for `UsageSyncManager`
   (alert dedupe, snapshot upsert).
2. **Pricing editor UI** — backend already exists in `PricingRepository`; just
   needs a settings sub-screen.
3. **Per-model breakdown** on the History screen (data is already keyed by
   model in `UsageData.perModel`).
4. **Real device-test the OpenAI dashboard endpoints** — they may need a
   `Session` cookie now; fall back gracefully if so.
5. **Hilt or Koin** if the container ever outgrows hand-rolled DI.
6. **iOS port** — a Kotlin Multiplatform extraction of the
   `data/`, `domain/`, and `providers/` modules would be a clean line.

## Privacy

This app stores nothing remotely. API keys are encrypted on-disk with a
hardware-backed key when available. The only network traffic is direct calls
from your device to the provider you authenticated with. There is no
telemetry. You can wipe everything from **Settings → Reset all data**.

## License

This project is intentionally unlicensed in v1 — pick a license (MIT/Apache-2)
before publishing.
>>>>>>> e2076cb (Initial commit: LLM Usage Monitor v1 scaffold)
