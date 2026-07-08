# कृषकसेवा (Krishak Seva)

AI-powered farming assistant for Indian farmers.

> **New here?** Read **[APP_OVERVIEW.md](APP_OVERVIEW.md)** for a plain-language guide (features, user flow, demo script) aimed at judges, farmers, and non-technical reviewers.  
> **Groundwater + crop AI:** See **[GROUNDWATER_FEATURE.md](GROUNDWATER_FEATURE.md)** for data source (OpenCity / CGWB), architecture, and pitch talking points.  
> This README is for **developers** — setup, build, and deployment.

## Architecture

**Jetpack Compose + MVI + Clean Architecture**

- **Presentation:** Compose screens, `MviViewModel`, sealed `*Event`, `*UiState`
- **Domain:** models, repository contracts, use cases
- **Data:** Room, Retrofit, Firebase implementations

See [ARCHITECTURE.md](ARCHITECTURE.md) for layer rules and how to add features.

## App name

**Display name:** कृषकसेवा (Krishak Seva — Farmer Service)  
**Package / Firebase project:** `com.kisanalert` / `kisan-alert-99bb3` *(unchanged — internal identifiers)*

## Features completed

### Feature 1–4
Foundation, Auth, Farmer Registration, Dashboard

### Feature 5: Smart Crop Recommendation
- Season selector (Kharif / Rabi / Zaid)
- Firebase Cloud Function → Gemini API (no keys in Android)
- Beautiful recommendation cards with risk score, water, yield, fertilizer
- **Groundwater-aware recommendations** — district-level CGWB data (AP + Telangana) from [OpenCity.in](https://data.opencity.in/); see **[GROUNDWATER_FEATURE.md](GROUNDWATER_FEATURE.md)**
- Room offline cache
- Local smart fallback when Cloud Function unavailable (groundwater-aware)

### Feature 6: Weather Advisory
- Current weather + 5-day forecast (OpenWeather)
- AI irrigation advice via `getWeatherAdvisory` Cloud Function
- Room advisory cache + offline fallback
- Material 3 advisory screen

### Feature 7: Crop Doctor
- CameraX leaf photo capture
- Firebase Storage upload + `detectCropDisease` Cloud Function (Gemini vision)
- Disease diagnosis with treatment and prevention tips
- Room scan history + offline fallback

### Feature 8: Voice Assistant
- SpeechRecognizer input (English / Telugu)
- Text-to-speech responses in selected language
- `askFarmingQuestion` Cloud Function (Gemini)
- Chat-style conversation history in Room
- Suggested follow-up questions + typed fallback input

### Feature 9: Notifications (FCM)
- FCM token sync to Firestore `fcm_tokens/{userId}`
- Push notification receive + system tray display
- Notifications screen with weather/irrigation/disease filters
- Unread badge on dashboard bell icon
- Mark read / mark all read
- `sendFarmAlert` Cloud Function for testing pushes

### Feature 10: Farmer Profile
- View profile summary (personal, location, farm, crop)
- Edit all registration fields in one scrollable form
- Save to Room + sync to Firestore
- Sync status indicator (synced / pending)

### Feature 11: Settings
- Preferred language (English / Telugu) synced to farmer profile
- Farm alerts notification toggle (DataStore preference)
- Notification permission + system settings shortcuts
- Sign out with confirmation
- About section with app version

## Polish (v1.0.1)
- Full Telugu UI via `values-te/strings.xml` + runtime locale switching
- Language picker on auth screen (before sign-in)
- Registration onboarding tip + first-visit dashboard guide
- Release build with ProGuard rules

## Deploy Firestore Security Rules (required)

Without these rules, Firestore writes (FCM token sync, farmer profile) fail with `PERMISSION_DENIED`.

```bash
firebase deploy --only firestore:rules --project kisan-alert-99bb3
```

Rules are in `firestore.rules` at the project root.

## Deploy Cloud Functions (Gemini AI)

### 1. Link Firebase project (one-time)
From project root:
```bash
cd /Users/ramakrishnasunkara/Desktop/hack2skill-challenge
firebase login
firebase use kisan-alert-99bb3
```

### 2. Set Gemini API key secret
**Secret name must be `GEMINI_API_KEY`** (not the key value itself):
```bash
firebase functions:secrets:set GEMINI_API_KEY --project kisan-alert-99bb3
```
When prompted, paste your Gemini API key from https://aistudio.google.com/apikey

### 3. Install & deploy
```bash
cd functions
npm install
cd ..
firebase deploy --only functions:getCropRecommendation,functions:getWeatherAdvisory,functions:detectCropDisease,functions:askFarmingQuestion,functions:sendFarmAlert --project kisan-alert-99bb3
```

### Common mistakes
| Wrong | Correct |
|-------|---------|
| `firebase functions:secrets:set AIzaSy...` | `firebase functions:secrets:set GEMINI_API_KEY` then paste key when prompted |
| Running from `functions/` without project | Use `--project kisan-alert-99bb3` or run `firebase use` from root |

## Android setup
1. Firebase `google-services.json`
2. `local.properties` → `OPEN_WEATHER_API_KEY`
3. Enable Auth providers (Anonymous + Phone)
4. Enable Firestore

## Build

### Debug
```bash
./gradlew assembleDebug
```

### Release (unsigned APK)
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

For Play Store or signed installs, configure a signing key in `app/build.gradle.kts` under `signingConfigs` and reference it in the `release` build type.

## Test Telugu UI
1. Open app → Auth screen → select **తెలుగు**
2. UI reloads in Telugu (or set language in Settings after registration)
3. Voice assistant responses also use the selected language

## Hackathon submission

See **[SUBMISSION.md](SUBMISSION.md)** for copy-paste description, tech stack, GitHub setup, and prototype link instructions.  
See **[PITCH_DECK.md](PITCH_DECK.md)** for 10–12 slide pitch deck content (export to PDF).  
See **[GROUNDWATER_FEATURE.md](GROUNDWATER_FEATURE.md)** for groundwater data source, feature flow, and judge talking points.
