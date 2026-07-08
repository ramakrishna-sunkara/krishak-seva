const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {defineSecret} = require("firebase-functions/params");
const admin = require("firebase-admin");
const {GoogleGenerativeAI} = require("@google/generative-ai");

const STORAGE_BUCKET = "kisan-alert-99bb3.firebasestorage.app";

admin.initializeApp({
  storageBucket: STORAGE_BUCKET,
});

const geminiApiKey = defineSecret("GEMINI_API_KEY");
const dataGovApiKey = defineSecret("DATA_GOV_IN_API_KEY");
const GEMINI_MODEL = "gemini-2.5-flash";
const CROP_RECOMMENDATIONS_COLLECTION = "crop_recommendations";
const MANDI_PRICES_COLLECTION = "mandi_prices";
const AGMARKNET_RESOURCE_ID = "35985678-0d79-46b4-9ed6-6f13308a1d24";
const MANDI_CACHE_TTL_MS = 12 * 60 * 60 * 1000;

function resolveLanguageCode(languageCode) {
  return languageCode === "te" ? "te" : "en";
}

function getLanguageName(languageCode) {
  return resolveLanguageCode(languageCode) === "te" ? "Telugu" : "English";
}

function buildLanguageInstruction(languageCode) {
  const languageName = getLanguageName(languageCode);
  return `IMPORTANT: Write ALL user-facing text string values in ${languageName} only` +
    (resolveLanguageCode(languageCode) === "te" ?
      " using Telugu script (తెలుగు)." :
      ".") +
    " Keep JSON keys in English.";
}

function mapAiServiceError(error) {
  const message = (error && error.message) ? error.message : "";
  const normalizedMessage = message.toLowerCase();
  if (
    normalizedMessage.includes("quota_exceeded") ||
    normalizedMessage.includes("too many requests") ||
    normalizedMessage.includes("429") ||
    normalizedMessage.includes("quota") ||
    normalizedMessage.includes("rate limit")
  ) {
    return "QUOTA_EXCEEDED";
  }
  if (
    normalizedMessage.includes("deadline_exceeded") ||
    normalizedMessage.includes("timed out") ||
    normalizedMessage.includes("timeout")
  ) {
    return "TIMEOUT";
  }
  if (
    normalizedMessage.includes("unavailable") ||
    normalizedMessage.includes("network") ||
    normalizedMessage.includes("econnreset")
  ) {
    return "SERVICE_UNAVAILABLE";
  }
  if (normalizedMessage.includes("api key is not configured")) {
    return "NOT_CONFIGURED";
  }
  if (normalizedMessage.includes("image is required")) {
    return "NO_IMAGE";
  }
  return "GENERIC";
}

exports.getCropRecommendation = onCall(
    {secrets: [geminiApiKey, dataGovApiKey]},
    async (request) => {
      if (!request.auth) {
        throw new HttpsError("unauthenticated", "Authentication required.");
      }

      const userId = request.auth.uid;
      const payload = request.data || {};
      const mandiSummary = await fetchMandiPriceSummary(
          payload.state,
          payload.currentCrop,
          dataGovApiKey.value(),
      );
      const prompt = buildCropPrompt(payload, mandiSummary);
      const recommendations = await generateRecommendations(
          prompt,
          geminiApiKey.value(),
          resolveLanguageCode(payload.languageCode),
      );

      await admin.firestore()
          .collection(CROP_RECOMMENDATIONS_COLLECTION)
          .doc(userId)
          .set({
            userId,
            season: payload.season || "KHARIF",
            recommendations,
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
          }, {merge: true});

      return {recommendations};
    }
);

exports.getWeatherAdvisory = onCall(
    {secrets: [geminiApiKey]},
    async (request) => {
      if (!request.auth) {
        throw new HttpsError("unauthenticated", "Authentication required.");
      }

      const userId = request.auth.uid;
      const payload = request.data || {};
      const prompt = buildWeatherAdvisoryPrompt(payload);
      const languageCode = resolveLanguageCode(payload.languageCode);
      const advisory = await generateWeatherAdvisory(
          prompt,
          geminiApiKey.value(),
          languageCode,
      );

      await admin.firestore()
          .collection("weather_advisories")
          .doc(userId)
          .set({
            userId,
            ...advisory,
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
          }, {merge: true});

      return advisory;
    }
);

