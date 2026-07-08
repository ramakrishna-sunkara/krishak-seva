# Groundwater-Aware Crop Recommendations — कृषकसेवा

**Feature status:** Implemented  
**Data source:** [OpenCity.in](https://data.opencity.in/) — CGWB National Compilation on Dynamic Ground Water Resources of India (2022)  
**Coverage:** Andhra Pradesh (26 districts) + Telangana (33 districts) = **59 district records**

---

## Why this feature exists

Crop choice in India depends heavily on **water availability**, not just soil and season. Many farmers rely on **borewells** in regions where groundwater is already **semi-critical, critical, or over-exploited**. Recommending water-intensive crops (e.g. rice, sugarcane) in such districts can worsen extraction and crop failure risk.

**कृषकसेवा** enriches AI crop recommendations with **official district-level groundwater assessment data** so advice reflects real water stress in the farmer’s location.

---

## Problem we solved

| Challenge | Our approach |
|-----------|--------------|
| India-WRIS DataDownload portal often broken or slow | Used **OpenCity CKAN mirror** of CGWB 2022 district CSVs |
| No public REST API for live groundwater lookup | **Pre-process CSV → bundle JSON in app** (offline-first, no runtime gov dependency) |
| India Post / profile district names differ from CGWB names | **District alias matcher** (e.g. `Anantapur` → `Ananthapuramu`, `YSR Kadapa` → `Y.S.R Kadapa`) |
| Block-level data hard to obtain quickly | **District-level MVP** aligned with farmer profile fields today; block-level planned as Phase 2 |

---

## Data source — OpenCity.in

**Portal:** [https://data.opencity.in/](https://data.opencity.in/)  
**Dataset:** National Compilation on Dynamic Ground Water Resources of India — **2022**  
**Dataset URL:** [opencity.in — GW Resources India 2022](https://data.opencity.in/dataset/national-compilation-on-dynamic-ground-water-resources-of-india-2022)

OpenCity hosts civic and public datasets in CKAN format. We downloaded state-wise CSV exports for:

- **Andhra Pradesh** — `andhra_pradesh_groundwater.csv`
- **Telangana** — `telangana_groundwater.csv`

### Key CSV columns used

| Column | Usage |
|--------|--------|
| `Name of District` | Lookup key (matched to farmer profile district) |
| `Stage of GW extraction (%)` | Primary signal — how much of available groundwater is already used |
| `Annual Extractable Groundwater Resource` | Context for Gemini / future analytics |
| `Total Annual Extraction` | Context for stress analysis |
| `Net GW availability for future` | Remaining allocatable resource |

### Category mapping (CGWB standard)

| Stage of extraction | Category | Crop guidance bias |
|---------------------|----------|-------------------|
| &lt; 70% | **Safe** | Most crops feasible with normal irrigation |
| 70–90% | **Semi-critical** | Prefer moderate-water crops; efficient irrigation |
| 90–100% | **Critical** | Low-water crops (groundnut, pulses, millets) |
| &gt; 100% | **Over-exploited** | Rain-fed / drought-tolerant crops only |

**Example districts (2022 data):**

| District | State | Stage % | Category |
|----------|-------|---------|----------|
| West Godavari | Andhra Pradesh | 10.83% | Safe |
| Ananthapuramu | Andhra Pradesh | 37.97% | Safe |
| Hyderabad | Telangana | 95.99% | Critical |
| Siddipet | Telangana | 69.22% | Safe (near semi-critical threshold) |

---

## How it works in the app

```
Farmer profile (state + district from pincode/registration)
        ↓
GroundWaterAssessmentRepository
        ↓
app/src/main/assets/groundwater/groundwater_districts.json
        ↓
CropRecommendationRequest (+ category, stage %, assessment year)
        ↓
Firebase Cloud Function: getCropRecommendation
        ↓
Google Gemini prompt includes groundwater context
        ↓
Personalized crop cards (risk, water need, fertilizer)
```

If Cloud Function is unavailable, **LocalCropRecommendationAdvisor** uses the same groundwater assessment to bias toward low-water crops in stressed districts.

### Inputs to crop recommendation (after this feature)

1. **Farmer profile** — village, district, state, farm size, soil, water source, current crop  
2. **Season** — Kharif / Rabi / Zaid  
3. **Weather cache** — temperature, humidity, rain  
4. **Mandi prices** — Agmarknet via Cloud Function  
5. **Groundwater** — district category + stage of extraction % *(new)*  
6. **Language** — English / Telugu for Gemini responses  

---

## Project files

| Path | Purpose |
|------|---------|
| `data/groundwater/andhra_pradesh_groundwater.csv` | Source CSV (AP) |
| `data/groundwater/telangana_groundwater.csv` | Source CSV (Telangana) |
| `app/src/main/assets/groundwater/groundwater_districts.json` | Bundled lookup asset (59 records) |
| `scripts/generate_groundwater_asset.py` | Regenerate JSON from CSVs |
| `domain/model/GroundWaterAssessment.kt` | Category enum + district assessment model |
| `domain/repository/GroundWaterAssessmentRepository.kt` | Repository contract |
| `data/local/GroundWaterAssetDataSource.kt` | Loads JSON from assets |
| `data/repository/GroundWaterAssessmentRepositoryImpl.kt` | District lookup + alias matching |
| `core/utils/DistrictNameMatcher.kt` | Normalizes district names for fuzzy match |
| `data/repository/CropRecommendationRepositoryImpl.kt` | Wires groundwater into recommendation flow |
| `functions/index.js` | Gemini prompt includes groundwater fields |

---

## Regenerate asset after CSV update

```bash
# Place updated CSVs in data/groundwater/
python3 scripts/generate_groundwater_asset.py
```

Then rebuild the Android app so the new JSON is bundled in the APK.

---

## Deploy note (Cloud Function)

Groundwater fields are sent from Android to `getCropRecommendation`. Redeploy after prompt changes:

```bash
firebase deploy --only functions:getCropRecommendation --project kisan-alert-99bb3
```

---

## Pitch / demo talking points

1. **Data-driven, not guesswork** — Uses CGWB national compilation data via OpenCity, not generic crop lists.  
2. **Built for AP & Telangana** — Primary hackathon market with 59 districts pre-loaded.  
3. **Works offline** — Groundwater lookup is local; no dependency on broken gov portals at runtime.  
4. **Farmer-safe recommendations** — AI is instructed to avoid high-water crops in stressed districts.  
5. **Extensible** — Same pipeline can add block/mandal data or newer assessment years when available.

### One-liner for judges

> *We combine official CGWB groundwater data from OpenCity with the farmer’s profile, season, and weather so crop AI recommends what the land can actually sustain — not just what grows well on paper.*

---

## Future enhancements (Phase 2)

- [ ] Block/mandal-level categorization (CGWB block-wise PDFs or NWIC GIS)  
- [ ] Store `block` from India Post pincode API in farmer profile  
- [ ] Firestore cache for multi-state expansion beyond AP/TG  
- [ ] Dashboard widget: “Your district groundwater: Safe / Critical”  
- [ ] Telugu labels for groundwater category on crop screen  

---

## Related documentation

- [README.md](README.md) — Developer setup & feature list  
- [APP_OVERVIEW.md](APP_OVERVIEW.md) — Plain-language feature guide  
- [ARCHITECTURE.md](ARCHITECTURE.md) — Code structure  
