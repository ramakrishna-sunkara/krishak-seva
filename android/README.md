# Krishak Seva — Android

**Krishak Seva** (कृषकसेवा) is an AI-powered farming assistant for Indian farmers — weather, crop advice, disease detection, and voice Q&A in **English** and **Telugu**.

**Tagline:** *Smart Water, Crop & Advisory System*

## Documentation

| Document | Purpose |
|----------|---------|
| [APP_OVERVIEW.md](APP_OVERVIEW.md) | Plain-language guide for judges, farmers, and reviewers |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Code structure, MVI pattern, and how to add features |
| [GROUNDWATER_FEATURE.md](GROUNDWATER_FEATURE.md) | Groundwater-aware crop recommendations |
| [PROJECT_WRITEUP.md](PROJECT_WRITEUP.md) | Problem, solution, tools, and architecture diagram |

## Tech stack

- **UI:** Kotlin, Jetpack Compose, Material 3, CameraX
- **Architecture:** Clean Architecture, MVI, Hilt, Repository pattern
- **Local:** Room, DataStore
- **Backend:** Firebase Auth, Firestore, Cloud Functions, Storage, FCM
- **AI:** Google Gemini (via Cloud Functions only — no API keys on device)
- **APIs:** OpenWeather, India Post Pincode API
- **Data:** CGWB groundwater districts (AP + Telangana) bundled as JSON asset

## Prerequisites

1. Android Studio (open this **`android/`** folder as the project root)
2. JDK 17+
3. Firebase `google-services.json` in `app/`
4. `local.properties` with `OPEN_WEATHER_API_KEY` (copy from `local.properties.example`)

## Build

```bash
./gradlew assembleDebug
```

Release APK:

```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

## Firebase setup

Run from this `android/` directory:

```bash
firebase login
firebase use kisan-alert-99bb3
firebase deploy --only firestore:rules --project kisan-alert-99bb3
cd functions && npm install && cd ..
firebase functions:secrets:set GEMINI_API_KEY --project kisan-alert-99bb3
firebase deploy --only functions --project kisan-alert-99bb3
```

> **Note:** `kisan-alert-99bb3` is the Firebase project ID (legacy). The app display name is **Krishak Seva**.

## Features

| Feature | Description |
|---------|-------------|
| Dashboard | Weather, water score, crop health, irrigation tips, alerts |
| Crop recommendation | Kharif / Rabi / Zaid with groundwater-aware AI |
| Weather advisory | 5-day forecast + irrigation guidance |
| Crop Doctor | Camera capture or gallery upload for leaf disease detection |
| Voice assistant | Ask farming questions by voice or text |
| Notifications | FCM push alerts for farm risks |
| Profile & settings | Edit farmer profile, language (EN/TE), sign out |

## Project layout

```
android/
├── app/                 # Android application module
├── functions/           # Firebase Cloud Functions (Gemini AI)
├── data/                # Source CSVs for groundwater asset
├── scripts/             # Data processing (e.g. generate JSON asset)
├── firebase.json
├── firestore.rules
└── storage.rules
```

## Internal identifiers

| Item | Value | Notes |
|------|-------|-------|
| Display name | Krishak Seva | Shown in launcher and UI |
| Application class | `KrishakSevaApplication` | Hilt entry point |
| Package name | `com.kisanalert` | Legacy — unchanged for Firebase/Play compatibility |
| Firebase project | `kisan-alert-99bb3` | Legacy project ID |

## Test Telugu UI

1. Open app → Auth screen → select **తెలుగు**
2. Entire UI switches to Telugu
3. Voice assistant responses follow the selected language