function buildWeatherAdvisoryPrompt(payload) {
  const languageCode = resolveLanguageCode(payload.languageCode);
  const forecastText = Array.isArray(payload.forecastDays) ?
    payload.forecastDays.map((day) =>
      `${day.dayLabel}: ${day.description}, ${day.temperatureCelsius}C, ` +
      `humidity ${day.humidityPercent}%, rain ${day.rainVolumeMm}mm`
    ).join("\n") :
    "No forecast available";

  return `You are an expert Indian agricultural weather advisor.
${buildLanguageInstruction(languageCode)}
Return ONLY valid JSON (no markdown) in this exact format:
{
  "summary": "one sentence headline for the farmer",
  "irrigationAdvice": "practical irrigation guidance",
  "cropRiskLevel": "Low|Medium|High",
  "tomorrowOutlook": "short outlook for tomorrow",
  "alertTips": ["tip 1", "tip 2", "tip 3"]
}

Farmer details:
- Name: ${payload.farmerName || "Farmer"}
- Location: ${payload.village || ""}, ${payload.district || ""}, ${payload.state || "India"}
- Current crop: ${payload.currentCrop || "Unknown"}
- Water source: ${payload.waterSource || "RAIN_FED"}
- Soil: ${payload.soilType || "LOAMY"}

Current weather:
- Temperature: ${payload.temperatureCelsius ?? "unknown"} C
- Humidity: ${payload.humidityPercent ?? "unknown"} %
- Rain: ${payload.rainVolumeMm ?? "unknown"} mm
- Wind: ${payload.windSpeedKmh ?? "unknown"} km/h
- Conditions: ${payload.weatherDescription || "unknown"}

5-day forecast:
${forecastText}

Give actionable advice for smallholder Indian farmers. Example summary:
"Rain expected tomorrow. Skip irrigation today."`;
}

async function generateWeatherAdvisory(prompt, apiKey, languageCode = "en") {
  if (!apiKey) {
    return buildFallbackWeatherAdvisory(languageCode);
  }

  try {
    const genAI = new GoogleGenerativeAI(apiKey);
    const model = genAI.getGenerativeModel({model: GEMINI_MODEL});
    const result = await model.generateContent(prompt);
    const text = result.response.text();
    const jsonText = text.replace(/```json/g, "").replace(/```/g, "").trim();
    const parsed = JSON.parse(jsonText);
    return {
      summary: parsed.summary || buildFallbackWeatherAdvisory(languageCode).summary,
      irrigationAdvice: parsed.irrigationAdvice || parsed.irrigation_advice ||
        buildFallbackWeatherAdvisory(languageCode).irrigationAdvice,
      cropRiskLevel: parsed.cropRiskLevel || parsed.cropRisk || "Medium",
      tomorrowOutlook: parsed.tomorrowOutlook || parsed.tomorrow_outlook ||
        (resolveLanguageCode(languageCode) === "te" ?
          "స్థానిక వాతావరణ నవీకరణలను తర్వాత తనిఖీ చేయండి." :
          "Monitor local weather updates."),
      alertTips: Array.isArray(parsed.alertTips) ? parsed.alertTips :
        (Array.isArray(parsed.alerts) ? parsed.alerts :
          buildFallbackWeatherAdvisory(languageCode).alertTips),
    };
  } catch (error) {
    console.error("Gemini weather advisory error:", error);
    return buildFallbackWeatherAdvisory(languageCode);
  }
}

