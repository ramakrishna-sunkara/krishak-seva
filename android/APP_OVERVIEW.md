# कृषकसेवा (Krishak Seva) — App Overview

**A simple guide for judges, farmers, product reviewers, and anyone who wants to understand what this app does — without reading code.**

---

## What is कृषकसेवा?

**कृषकसेवा** is a mobile farming assistant built for Indian farmers. It helps with everyday farm decisions using **weather data**, **crop advice**, **disease detection from leaf photos**, and a **voice assistant** — in **English** or **Telugu**.

Think of it as a **smart farm companion in your pocket**: it knows your land details, your current crop, and your local weather, then gives practical suggestions you can act on.

**Tagline:** *Smart Water, Crop & Advisory System*

---

## Who is it for?

| User | How they benefit |
|------|------------------|
| **Small & marginal farmers** | Clear advice without needing an agronomist on every visit |
| **Farmers in Telugu-speaking regions** | Full app UI and voice answers in Telugu |
| **Younger farmers with smartphones** | Quick access to AI-powered tools from one dashboard |
| **Extension workers / NGOs** | A demo-ready tool to show digital advisory at scale |

---

## What problem does it solve?

Many farmers face the same challenges:

- **When to irrigate** — too much or too little water hurts the crop and wastes money
- **Which crop to grow** — season, soil, and water must match
- **Early disease detection** — spotting leaf problems late can destroy a harvest
- **Where to get trusted advice** — experts are not always available in the village
- **Language barrier** — most digital tools are English-only

कृषकसेवा brings these answers into **one simple app**, personalized using the farmer’s own profile (village, farm size, soil, water source, current crop).

---

## Main features (in plain language)

### 1. Sign in & farmer profile
- Sign in with **phone OTP** or continue as **guest**
- Choose **English** or **Telugu** before you start
- Register in 3 easy steps: **personal details → farm details → current crop**
- Profile is saved and used to personalize all advice

### 2. Farm dashboard (home screen)
Your daily snapshot at a glance:
- **Today’s weather** for your area
- **Water saving score** — how efficiently you might be using water
- **Crop health** indicator
- **Irrigation advice** — should you water today?
- **AI recommendation** — short smart tip for your farm
- **Recent alerts** — weather, irrigation, or disease notifications
- Quick buttons to open **Crop**, **Weather**, **Doctor**, **Voice**, **Profile**, and **Settings**

