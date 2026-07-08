# Krishak Seva — Android

AI-powered farming assistant for Indian farmers (कृषकसेवा).

> **Overview:** [APP_OVERVIEW.md](APP_OVERVIEW.md)  
> **Groundwater feature:** [GROUNDWATER_FEATURE.md](GROUNDWATER_FEATURE.md)  
> **Architecture:** [ARCHITECTURE.md](ARCHITECTURE.md)  
> **Submission:** [SUBMISSION.md](SUBMISSION.md)

## Setup

1. Copy `local.properties.example` to `local.properties`
2. Add `OPEN_WEATHER_API_KEY` in `local.properties`
3. Place Firebase `google-services.json` in `app/`
4. Enable Auth providers (Anonymous + Phone) and Firestore in Firebase Console

## Build

```bash
./gradlew assembleDebug
```

Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`

Open this **`android/`** folder in Android Studio.

## Firebase

From this `android/` directory:

```bash
firebase login
firebase use kisan-alert-99bb3
firebase deploy --only firestore:rules --project kisan-alert-99bb3
cd functions && npm install && cd ..
firebase functions:secrets:set GEMINI_API_KEY --project kisan-alert-99bb3
firebase deploy --only functions --project kisan-alert-99bb3
```

## Features

- Farmer registration and dashboard
- Smart crop recommendation (groundwater-aware)
- Weather advisory
- Crop Doctor (camera + gallery upload)
- Voice assistant (English / Telugu)
- Push notifications (FCM)
- Full Telugu UI