function buildFallbackWeatherAdvisory(languageCode = "en") {
  const isTelugu = resolveLanguageCode(languageCode) === "te";
  return {
    summary: isTelugu ?
      "వాతావరణం స్థిరంగా ఉంది. సాధారణ వ్యవసాయ పనులు కొనసాగించండి." :
      "Weather is stable. Continue regular farm activities.",
    irrigationAdvice: isTelugu ?
      "సాయంత్రం నీటి పారుదలకు ముందు నేల తేమను తనిఖీ చేయండి." :
      "Check soil moisture before evening irrigation.",
    cropRiskLevel: isTelugu ? "తక్కువ" : "Low",
    tomorrowOutlook: isTelugu ?
      "నేటి తర్వాత మళ్లీ వాతావరణ అంచనాను చూడండి." :
      "Recheck forecast later today.",
    alertTips: isTelugu ?
      [
        "వర్షం తర్వాత పొలం నీటి నిష్కాసనను పర్యవేక్షించండి.",
        "పంట ఆకులలో పురుగు గుర్తులు ఉన్నాయో చూడండి.",
        "రోజుకు రెండుసార్లు వాతావరణ నవీకరణలు చూడండి.",
      ] :
      [
        "Monitor field drainage after rain.",
        "Inspect crop leaves for pest signs.",
        "Update weather data twice daily.",
      ],
  };
}

function buildCropPrompt(payload, mandiSummary = "") {
  const languageCode = resolveLanguageCode(payload.languageCode);
  const marketContext = mandiSummary.trim().length > 0 ?
    mandiSummary :
    "Not available — use general Indian market knowledge.";
  return `You are an expert Indian agricultural advisor.
${buildLanguageInstruction(languageCode)}
Return ONLY valid JSON (no markdown) in this exact format:
{
  "recommendations": [
    {
      "cropName": "string",
      "reason": "string",
      "riskScore": number between 0-100,
      "waterRequirement": "Low|Medium|High",
      "expectedYield": "string with quintals per acre",
      "fertilizerAdvice": "string"
    }
  ]
}
Provide exactly 3 crop recommendations for an Indian farmer.

Farmer details:
- Name: ${payload.farmerName || "Farmer"}
- Location: ${payload.village || ""}, ${payload.district || ""}, ${payload.state || "India"}
- Farm size: ${payload.farmSizeAcres || 1} acres
- Soil: ${payload.soilType || "LOAMY"}
- Water source: ${payload.waterSource || "RAIN_FED"}
- Current crop: ${payload.currentCrop || "Unknown"}
- Season: ${payload.season || "KHARIF"}
- Temperature: ${payload.temperatureCelsius ?? "unknown"} C
- Humidity: ${payload.humidityPercent ?? "unknown"} %
- Rain: ${payload.rainVolumeMm ?? "unknown"} mm
- Groundwater category: ${payload.groundWaterCategory || "unknown"}
- Groundwater stage of extraction: ${payload.groundWaterStagePercent ?? "unknown"} %
- Groundwater assessment year: ${payload.groundWaterAssessmentYear || "unknown"}

Market price context (Agmarknet mandi data for ${payload.state || "India"}):
${marketContext}

Use the mandi price data above to ground yield and market reasoning where relevant.
If groundwater is Semi-critical, Critical, or Over-exploited, prioritize low-water crops and efficient irrigation.
Focus on practical, low-cost advice for smallholder Indian farmers.`;
}

async function fetchMandiPriceSummary(state, currentCrop, apiKey) {
  if (!apiKey || !state) {
    return "";
  }
  try {
    const today = new Date().toISOString().slice(0, 10);
    const cacheKey = `${state.replace(/\s+/g, "_").toLowerCase()}_${today}`;
    const cacheRef = admin.firestore().collection(MANDI_PRICES_COLLECTION).doc(cacheKey);
    const cacheSnap = await cacheRef.get();
    if (cacheSnap.exists) {
      const cached = cacheSnap.data() || {};
      const fetchedAt = cached.fetchedAt?.toMillis?.() || 0;
      if (Date.now() - fetchedAt < MANDI_CACHE_TTL_MS && cached.summary) {
        return filterMandiSummaryForCrop(cached.summary, currentCrop);
      }
    }
    const records = await fetchAgmarknetRecords(state, today, apiKey);
    const summary = buildMandiSummary(records, state);
    await cacheRef.set({
      state,
      date: today,
      summary,
      recordCount: records.length,
      fetchedAt: admin.firestore.FieldValue.serverTimestamp(),
    }, {merge: true});
    return filterMandiSummaryForCrop(summary, currentCrop);
  } catch (error) {
    console.error("Agmarknet fetch error:", error);
    return "";
  }
}

