# Architecture

## Module graph

```
app  ─────────────┬─> feature:exchangelist ─┐
                   ├─> feature:exchangedetail┤
                   ├─> data ──────────────┐  │
                   ├─> domain <───────────┼──┴─> core:common
                   ├─> core:ui            │
                   └─> core:network <─────┘
```

- **`domain`** and **`core:common`** are plain Kotlin/JVM modules - no Android Gradle plugin. That
  buys fast compilation, unit tests that don't need Robolectric or an emulator, and a hard
  guarantee that business rules can't accidentally depend on Android or Retrofit/Moshi types.
- **`core:network`** owns the Retrofit/OkHttp/Moshi client and the API key wiring, and nothing
  else - it doesn't know CoinMarketCap's endpoints exist. That's `data`'s job.
- **`core:ui`** holds the Compose theme, shared state composables (loading/error/empty), the
  logo-with-fallback image component, and the formatters. Both feature modules depend on it; it
  depends on nothing feature-specific.
- **`data`** implements `domain`'s repository interfaces against CoinMarketCap - paging, error
  normalization, all of it.
- **`feature:exchangelist`** and **`feature:exchangedetail`** are self-contained: each exposes one
  `@Composable ...Route` and doesn't know the other exists. `app` is the only module that wires
  them together (see "Adaptive list-detail layout"), so neither feature depends on the other.

## Why CoinMarketCap access is shaped the way it is

The challenge points at two fields (`spot_volume_usd`, and a listing sorted implicitly by
relevance) that map naturally onto `GET /v1/exchange/listings/latest`. That endpoint isn't
actually reachable on the free plan, though - I got `403 - "Your API Key subscription plan
doesn't support this endpoint"` (error code 1006) the first time I tried it with the provided key.
Two endpoints are available on the free tier, and between them they cover every field the
challenge asks for:

| Field | Source |
|---|---|
| logo, name, spot_volume_usd, date_launched, id, description, website, maker_fee, taker_fee | `GET /v1/exchange/info` |
| currency.name, currency.price_usd | `GET /v1/exchange/assets` |

`/v1/exchange/info` takes a comma-separated `id` list and returns all of them for one API credit,
but has no pagination or sort of its own. `/v1/exchange/map` is the mirror image: cheap, supports
`sort=volume_24h` with `start`/`limit`, but only gives back `id`/`name`/`slug`.

**`ExchangePagingSource`** (`data/paging/ExchangePagingSource.kt`) stitches the two together: each
page asks `/v1/exchange/map` which ids belong on it, in volume order, then makes one batched
`/v1/exchange/info` call for exactly those ids. That's 2 credits per 20-item page - against the
key's 15,000/month budget that's roughly 7,500 pages, which is more than enough for manual testing
or a demo.

I checked all of this against the live API rather than trusting the docs - CoinMarketCap's public
API reference is a JS-rendered SPA I couldn't scrape for exact field shapes, so which endpoints
403 on this plan, whether errors come back as real HTTP statuses or HTTP 200 with an embedded
error code, and whether `/exchange/assets` can legitimately come back empty were all confirmed
with real `curl` calls first.

## Error handling model

Every method that can fail returns `AppResult<T>` (`core/common/result/AppResult.kt`) instead of
throwing. `SafeApiCallExecutor` (`data/remote/SafeApiCallExecutor.kt`) is the one place that turns
Retrofit/OkHttp/Moshi exceptions and CoinMarketCap's own `status.error_code` payloads into
`AppError`, a sealed type the UI can exhaustively branch on. `core/ui/error/ErrorMessage.kt` maps
each case to a user-facing string, with specific messages for rate limiting, an invalid API key,
and a plan-restricted endpoint - real CMC error codes, not generic guesses.

Paging 3 wants `LoadResult.Error` to carry a `Throwable`, not an `AppResult`, so
`AppError.asException()` / `Throwable.toAppError()` (`core/common/error/AppError.kt`) bridge the
two styles right at that boundary, without leaking Paging's `Throwable` contract anywhere else.