### 3. Smart crop recommendation
- Pick the season: **Kharif**, **Rabi**, or **Zaid**
- AI suggests suitable crops for **your farm conditions**
- Uses **official groundwater data** for your district (Andhra Pradesh & Telangana) sourced from [OpenCity.in](https://data.opencity.in/) — CGWB 2022 national compilation — so water-stressed areas get low-water crop bias
- Each suggestion shows:
  - Why that crop fits
  - Water needs
  - Expected yield
  - Fertilizer guidance
  - Risk score (lower is safer)

**Details:** [GROUNDWATER_FEATURE.md](GROUNDWATER_FEATURE.md)

### 4. Weather & irrigation advisory
- **Current weather** (temperature, humidity, rain, wind)
- **5-day forecast**
- **AI irrigation guidance** — e.g. when to water, when to wait for rain
- **Farm tips and alerts** based on weather + your crop

### 5. Crop Doctor (leaf disease scanner)
- Open the camera and photograph affected **crop leaves**
- AI analyzes the image and returns:
  - Likely disease name
  - Confidence level
  - Symptoms
  - Treatment steps
  - Prevention tips
- **Scan history** is saved so you can review past diagnoses

### 6. Voice assistant (कृषकसेवा Voice Advisor)
- **Speak** your farming question (English or Telugu)
- Or **type** if you prefer
- AI answers in your chosen language
- **Suggested questions** help you get started (e.g. irrigation, pests, fertilizer)
- Conversation history is kept in the app

### 7. Farm alerts & notifications
- Receive **push notifications** for weather, irrigation, and crop health
- View all alerts in the **Notifications** screen
- Filter by type; mark as read
- Unread count shown on the dashboard bell icon

### 8. Profile & settings
- View and **edit** your farm profile anytime
- Change **language** (English / Telugu)
- Turn **farm alerts** on or off
- **Sign out** when needed

---

## How to use the app (step by step)

```
1. Open app  →  See welcome screen
2. Choose language  →  English or తెలుగు
3. Sign in  →  Phone OTP or Guest
4. Complete farmer profile  →  Name, village, farm size, crop, etc.
5. Land on Dashboard  →  See weather, scores, and quick actions
6. Use any feature:
      • Crop      →  Get season-based crop ideas
      • Weather   →  Check forecast + irrigation advice
      • Doctor    →  Scan a leaf photo for disease
      • Voice     →  Ask a question by voice or text
      • Bell icon →  Read farm alerts
7. Update profile or settings anytime from the dashboard
```

---

## Languages

| Language | UI | Voice input | Voice / text answers |
|----------|----|-------------|----------------------|
| English  | ✅ | ✅          | ✅                   |
| Telugu   | ✅ | ✅          | ✅                   |

Language can be changed on the **login screen** or in **Settings**.

---

## Works with or without internet

| Situation | What still works |
|-----------|------------------|
| **Good internet** | Full AI features (crop, weather, disease, voice) powered by Google Gemini via secure cloud |
| **Weak / no internet** | Saved profile, cached weather, offline fallback advice, and past scan history |
| **No account** | Guest mode for trying the app (limited persistence) |

The app is designed so farmers are **not left with a blank screen** when the network drops.

---

## How the AI works (simple explanation)

- The **phone app** collects your question, photo, or farm details.
- A **secure cloud service** (Firebase) sends that to **Google Gemini AI**.
- Gemini generates the advice; the app shows it in a **clean, readable card**.
- **API keys stay on the server** — they are never stored inside the app on the farmer’s phone.

This keeps the app safer and easier to maintain.

---

## 5-minute demo script (for presentations)

| Step | What to show | What to say |
|------|--------------|-------------|
| 1 | Splash + language picker | “कृषकसेवा splash — farmer chooses Telugu, entire UI switches.” |
| 2 | Registration | “We capture village, farm size, soil, water, and current crop.” |
| 3 | Dashboard | “One screen: weather, water score, crop health, irrigation tip.” |
| 4 | Crop recommendation | “AI suggests crops for Kharif — uses CGWB groundwater for your district so we don't push rice where water is critical.” |
| 5 | Weather advisory | “5-day forecast plus when to irrigate.” |
| 6 | Crop Doctor | “Take a leaf photo — get disease name and treatment.” |
| 7 | Voice assistant | “Ask in Telugu: ‘ఈ రోజు నీరు పెట్టాలా?’ — spoken answer.” |
| 8 | Notification | “Push alert arrives → badge on bell → farmer reads advisory.” |

---

## What makes कृषकसेवा different?

1. **Built for Indian farmers** — seasons (Kharif/Rabi/Zaid), acres, village-based profile
2. **Telugu-first mindset** — not just English with a translation bolted on
3. **All-in-one** — crop + weather + disease + voice + alerts in a single app
4. **Voice for low-literacy users** — speak instead of typing long questions
5. **Offline-friendly** — cached data and fallbacks when connectivity is poor
6. **Privacy-conscious AI** — keys and heavy AI processing stay in the cloud
7. **Groundwater-aware crop AI** — district-level CGWB data (via [OpenCity.in](https://data.opencity.in/)) combined with profile, season, and weather

---

## App information

| Item | Detail |
|------|--------|
| **App name** | कृषकसेवा (Krishak Seva — Farmer Service) |
| **Version** | 1.0.1 |
| **Platform** | Android |
| **Target users** | Indian farmers (especially Telugu-speaking regions) |
| **Core technologies** | Android app + Firebase + Google Gemini AI + OpenWeather + OpenCity CGWB groundwater data |

---

## For technical readers

If you need setup, build commands, Firebase deployment, or code architecture, see:

- **[README.md](README.md)** — developer setup, build, and deploy
- **[ARCHITECTURE.md](ARCHITECTURE.md)** — code structure for engineers
- **[GROUNDWATER_FEATURE.md](GROUNDWATER_FEATURE.md)** — groundwater data integration (OpenCity / CGWB), pitch points

---

## One-sentence summary

> **कृषकसेवा is an AI-powered Android app that helps Indian farmers make better decisions about water, crops, and plant health — in their own language, from one simple dashboard.**