async function fetchAgmarknetRecords(state, arrivalDate, apiKey) {
  const url = new URL(`https://api.data.gov.in/resource/${AGMARKNET_RESOURCE_ID}`);
  url.searchParams.set("api-key", apiKey);
  url.searchParams.set("format", "json");
  url.searchParams.set("limit", "500");
  url.searchParams.set("filters[State]", state);
  url.searchParams.set("filters[Arrival_Date]", arrivalDate);
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 12000);
  try {
    const response = await fetch(url.toString(), {signal: controller.signal});
    if (!response.ok) {
      throw new Error(`Agmarknet API HTTP ${response.status}`);
    }
    const body = await response.json();
    return Array.isArray(body.records) ? body.records : [];
  } finally {
    clearTimeout(timeout);
  }
}

function buildMandiSummary(records, state) {
  if (!records.length) {
    return `No mandi price records found for ${state} today.`;
  }
  const grouped = {};
  records.forEach((record) => {
    const commodity = record.Commodity || record.commodity || "Unknown";
    const minPrice = parsePrice(record["Min Price"] || record.min_price);
    const maxPrice = parsePrice(record["Max Price"] || record.max_price);
    const modalPrice = parsePrice(record["Modal Price"] || record.modal_price);
    const market = record.Market || record.market || "Market";
    if (!grouped[commodity]) {
      grouped[commodity] = {
        minPrices: [],
        maxPrices: [],
        modalPrices: [],
        markets: new Set(),
      };
    }
    grouped[commodity].minPrices.push(minPrice);
    grouped[commodity].maxPrices.push(maxPrice);
    grouped[commodity].modalPrices.push(modalPrice);
    grouped[commodity].markets.add(market);
  });
  return Object.entries(grouped).map(([commodity, stats]) => {
    const min = Math.min(...stats.minPrices);
    const max = Math.max(...stats.maxPrices);
    const modal = average(stats.modalPrices);
    return `Current mandi prices for ${commodity} in ${state}: ` +
      `min ₹${min}, max ₹${max}, modal ₹${Math.round(modal)} per quintal ` +
      `(${stats.markets.size} markets).`;
  }).join("\n");
}

function filterMandiSummaryForCrop(summary, currentCrop) {
  if (!summary || !currentCrop) {
    return summary || "";
  }
  const cropToken = currentCrop.trim().toLowerCase();
  if (!cropToken) {
    return summary;
  }
  const lines = summary.split("\n").filter((line) => line.trim().length > 0);
  const matched = lines.filter((line) => line.toLowerCase().includes(cropToken));
  if (matched.length > 0) {
    return matched.join("\n");
  }
  const preview = lines.slice(0, 5).join("\n");
  return `${preview}${lines.length > 5 ? "\n(Additional mandi commodities available.)" : ""}`;
}

function parsePrice(value) {
  const parsed = Number(String(value || "0").replace(/[^\d.]/g, ""));
  return Number.isFinite(parsed) ? parsed : 0;
}

function average(values) {
  if (!values.length) {
    return 0;
  }
  return values.reduce((sum, value) => sum + value, 0) / values.length;
}

async function generateRecommendations(prompt, apiKey, languageCode = "en") {
  if (!apiKey) {
    return buildFallbackRecommendations(languageCode);
  }

  try {
    const genAI = new GoogleGenerativeAI(apiKey);
    const model = genAI.getGenerativeModel({model: GEMINI_MODEL});
    const result = await model.generateContent(prompt);
    const text = result.response.text();
    const jsonText = text.replace(/```json/g, "").replace(/```/g, "").trim();
    const parsed = JSON.parse(jsonText);
    if (Array.isArray(parsed.recommendations) && parsed.recommendations.length > 0) {
      return parsed.recommendations.slice(0, 3);
    }
    if (parsed.cropName) {
      return [parsed];
    }
    return buildFallbackRecommendations(languageCode);
  } catch (error) {
    console.error("Gemini error:", error);
    return buildFallbackRecommendations(languageCode);
  }
}