`AppError.NoConnectivity` and `AppError.Timeout` stay separate cases instead of one "network
problem" bucket because they mean different things. No connectivity means the device has no route
to the internet at all - `ConnectivityObserver` already knows this before the request is even
attempted. A timeout means the connection was fine, but this particular request, to this
particular server, didn't finish inside the 15s connect/read/write window
(`core/network/di/NetworkModule.kt`). `SafeApiCallExecutor` catches `SocketTimeoutException` ahead
of the general `IOException` branch to keep them apart (it's a subtype, so catch order matters
here). The split also drives the reconnect auto-retry logic below: reconnecting is a good reason
to retry a `NoConnectivity` failure, but it says nothing about whether a request that already
timed out would succeed now, so `Timeout` failures are left out of that auto-retry and only get
the manual retry button.

### Proactive connectivity

`ConnectivityObserver` (`core/common/connectivity/ConnectivityObserver.kt`) is a single interface
exposing `isConnected: StateFlow<Boolean>`. Its Android implementation
(`core/network/connectivity/AndroidConnectivityObserver.kt`) backs that with
`ConnectivityManager.registerDefaultNetworkCallback` instead of polling, and only calls a network
"connected" once it reports both `NET_CAPABILITY_INTERNET` and `NET_CAPABILITY_VALIDATED` - a
Wi-Fi network stuck behind a captive portal has the first but not the second, and would otherwise
read as connected while every real request keeps failing. It's a singleton, one system callback
registration for the app's whole lifetime, shared through an `@ApplicationScope` `CoroutineScope`
rather than something each consumer sets up on its own.

Three things read it, each for a different reason:

- **`SafeApiCallExecutor`** checks it before making any call. Skip this and a disconnected device
  would still sit through OkHttp's full connect timeout before landing on the exact same
  `AppError.NoConnectivity` - just slower. This is the one on the critical path for every request;
  the other two are more of a nicety.
- **`ExchangeDetailViewModel`** and **`ExchangeListRoute`** (the list is Paging-driven, so its
  `retry()` lives on `LazyPagingItems` in the UI layer, not the ViewModel) watch specifically for
  the disconnected→connected edge - not "is currently connected," which would refire on every
  unrelated recomposition - and auto-retry only whatever's currently sitting in a `NoConnectivity`
  error state. A plan-restricted or rate-limited failure next to it is left alone, since
  reconnecting doesn't fix those.
- **`ExchangeListDetailScreen`** shows a persistent banner whenever `isConnected` is false,
  regardless of whether anything has actually failed yet. That's the "proactive" part: the user
  finds out before tapping something and hitting a delayed error, not after.

### Independent failure domains on the detail screen

The detail screen has two data sources - the exchange record and its proof-of-reserve assets - and
they're modeled as two independent state machines in `ExchangeDetailUiState`
(`feature/exchangedetail/ExchangeDetailUiState.kt`), each with its own loading/success/error state
and retry action. A blip fetching proof-of-reserve assets doesn't blank out the name, fees, and
description that already loaded fine, and the reverse holds too. I considered a single combined
`Result<ExchangeDetailWithAssets>` first, but that would force one failure to hide data that was
otherwise perfectly available, which felt wrong for what's really two independent requests.

### Adaptive list-detail layout

`app/ui/ExchangeListDetailScreen.kt` hosts both screens through Material3's
`ListDetailPaneScaffold` (`androidx.compose.material3.adaptive:adaptive-*:1.2.0` - pinned below
the current 1.3.0 because that version needs compileSdk 37 / AGP 9.1, which nothing else here
requires). On a compact window it behaves like ordinary push navigation: list, then detail pushed
on top, with a back button. Once the window is wide enough, both panes show side by side instead,
with nothing to navigate "back" through. What decides that is **width**, not raw orientation.

