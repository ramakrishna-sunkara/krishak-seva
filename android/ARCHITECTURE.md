# Architecture — कृषकसेवा (Krishak Seva)

कृषकसेवा uses **Jetpack Compose + MVI + Clean Architecture**.

## Layer overview

```
┌─────────────────────────────────────────────────────────┐
│  presentation/   Compose UI, ViewModels, navigation     │
│       │ dispatches Event, observes UiState              │
│       ▼                                                 │
│  domain/         models, repository interfaces, use cases │
│       ▲                                                 │
│       │ implements                                      │
│  data/           Room, Retrofit, Firebase, mappers      │
└─────────────────────────────────────────────────────────┘
         di/ wires implementations via Hilt
```

**Dependency rule:** `presentation → domain ← data`. Presentation never imports `data`.

## MVI (Model-View-Intent)

Each screen follows unidirectional data flow:

| Piece | Role |
|-------|------|
| **View** | Compose screen; collects `uiState`; calls `viewModel.onEvent(...)` |
| **Event** | Sealed interface — user/system actions (`Refresh`, `SeasonSelected`, …) |
| **State** | Immutable `*UiState` data class — single source of truth for UI |
| **ViewModel** | Extends `MviViewModel<Event, State>`; handles events; calls use cases |

```kotlin
// UI
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
viewModel.onEvent(DashboardEvent.Refresh)

// ViewModel
override fun onEvent(event: DashboardEvent) {
    when (event) {
        DashboardEvent.Refresh -> loadDashboard(forceRefresh = true)
        // ...
    }
}
```

Base class: `presentation/base/MviViewModel.kt`

### Screen patterns

| Screen | Pattern |
|--------|---------|
| Splash, Dashboard, Crop, Weather | Full MVI — sealed `*Event` + `onEvent` |
| Auth, Farmer Registration | UiState + typed handlers (form-heavy; each handler maps to one intent) |

## Clean Architecture rules

1. **Use cases** — ViewModels call `domain/usecase/*`, not repositories directly.
2. **Repository interfaces** — defined in `domain/repository/`, implemented in `data/repository/`.
3. **Domain models** — `domain/model/`; DTOs stay in `data/remote/dto/`.
4. **Gemini / AI** — only via Firebase Cloud Functions, never from Android.
5. **Offline fallbacks** — local advisors in `core/utils/`, invoked from data layer.
6. **Open government data** — groundwater district assessments bundled from OpenCity CGWB CSVs; see [GROUNDWATER_FEATURE.md](GROUNDWATER_FEATURE.md).

## Groundwater data flow (crop recommendations)

```
FarmerProfile (state, district)
    → GroundWaterAssessmentRepository
    → assets/groundwater/groundwater_districts.json
    → CropRecommendationRequest (category, stage %)
    → Cloud Function getCropRecommendation → Gemini prompt
```

District name aliases handle India Post vs CGWB naming (e.g. `Anantapur` → `Ananthapuramu`).

## Package map

```
com.kisanalert/
├── presentation/     # feature screens + ViewModels + *UiState + *Event
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── data/
│   ├── local/
│   ├── remote/
│   ├── repository/
│   └── mapper/
├── core/             # theme, shared UI, Result, constants
└── di/               # Hilt modules
```

## Adding a new feature

1. Define domain models and repository interface.
2. Implement repository in `data/`.
3. Add use case(s) in `domain/usecase/`.
4. Create `*UiState`, sealed `*Event`, and `*ViewModel` extending `MviViewModel`.
5. Build Compose screen observing `uiState` and dispatching events.
6. Register bindings in `di/`.
7. Add navigation route in `KrishakSevaNavHost.kt`.