function buildFallbackRecommendations(languageCode = "en") {
  const isTelugu = resolveLanguageCode(languageCode) === "te";
  if (isTelugu) {
    return [
      {
        cropName: "పత్తి",
        reason: "ఖరీఫ్ సీజన్‌లో నల్ల/ఎర్ర న soilsకు మధ్యస్థ నీటి అవసరంతో అనుకూలం.",
        riskScore: 35,
        waterRequirement: "మధ్యస్థ",
        expectedYield: "8-12 quintals/acre",
        fertilizerAdvice: "NPK 120:60:60 kg/haను రెండు విడతల్లో వేయండి.",
      },
      {
        cropName: "వేరుశనగ",
        reason: "తక్కువ నీటి అవసరం, స్థిరమైన మార్కెట్ డిమాండ్‌తో మంచి నూనెగింజ పంట.",
        riskScore: 28,
        waterRequirement: "తక్కువ",
        expectedYield: "10-14 quintals/acre",
        fertilizerAdvice: "పుష్పించే సమయంలో gypsum; basal doseలో phosphorus.",
      },
      {
        cropName: "పప్పుధాన్యాలు",
        reason: "నేలలో nitrogenను పెంచి, వర్షాధార పంటలకు అనుకూలం.",
        riskScore: 22,
        waterRequirement: "తక్కువ",
        expectedYield: "6-10 quintals/acre",
        fertilizerAdvice: "Rhizobium seed treatment; nitrogen తక్కువగా.",
      },
    ];
  }
  return [
    {
      cropName: "Cotton",
      reason: "Suitable for black and red soils during Kharif with moderate water.",
      riskScore: 35,
      waterRequirement: "Medium",
      expectedYield: "8-12 quintals/acre",
      fertilizerAdvice: "Apply NPK 120:60:60 kg/ha in split doses.",
    },
    {
      cropName: "Groundnut",
      reason: "Good oilseed option with lower water need and stable market demand.",
      riskScore: 28,
      waterRequirement: "Low",
      expectedYield: "10-14 quintals/acre",
      fertilizerAdvice: "Use gypsum at flowering; apply phosphorus at basal.",
    },
    {
      cropName: "Pulses",
      reason: "Improves soil nitrogen and fits rain-fed conditions.",
      riskScore: 22,
      waterRequirement: "Low",
      expectedYield: "6-10 quintals/acre",
      fertilizerAdvice: "Seed treatment with Rhizobium; minimal nitrogen needed.",
    },
  ];
}

exports.detectCropDisease = onCall(
    {secrets: [geminiApiKey]},
    async (request) => {
      if (!request.auth) {
        throw new HttpsError("unauthenticated", "Authentication required.");
      }

      const userId = request.auth.uid;
      const payload = request.data || {};
      const scanId = payload.scanId || admin.firestore().collection("_").doc().id;
      try {
        const imageStorageUrl = await uploadCropScanImage(
            userId,
            scanId,
            payload.imageBase64,
            payload.imageMimeType,
        );
        const diagnosis = await detectDiseaseFromImage(payload, geminiApiKey.value());
        await admin.firestore()
            .collection("crop_disease_scans")
            .doc(userId)
            .collection("scans")
            .add({
              userId,
              scanId,
              cropName: payload.cropName || "Unknown",
              imageStorageUrl: imageStorageUrl || "",
              diagnosis,
              createdAt: admin.firestore.FieldValue.serverTimestamp(),
            });
        return {
          ...diagnosis,
          scanId,
          imageStorageUrl: imageStorageUrl || "",
        };
      } catch (error) {
        console.error("detectCropDisease error:", error);
        throw new HttpsError(
            "internal",
            mapAiServiceError(error),
        );
      }
    }
);

