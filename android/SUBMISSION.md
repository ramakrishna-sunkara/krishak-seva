# Krishak Seva — Prototype Submission (कृषकसेवा)

**Deadline:** 8 July 2026, 11:59 PM IST  
**Submit at:** Hack2skill dashboard → Submissions section

---

## Copy-paste for submission form

### Brief description (short — ~150 words)

**Krishak Seva** is an AI-powered Android farming assistant built for Indian farmers, especially in Telugu-speaking regions. Farmers register their land profile (pincode, village, crop, soil, water source) and get personalized advice from one dashboard: smart crop recommendations by season (Kharif/Rabi/Zaid) **enriched with district-level CGWB groundwater data from [OpenCity.in](https://data.opencity.in/)**, weather + irrigation guidance, leaf disease detection via camera or gallery upload, voice Q&A in English or Telugu, and push alerts for farm risks.

The app uses Google Gemini AI through secure Firebase Cloud Functions (API keys never stored on the device), OpenWeather for forecasts, and India Post pincode API for easy location entry. It works offline with cached data and local fallbacks when the network is weak — designed for real village conditions.

**Tagline:** Smart Water, Crop & Advisory System

---

### Brief description (one paragraph — if field is smaller)

Krishak Seva is a mobile AI farming companion for Indian farmers. It combines crop recommendations, weather and irrigation advisories, AI crop disease scanning from leaf photos (camera or gallery), a Telugu/English voice assistant, and push notifications — all personalized to the farmer’s profile. Built with Kotlin, Jetpack Compose, Firebase, and Google Gemini.

---

### Technologies used

| Category | Technologies |
|----------|--------------|
| **Mobile app** | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| **Architecture** | Clean Architecture, MVI, Repository pattern, Hilt (DI) |
| **Local data** | Room Database, DataStore Preferences |
| **Backend / cloud** | Firebase Auth, Firestore, Cloud Functions, FCM, Firebase Storage, Crashlytics |
| **AI** | Google Gemini API (via Cloud Functions — crop, weather, disease vision, voice Q&A) |
| **APIs** | OpenWeather API, India Post Pincode API (`api.postalpincode.in`), Agmarknet (via Cloud Function) |
| **Open data** | [OpenCity.in](https://data.opencity.in/) — CGWB Ground Water Resources India 2022 (AP & Telangana district CSVs) |
| **Android** | CameraX, SpeechRecognizer, Text-to-Speech, WorkManager |
| **Networking** | Retrofit, OkHttp, Kotlinx Serialization |
| **Build** | Gradle (KTS), ProGuard/R8 release builds |

**Paste as bullet list:**

```
Kotlin, Jetpack Compose, Material 3, Clean Architecture, MVI, Hilt, Room, DataStore, Retrofit, Firebase (Auth, Firestore, Cloud Functions, FCM, Storage, Crashlytics), Google Gemini AI, OpenWeather API, India Post Pincode API, OpenCity CGWB Groundwater Data (2022), CameraX, SpeechRecognizer, Text-to-Speech, Navigation Compose, ProGuard
```

---

### GitHub repository URL

```
https://github.com/ramakrishna-sunkara/krishak-seva
```

---

### Working prototype link

**Submit this single page to Hack2skill** (hosts all links, videos, docs):

| Item | URL |
|------|-----|
| **Submission landing page** | `https://ramakrishna-sunkara.github.io/krishak-seva/` (GitHub Pages from `/docs`) |
| **Local preview** | Open `docs/index.html` in browser |

**GitHub Pages setup:** repo **Settings → Pages → Branch: `main` → Folder: `/docs`** → Save.

> GitHub Pages only supports **`/` (root)** or **`/docs`** — not `/submission`. The page lives in `docs/index.html`.

Before submitting, edit `docs/index.html` → `CONFIG` → paste your **YouTube demo video URLs**.

---

This is an **Android app** (not only a website). Also use **one or more** of these:

| Option | What to submit | Best for |
|--------|----------------|----------|
| **A. GitHub Release (recommended)** | `https://github.com/ramakrishna-sunkara/krishak-seva/releases/latest` | Judges download APK directly |
| **B. Demo video** | YouTube / Google Drive link of 3–5 min walkthrough | Shows app working without install |

**Your recording (local):**

| Property | Value |
|----------|--------|
| **Source file** | `~/Downloads/Record_2026-07-08-22-04-16.mp4` |
| **Duration** | ~9 min 45 sec (consider trimming to 5 min for judges) |
| **Size** | ~318 MB (upload compressed copy from `android/demo/`) |
| **Format** | 720×1600 portrait screen recording (good for mobile demo) |
| **Compressed copy** | `android/demo/krishak-seva-demo-compressed.m4v` |

**Upload before submit (pick one):**

1. **Google Drive (fastest)** — Upload compressed MP4 → Share → “Anyone with the link” → paste link in Hack2skill form  
2. **YouTube (best for judges)** — Upload as **Unlisted** → title: `Krishak Seva — Android Demo | Hack2skill` → paste link  
3. **GitHub Release asset** — Attach only if under ~100 MB after compression (optional extra)

**Paste in submission form (after upload):**

```
Demo video: <YOUR_YOUTUBE_OR_DRIVE_LINK>
APK: https://github.com/ramakrishna-sunkara/krishak-seva/releases/latest
```

| **C. Google Drive APK** | Public link to `app-release-unsigned.apk` | Quick share if no GitHub yet |

**Recommended:** Submit **GitHub Releases URL + demo video link** in the description field if the form allows only one URL, put the video link and mention APK in GitHub Releases in the description.

**APK location after build:**
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

**Install note for judges:** Enable “Install from unknown sources” on Android, then install the APK. Firebase backend must be deployed for full AI features (see README.md).

---

## Submission checklist (do in order)

### Today (Sunday — focus day)

- [ ] **1. Pitch deck PDF** — Use [PITCH_DECK.md](PITCH_DECK.md) → create 10–12 slides in Google Slides / Canva / PowerPoint → Export as PDF
- [ ] **2. Demo video (3–5 min)** — Record: language switch → register with pincode → dashboard → crop AI → weather → crop doctor → voice → notification  
  - **Recorded:** `Record_2026-07-08-22-04-16.mp4` (~9:45 — trim or use as-is)  
  - **Next:** Upload to YouTube (Unlisted) or Google Drive → paste link in dashboard
- [ ] **3. GitHub** — Push code (see commands below)
- [ ] **4. GitHub Release** — Upload `app-release-unsigned.apk` as release asset
- [ ] **5. Firebase** — Confirm Cloud Functions + Firestore rules are deployed (`kisan-alert-99bb3`)
- [ ] **6. Test on real phone** — Full flow once before submitting
- [ ] **7. Hack2skill dashboard** — Fill all fields, cross-check, submit

### GitHub setup

Push from the repository root. The Android app lives in `android/`; web is a separate project in `web/`.

```bash
cd /path/to/krishak-seva
git add .
git commit -m "Krishak Seva — AI farming assistant for Indian farmers"

# Create repo on GitHub (github.com → New repository → krishak-seva → Public)
git remote add origin https://github.com/ramakrishna-sunkara/krishak-seva.git
git branch -M main
git push -u origin main
```

### GitHub Release (prototype link)

1. GitHub repo → **Releases** → **Create a new release**
2. Tag: `v1.0.1`
3. Title: `Krishak Seva v1.0.1 — Hackathon Prototype`
4. Upload: `app/build/outputs/apk/release/app-release-unsigned.apk`
5. Description: link to APP_OVERVIEW.md and demo video
6. Publish → copy release URL for submission form

### Before pushing — secrets check

These are **gitignored** and safe (not pushed):

- `local.properties` (OpenWeather API key)

**Do not commit:** Gemini API keys, private keystores, or passwords. Gemini key lives only in Firebase Functions secrets.

---

## 5-minute demo script (for video or live pitch)

| Time | Show | Say |
|------|------|-----|
| 0:00 | Splash + Telugu language | “कृषकसेवा — farmer selects Telugu, entire UI switches.” |
| 0:30 | Pincode registration | “Enter pincode — village, district, state auto-fill.” |
| 1:00 | Dashboard | “Weather, water score, crop health, irrigation tip in one screen.” |
| 1:30 | Crop recommendation | “AI suggests Kharif crops with risk, water, fertilizer.” |
| 2:00 | Weather advisory | “5-day forecast + when to irrigate.” |
| 2:30 | Crop Doctor | “Photo of leaf → disease name + treatment.” |
| 3:00 | Voice assistant | “Ask in Telugu — spoken AI answer.” |
| 3:30 | Push notification | “Farm alert → bell badge → read advisory.” |
| 4:00 | Closing | “कृषकसेवा — built for real Indian farmers, offline-friendly, Telugu-first.” |

---

## Files judges should read

| File | Purpose |
|------|---------|
| [APP_OVERVIEW.md](APP_OVERVIEW.md) | Non-technical product overview |
| [README.md](README.md) | Build, Firebase deploy, developer setup |
| [PITCH_DECK.md](PITCH_DECK.md) | Slide content for PDF deck |
| [GROUNDWATER_FEATURE.md](GROUNDWATER_FEATURE.md) | Groundwater data integration (OpenCity / CGWB) |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Technical architecture |

---

## Team formation

Hack2skill dashboard → **Team Formation** → add up to 4 members before final submit.

---

## One-line elevator pitch

> कृषकसेवा puts AI crop, weather, disease, and voice advice in one Telugu-friendly Android app — built for Indian farmers who need answers in the field, not in English-only apps.
