# कृषकसेवा (Krishak Seva) — Pitch Deck (10–12 Slides)

**Instructions:** Copy each slide into Google Slides, Canva, or PowerPoint. Use green/earth tones (farm theme). Add 2–3 screenshots from the app. Export as **PDF** for submission.

**Suggested title slide image:** App splash screen or dashboard screenshot.

---

## Slide 1 — Title

**कृषकसेवा**  
Smart Water, Crop & Advisory System

AI-powered farming assistant for Indian farmers

Ramakrishna Sunkara  
Hack2skill Prototype Submission · July 2026

---

## Slide 2 — The Problem

**Indian farmers face daily decisions without expert help**

- When to irrigate — wrong timing wastes water and hurts yield
- Which crop to grow — season, soil, and water must align
- Disease spreads fast — late detection can destroy a harvest
- Experts are scarce — agronomists are not in every village
- Language barrier — most digital tools are English-only

**Result:** Small and marginal farmers lose money, water, and crops.

---

## Slide 3 — Our Solution

**कृषकसेवा — one app, complete farm intelligence**

A mobile assistant that gives personalized advice using:

- Farmer profile (location, land, crop, water source)
- Live weather data
- Google Gemini AI
- Voice interaction in **English & Telugu**

All from a single dashboard — designed for smartphone farmers in rural India.

---

## Slide 4 — Who We Serve

| User | Benefit |
|------|---------|
| **Small & marginal farmers** | Expert-like guidance without visiting an office |
| **Telugu-speaking farmers** | Full UI + voice in their language |
| **Young farmers with phones** | Modern AI tools in a simple interface |
| **NGOs & extension workers** | Scalable digital advisory platform |

**Primary market:** Andhra Pradesh, Telangana, and similar agrarian regions.

---

## Slide 5 — Product Overview (Dashboard)

**One screen. Full farm picture.**

- Today's weather & 5-day forecast
- Water saving score
- Crop health indicator
- Irrigation advice — water today or wait?
- AI recommendation card
- Farm alerts & notifications
- Quick access: Crop · Weather · Doctor · Voice

*[Screenshot: Dashboard]*

---

## Slide 6 — Feature: Smart Crop Recommendation

**AI crop advisor by Indian season — groundwater-aware**

- Select **Kharif / Rabi / Zaid**
- Gemini analyzes **farmer profile + season + weather + mandi prices**
- **District groundwater data** (CGWB 2022 via [OpenCity.in](https://data.opencity.in/)) — 59 districts in AP & Telangana
- Stressed districts → AI prioritizes **low-water crops** (millets, pulses, groundnut)
- Returns ranked crops with:
  - Risk score
  - Water requirement
  - Expected yield
  - Fertilizer guidance
- Works offline with smart fallback (groundwater-aware)

**Talking point:** *We don't recommend rice in a critical groundwater district.*

*[Screenshot: Crop Recommendation screen]*

---

## Slide 6b — Data Innovation: OpenCity Groundwater (optional deep-dive slide)

**Real government data, farmer-ready**

| Item | Detail |
|------|--------|
| **Source** | [OpenCity.in](https://data.opencity.in/) — CKAN civic data commons |
| **Dataset** | CGWB National Compilation on Dynamic Ground Water Resources (2022) |
| **Coverage** | Andhra Pradesh (26) + Telangana (33) districts |
| **Key metric** | Stage of groundwater extraction (%) → Safe / Semi-critical / Critical |
| **Why not live gov API?** | India-WRIS portal unreliable; we bundle pre-processed data offline |

**Impact:** Crop advice respects **actual water availability** — critical for borewell-dependent farmers.

See **[GROUNDWATER_FEATURE.md](GROUNDWATER_FEATURE.md)** for full technical write-up.

---

## Slide 7 — Feature: Weather & Crop Doctor

**Weather Advisory**
- OpenWeather live data + 5-day forecast
- AI irrigation guidance based on crop + weather
- Actionable tips: when to water, when to delay

**Crop Doctor**
- Photograph affected leaves (CameraX)
- Gemini Vision diagnoses disease
- Treatment + prevention steps
- Scan history saved locally

*[Screenshot: Weather + Crop Doctor]*

---

## Slide 8 — Feature: Voice Assistant & Alerts

**कृषकसेवा Voice Advisor**
- Speak farming questions in English or Telugu
- AI answers in chosen language (text-to-speech)
- Suggested questions for first-time users
- Chat history stored offline

**Farm Alerts (FCM)**
- Push notifications for weather, irrigation, disease
- In-app notification center with filters
- Unread badge on dashboard

*[Screenshot: Voice Assistant + Notifications]*

---

## Slide 9 — How the AI Works

```
Farmer (Android App)
        ↓
Firebase Cloud Functions (secure backend)
        ↓
Google Gemini AI
        ↓
Personalized advice back to farmer
```

**Why this architecture?**
- API keys stay on server — never on farmer's phone
- Scalable — same backend serves thousands of farmers
- Safe — Firebase Auth protects user data
- Offline fallbacks when network is weak

**AI powers:** Crop recommendation · Weather advisory · Disease vision · Voice Q&A

**Data sources:** OpenWeather · India Post Pincode API · Agmarknet (mandi) · **OpenCity CGWB groundwater (2022)**

---

## Slide 10 — Built for India (Not Generic)

**Localization & accessibility**
- Full **Telugu UI** (`values-te/strings.xml`)
- Runtime language switch before login
- Voice input & output in farmer's language
- **Pincode-based location** — India Post API auto-fills village, district, state

**Farmer-first UX**
- Phone OTP or guest login
- 3-step registration
- Offline cache (Room database)
- Works on mid-range Android phones (API 26+)

---

## Slide 11 — Why It's Deployable

**Production-ready foundations**

| Layer | Technology |
|-------|------------|
| App | Kotlin, Jetpack Compose, Material 3 |
| Architecture | Clean Architecture + MVI |
| Cloud | Firebase Auth, Firestore, FCM, Cloud Functions |
| AI | Google Gemini (server-side) |
| Data | Room offline cache, Firestore sync |
| Release | ProGuard, signed release APK |

**Already working:** Auth, profile sync, push notifications, 5 Cloud Functions, Telugu UI, release build.

**Next step for scale:** Play Store publish, district expansion, Hindi support, government/NGO partnerships.

---

## Slide 12 — Demo & Ask

**Try it today**

- **GitHub:** [your-repo-url]
- **APK:** GitHub Releases
- **Demo video:** [your-video-link]

**कृषकसेवा**  
*Smart decisions. Better harvests. In your language.*

Thank you — questions?

Ramakrishna Sunkara  
ramakrishna.sunkara@email.com *(add your contact)*

---

## Design tips for PDF export

1. **Font:** Clean sans-serif (Inter, Roboto, Poppins)
2. **Colors:** Green `#2E7D32`, earth brown, white backgrounds
3. **Screenshots:** Dashboard, Crop AI, Crop Doctor, Telugu UI, Voice
4. **Keep text large** — judges may view on phone
5. **10–12 slides max** — delete Slide 12 contact if you need exactly 10