exports.uploadCropScanImage = onCall(
    async (request) => {
      if (!request.auth) {
        throw new HttpsError("unauthenticated", "Authentication required.");
      }
      const payload = request.data || {};
      if (!payload.scanId || !payload.imageBase64) {
        throw new HttpsError("invalid-argument", "scanId and imageBase64 are required.");
      }
      const imageStorageUrl = await uploadCropScanImage(
          request.auth.uid,
          payload.scanId,
          payload.imageBase64,
          payload.imageMimeType,
      );
      if (!imageStorageUrl) {
        throw new HttpsError("internal", "IMAGE_UPLOAD_FAILED");
      }
      return {imageStorageUrl};
    },
);

async function uploadCropScanImage(userId, scanId, imageBase64, mimeType) {
  if (!imageBase64 || !scanId || !userId) {
    return null;
  }
  try {
    const bucket = admin.storage().bucket(STORAGE_BUCKET);
    const filePath = `crop_scans/${userId}/${scanId}.jpg`;
    const buffer = Buffer.from(imageBase64, "base64");
    const downloadToken = require("crypto").randomUUID();
    const file = bucket.file(filePath);
    await file.save(buffer, {
      metadata: {
        contentType: mimeType || "image/jpeg",
        metadata: {
          firebaseStorageDownloadTokens: downloadToken,
        },
      },
      resumable: false,
    });
    const encodedPath = encodeURIComponent(filePath);
    const downloadUrl =
      `https://firebasestorage.googleapis.com/v0/b/${bucket.name}/o/` +
      `${encodedPath}?alt=media&token=${downloadToken}`;
    console.log(`uploadCropScanImage success path=${filePath}`);
    return downloadUrl;
  } catch (error) {
    console.error("uploadCropScanImage failed:", error);
    return null;
  }
}

async function detectDiseaseFromImage(payload, apiKey) {
  if (!apiKey) {
    throw new Error("Gemini API key is not configured on the server.");
  }
  if (!payload.imageBase64) {
    throw new Error("Crop image is required for AI diagnosis.");
  }

  const prompt = buildDiseaseDetectionPrompt(payload);

  const genAI = new GoogleGenerativeAI(apiKey);
  const model = genAI.getGenerativeModel({model: GEMINI_MODEL});
  const result = await model.generateContent([
    {
      inlineData: {
        mimeType: payload.imageMimeType || "image/jpeg",
        data: payload.imageBase64,
      },
    },
    {text: prompt},
  ]);
  const text = result.response.text();
  const parsed = extractJsonFromText(text);
  if (!isValidCropImageDiagnosis(parsed)) {
    throw new Error("INVALID_CROP_IMAGE");
  }
  const languageCode = resolveLanguageCode(payload.languageCode);
  const isTelugu = languageCode === "te";
  return {
    isHealthy: parsed.isHealthy ?? false,
    isValidCropImage: true,
    diseaseName: parsed.diseaseName || (isTelugu ? "తెలിയని పరిస్థితి" : "Unknown Condition"),
    confidencePercent: parsed.confidencePercent ?? parsed.confidence ?? 50,
    severity: parsed.severity || "Medium",
    symptoms: parsed.symptoms || (isTelugu ?
      "లక్షణాలు నిర్ణయించలేకపోయాము." :
      "Symptoms could not be determined."),
    treatmentAdvice: parsed.treatmentAdvice || parsed.treatment ||
      (isTelugu ?
        "మీ స్థానిక వ్యవసాయ అధికారిని సంప్రదించండి." :
        "Consult your local agricultural officer."),
    preventionTips: Array.isArray(parsed.preventionTips) ? parsed.preventionTips :
      (Array.isArray(parsed.prevention_tips) ? parsed.prevention_tips :
        [isTelugu ?
          "పంట మార్పులను ప్రతిరోజు పర్యవేక్షించండి." :
          "Monitor the crop daily for changes."]),
    isFromCloud: true,
  };
}

function extractJsonFromText(text) {
  const cleaned = text.replace(/```json/gi, "").replace(/```/g, "").trim();
  const startIndex = cleaned.indexOf("{");
  const endIndex = cleaned.lastIndexOf("}");
  if (startIndex === -1 || endIndex === -1 || endIndex <= startIndex) {
    throw new Error("No JSON object found in model response.");
  }
  return JSON.parse(cleaned.substring(startIndex, endIndex + 1));
}