The library's own default breakpoint for two panes is the Expanded window size class (840dp+),
which undersells what this challenge actually wants: a Pixel's landscape width is only ~731dp
(Medium, 600-840dp), so with the default breakpoint only tablets would ever see two panes.
`calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth` - a directive Google ships for exactly
this situation - lowers that threshold so an ordinary phone rotated to landscape gets the two-pane
layout, while a narrow compact window still collapses to one pane correctly. I verified this
on-device: the same instrumented run screenshots two panes at 731dp-wide landscape; the default
breakpoint doesn't.

Two things had to hold for rotation to neither re-fetch data nor lose scroll position, given that
the layout itself changes shape across that same rotation:

- **`ExchangeListViewModel` and the list's `LazyListState` are hoisted in
  `ExchangeListDetailScreen`**, above the branch that picks one pane vs. two. Both panes are
  composed from that single call site no matter the arrangement, so rotating never recreates
  either one - `cachedIn(viewModelScope)` and `rememberSaveable(saver = LazyListState.Saver)` only
  hold on to state if the instance itself survives, and here it always does.
- **`ExchangeDetailViewModel` doesn't source its exchange id from a nav-graph
  `SavedStateHandle` argument**, because there's no `NavBackStackEntry` per pane in this layout -
  both panes are just composables on the same screen. It's `@AssistedInject`-constructed instead,
  taking the id directly, and `ExchangeDetailRoute` requests it through
  `hiltViewModel(key = "exchange-detail-$id", creationCallback = ...)`. Keying by id has a nice
  side effect beyond surviving rotation: picking a second exchange, then coming back to the first
  one later in the same session, doesn't re-fetch either - each visited id keeps its own retained
  instance for the life of the screen.

### An empty assets list is not an error

CoinMarketCap doesn't guarantee every exchange publishes proof-of-reserve data - Kraken, for
instance, currently returns `"data": []` from `/v1/exchange/assets`, confirmed live.
`AssetsState.Success(emptyList())` renders its own "no assets disclosed" message, kept separate
from `AssetsState.Error` on purpose, so a legitimately empty response never gets mistaken for (or
coded as) a failure.

## Testing strategy

- **Unit tests** (`src/test`, plain JUnit4 + MockK + Truth + kotlinx-coroutines-test): mappers,
  `SafeApiCallExecutor` (against a real Retrofit stack via MockWebServer, rather than hand-built
  `HttpException`s), `ExchangePagingSource`, `ExchangeRepositoryImpl`, both ViewModels, and the
  domain use cases.
- **UI tests** (`src/androidTest`, Compose UI testing + JUnit4): each feature's screen content is
  split into a small, ViewModel-free `internal` composable (`ExchangeListContent`,
  `ExchangeDetailContent`) that takes plain state as parameters. Tests feed it fake
  `PagingData`/`UiState` directly - no Hilt, no network, no emulator-side test doubles - and check
  rendered text and click callbacks across the loading/error/empty/success paths.
- I left network-hitting tests out of the automated suite on purpose: an instrumented test that
  calls the real CoinMarketCap API would burn the shared free-tier credit budget on every CI run
  and be flaky under the plan's rate limits.
- `:app` has no androidTest sources of its own. Its one composable, `ExchangeListDetailScreen`, is
  exercised indirectly through the feature modules' tests plus manual on-device checks for the
  rotation/two-pane behavior specifically, which isn't practical to assert from a Compose UI test.
  AGP still wires up a `connectedAndroidTest` task for every application variant regardless of
  whether it has test sources, and running that empty task against this project's dependency graph
  turned out to be flaky at the framework level - `androidx.test.runner.AndroidJUnitRunner`
  intermittently missing from the merged dex, reproduced even against a freshly restarted emulator,
  so it wasn't stale device state. `app/build.gradle.kts` disables the androidTest variant outright
  (`androidComponents { beforeVariants { it.enableAndroidTest = false } }`) rather than carrying a
  flaky, valueless task.