function buildDiseaseDetectionPrompt(payload) {
  const languageCode = resolveLanguageCode(payload.languageCode);
  const languageName = getLanguageName(languageCode);
  return `You are an expert Indian crop pathologist helping smallholder farmers.
${buildLanguageInstruction(languageCode)}
All user-facing fields (diseaseName, symptoms, treatmentAdvice, and every preventionTips item) MUST be written in ${languageName} only. Do NOT use English when ${languageName} is requested.
Carefully analyze the ACTUAL crop leaf/plant image provided. Different images must produce different diagnoses when symptoms differ.
Return ONLY valid JSON (no markdown, no extra text):
{
  "isValidCropImage": boolean,
  "isHealthy": boolean,
  "diseaseName": "string in ${languageName} or healthy label in ${languageName}",
  "confidencePercent": number between 0-100,
  "severity": "Low|Medium|High",
  "symptoms": "short symptom description in ${languageName} based on what you see in THIS image",
  "treatmentAdvice": "practical low-cost treatment in ${languageName} for Indian farmers",
  "preventionTips": ["tip 1 in ${languageName}", "tip 2 in ${languageName}", "tip 3 in ${languageName}"]
}

Farmer context:
- Crop: ${payload.cropName || "Unknown"}
- Location: ${payload.village || ""}, ${payload.district || ""}, ${payload.state || "India"}
- Farmer: ${payload.farmerName || "Farmer"}

IMPORTANT:
- Set isValidCropImage to false if the image is NOT a clear photo of crop leaves/plants (wrong object, person, device, soil only, too blurry, too dark, or no visible plant).
- When isValidCropImage is false, do NOT set isHealthy to true. Use diseaseName "Invalid Image" and confidencePercent 0.
- Only set isHealthy true when isValidCropImage is true AND the visible crop appears disease-free.
- Focus on common Indian crop diseases when the image is valid.`;
}

function isValidCropImageDiagnosis(parsed) {
  if (parsed.isValidCropImage === false) {
    return false;
  }
  const combined = [
    parsed.diseaseName || "",
    parsed.symptoms || "",
    parsed.treatmentAdvice || "",
    ...(Array.isArray(parsed.preventionTips) ? parsed.preventionTips : []),
  ].join(" ").toLowerCase();
  const strongMarkers = [
    "irrelevant objects",
    "clearly show the crop",
    "photographing plants",
    "not a crop",
    "no plant visible",
    "invalid image",
    "unable to analyze",
    "cannot analyze",
  ];
  if (strongMarkers.some((marker) => combined.includes(marker))) {
    return false;
  }
  if (parsed.isHealthy === true) {
    const photoTipCount = (parsed.preventionTips || []).filter((tip) => {
      const normalized = String(tip).toLowerCase();
      return normalized.includes("lighting") ||
        normalized.includes("blurr") ||
        normalized.includes("clearly show") ||
        normalized.includes("photograph");
    }).length;
    if (photoTipCount >= 2) {
      return false;
    }
  }
  return true;
}

function buildFallbackDiseaseDiagnosis(cropName) {
  return {
    isHealthy: false,
    diseaseName: "Possible Leaf Stress",
    confidencePercent: 55,
    severity: "Medium",
    symptoms: `Uneven color or minor spots may be present on ${cropName || "crop"} leaves.`,
    treatmentAdvice: "Remove affected leaves and apply neem oil spray (5 ml per liter) in the evening.",
    preventionTips: [
      "Inspect fields twice a week during humid weather.",
      "Maintain proper spacing for airflow.",
      "Consult local Krishi Vigyan Kendra if symptoms spread.",
    ],
  };
}

exports.askFarmingQuestion = onCall(
    {secrets: [geminiApiKey]},
    async (request) => {
      if (!request.auth) {
        throw new HttpsError("unauthenticated", "Authentication required.");
      }

      const userId = request.auth.uid;
      const payload = request.data || {};
      const response = await generateFarmingAnswer(payload, geminiApiKey.value());

      await admin.firestore()
          .collection("voice_conversations")
          .doc(userId)
          .collection("messages")
          .add({
            userId,
            question: payload.question || "",
            answer: response.answer,
            languageCode: resolveLanguageCode(payload.languageCode),
            followUpSuggestions: response.followUpSuggestions,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
          });

      return response;
    }
);

async function generateFarmingAnswer(payload, apiKey) {
  if (!apiKey) {
    return buildFallbackFarmingAnswer(payload);
  }

  const languageCode = resolveLanguageCode(payload.languageCode);
  const languageName = getLanguageName(languageCode);
  const prompt = `You are कृषकसेवा (Krishak Seva), a helpful AI farming assistant for Indian smallholder farmers.
${buildLanguageInstruction(languageCode)}
Return ONLY valid JSON (no markdown):
{
  "answer": "clear practical farming advice in 3-5 sentences in ${languageName}",
  "followUpSuggestions": ["question 1", "question 2", "question 3"]
}

Farmer context:
- Name: ${payload.farmerName || "Farmer"}
- Location: ${payload.village || ""}, ${payload.district || ""}, ${payload.state || "India"}
- Crop: ${payload.currentCrop || "Unknown"}
- Soil: ${payload.soilType || "LOAMY"}
- Water source: ${payload.waterSource || "RAIN_FED"}

Farmer question:
${payload.question || "General farming advice"}

Give low-cost, practical advice suitable for Indian farmers.`;

  try {
    const genAI = new GoogleGenerativeAI(apiKey);
    const model = genAI.getGenerativeModel({model: GEMINI_MODEL});
    const result = await model.generateContent(prompt);
    const text = result.response.text();
    const jsonText = text.replace(/```json/g, "").replace(/```/g, "").trim();
    const parsed = JSON.parse(jsonText);
    return {
      answer: parsed.answer || buildFallbackFarmingAnswer(payload).answer,
      followUpSuggestions: Array.isArray(parsed.followUpSuggestions) ?
        parsed.followUpSuggestions :
        buildFallbackFarmingAnswer(payload).followUpSuggestions,
    };
  } catch (error) {
    console.error("Gemini farming question error:", error);
    return buildFallbackFarmingAnswer(payload);
  }
}

function buildFallbackFarmingAnswer(payload) {
  const cropName = payload.currentCrop || "your crop";
  const isTelugu = resolveLanguageCode(payload.languageCode) === "te";
  return {
    answer: isTelugu ?
      `${cropName} పంటకు స్థానిక వ్యవసాయ అధికారిని సంప్రదించండి. నేల తేమ మరియు పంట ఆరోగ్యాన్ని వారానికి రెండుసార్లు పర్యవేక్షించండి.` :
      `For ${cropName}, monitor soil moisture and crop health twice a week. Consult your local agricultural officer for field-specific advice.`,
    followUpSuggestions: isTelugu ?
      [
        `${cropName} కోసం నీటి పారుదల ఎప్పుడు చేయాలి?`,
        "ఎరువు ఎప్పుడు వేయాలి?",
        "పురుగులను ఎలా నియంత్రించాలి?",
      ] :
      [
        `When should I irrigate ${cropName}?`,
        "What fertilizer should I use?",
        "How do I control pests?",
      ],
  };
}

const adminMessaging = admin.messaging();
const FCM_TOKENS_COLLECTION = "fcm_tokens";

exports.sendFarmAlert = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Authentication required.");
  }

  const userId = request.auth.uid;
  const payload = request.data || {};
  const title = payload.title || "कृषकसेवा";
  const message = payload.message || "You have a new farm advisory.";
  const alertType = payload.type || "WEATHER";

  const tokenDoc = await admin.firestore()
      .collection(FCM_TOKENS_COLLECTION)
      .doc(userId)
      .get();
  const token = tokenDoc.data()?.token;
  if (!token) {
    throw new HttpsError("not-found", "FCM token not found for user.");
  }

  await adminMessaging.send({
    token,
    notification: {title, body: message},
    data: {
      userId,
      title,
      message,
      type: alertType,
    },
    android: {
      priority: "high",
      notification: {
        channelId: "farm_alerts",
      },
    },
  });

  return {success: true};
});
