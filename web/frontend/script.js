// Base Backend API configuration dynamically loaded
const BACKEND_URL = window.location.origin;

// Global State
let currentLang = "en";
let isDarkMode = false;
let registeredFarmer = null;
let chatHistory = [];
let detectedLat = null;
let detectedLon = null;

// Chart Instances
let marketTrendChartInstance = null;
let profitTrendChartInstance = null;
let rainHistoryChartInstance = null;

// ---------- BILINGUAL TRANSLATION DICTIONARIES ----------
const TRANSLATIONS = {
  en: {
    app_subtitle: "हे कृषक, सुखी भव।",
    nav_home: "Home",
    nav_dashboard: "Dashboard",
    nav_advisor: "Crop Advisor",
    nav_market: "Mandi Market",
    nav_doctor: "Crop Doctor",
    nav_ivr: "IVR Helpline",
    nav_extension: "Services",
    nav_analytics: "Analytics",
    btn_sos: "SOS Help",
    hero_title: "हे कृषक, सुखी भव।",
    hero_desc: "Optimize yields, diagnose diseases in under 2 seconds, consult bilingual voice assistants, and get real-time mandi weather predictions customized for your fields.",
    btn_register: "Get Registered Profile",
    btn_try_demo: "Try Web Demo",
    feat_voice_title: "Voice AI Assistant",
    feat_voice_desc: "Speak naturally in English or Telugu to receive crop advisory solutions.",
    feat_weather_title: "Climate Warnings",
    feat_weather_desc: "Receive dry-spell hazards, rain forecasts, and cyclone warnings.",
    feat_doctor_title: "Doctor Crop Diagnosis",
    feat_doctor_desc: "Upload leaf photos to identify infections and retrieve organic therapies.",
    reg_header: "Farmer Registration Portal",
    reg_hint: "Setup your profile to customize weather warnings, mandi price alerts, and crop recommendation scores.",
    form_name: "Full Name",
    form_phone: "Mobile Number",
    form_age: "Age",
    form_gender: "Gender",
    form_state: "State",
    form_district: "District",
    form_mandal: "Mandal",
    form_village: "Village",
    form_pin: "PIN Code",
    form_acres: "Land Size (Acres)",
    form_soil: "Soil Type",
    form_water: "Water Source",
    form_crop: "Current Crop",
    btn_save_profile: "Create Profile & Enter Dashboard",
    farm_health_title: "AI Farm Health Score",
    dash_weather: "Weather Forecast",
    dash_crop_status: "Crop Health Status",
    dash_alerts: "Personalized Climate Alerts",
    dash_mandi: "Live Market Mandi Prices",
    result_title: "Advisory Diagnosis Report",
    extraction_title: "Live AI Parameter Extractor",
    extraction_hint: "As the farmer presses keys, the IVR state machine automatically updates your profile variables in the database:",
    transcript_log: "Call Dialogue Log"
  },
  te: {
    app_subtitle: "ఓ రైతు, వర్ధిల్లు!",
    nav_home: "హోమ్",
    nav_dashboard: "డ్యాష్‌బోర్డ్",
    nav_advisor: "పంట సలహాదారు",
    nav_market: "మండి మార్కెట్",
    nav_doctor: "పంట వైద్యుడు",
    nav_ivr: "హెల్ప్‌లైన్",
    nav_extension: "సేవలు",
    nav_analytics: "విశ్లేషణలు",
    btn_sos: "సహాయం",
    hero_title: "ఓ రైతు, వర్ధిల్లు!",
    hero_desc: "పంట దిగుబడిని పెంచుకోండి, 2 సెకన్లలో వ్యాధి నిర్ధారణ చేయండి, ద్విభాషా వాయిస్ సహాయకులతో మాట్లాడండి మరియు రియల్ టైమ్ మండి ధరలను పొందండి.",
    btn_register: "నమోదు చేసుకోండి",
    btn_try_demo: "వెబ్ డెమో",
    feat_voice_title: "వాయిస్ సహాయకురాలు",
    feat_voice_desc: "ఇంగ్లీష్ లేదా తెలుగులో సులభంగా మాట్లాడి పంట సలహాలు పొందండి.",
    feat_weather_title: "వాతావరణ హెచ్చరికలు",
    feat_weather_desc: "వర్షాలు, తుఫానులు మరియు వాతావరణ మార్పుల హెచ్చరికలు పొందండి.",
    feat_doctor_title: "పంట వ్యాధి నిర్ధారణ",
    feat_doctor_desc: "పంట ఆకు ఫోటోను అప్‌లోడ్ చేసి క్షణాల్లో నివారణ చర్యలు తెలుసుకోండి.",
    reg_header: "రైతు ప్రొఫైల్ నమోదు",
    reg_hint: "మీ ప్రొఫైల్ నమోదు చేసుకోవడం ద్వారా వాతావరణం మరియు మండి ధరలను ఎప్పటికప్పుడు తెలుసుకోండి.",
    form_name: "పూర్తి పేరు",
    form_phone: "మొబైల్ సంఖ్య",
    form_age: "వయస్సు",
    form_gender: "లింగం",
    form_state: "రాష్ట్రం",
    form_district: "జిల్లా",
    form_mandal: "మండలం",
    form_village: "గ్రామం",
    form_pin: "పిన్ కోడ్",
    form_acres: "భూమి పరిమాణం (ఎకరాలు)",
    form_soil: "నేల రకం",
    form_water: "నీటి వనరు",
    form_crop: "ప్రస్తుత పంట",
    btn_save_profile: "ప్రొఫైల్ సృష్టించండి",
    farm_health_title: "AI పంట ఆరోగ్య స్కోరు",
    dash_weather: "వాతావరణ సూచన",
    dash_crop_status: "పంట ఆరోగ్య పరిస్థితి",
    dash_alerts: "వ్యక్తిగత హెచ్చరికలు",
    dash_mandi: "మండి మార్కెట్ ధరలు",
    result_title: "పంట చికిత్స నివేదిక",
    extraction_title: "లైవ్ AI పారామీటర్ ఎక్స్‌ట్రాక్టర్",
    extraction_hint: "రైతు కీప్యాడ్ నొక్కుతుంటే, IVR సిస్టమ్ ఆటోమేటిక్‌గా ప్రొఫైల్ వివరాలను అప్‌డేట్ చేస్తుంది:",
    transcript_log: "కాల్ సంభాషణ వివరాలు"
  }
};

// ---------- PAGE NAVIGATION & TAB SYSTEM ----------
document.querySelectorAll(".nav-link").forEach(link => {
  link.addEventListener("click", (e) => {
    e.preventDefault();
    const targetTab = link.dataset.tab;
    if (!registeredFarmer && targetTab !== "landing" && targetTab !== "ivr") {
      alert("Please login or register using the secure OTP portal first.");
      document.getElementById("register-section").scrollIntoView({ behavior: "smooth" });
      return;
    }
    switchTab(targetTab);
  });
});

function switchTab(tabId) {
  document.querySelectorAll(".nav-link").forEach(lnk => {
    lnk.classList.toggle("active", lnk.dataset.tab === tabId);
  });

  document.querySelectorAll(".tab-panel").forEach(panel => {
    panel.classList.toggle("active", panel.id === `panel-${tabId}`);
  });

  if (tabId === "market") {
    setTimeout(() => {
      const lat = registeredFarmer ? registeredFarmer.latitude : (detectedLat || 14.4426);
      const lon = registeredFarmer ? registeredFarmer.longitude : (detectedLon || 79.9865);
      loadMandiMarketData(registeredFarmer ? registeredFarmer.crop_type : "Rice", lat, lon);
    }, 100);
  } else if (tabId === "analytics") {
    setTimeout(toggleAnalyticsState, 100);
  }
}

document.getElementById("heroRegisterBtn").addEventListener("click", (e) => {
  e.preventDefault();
  document.getElementById("register-section").scrollIntoView({ behavior: "smooth" });
});

document.getElementById("heroDemoBtn").addEventListener("click", (e) => {
  e.preventDefault();
  switchTab("ivr");
});

// ---------- BILINGUAL TRANSLATION STATE ----------
const langToggleBtn = document.getElementById("langToggleBtn");
langToggleBtn.addEventListener("click", () => {
  currentLang = currentLang === "en" ? "te" : "en";
  updateLanguageUI();
  if (registeredFarmer) {
    updateDashboardWithProfile(registeredFarmer);
  } else {
    const lat = detectedLat || 14.4426;
    const lon = detectedLon || 79.9865;
    fetchWeatherForCoordinates(lat, lon);
  }
});

function updateLanguageUI() {
  const trans = TRANSLATIONS[currentLang];
  
  document.querySelectorAll("[data-translate-key]").forEach(elem => {
    const key = elem.getAttribute("data-translate-key");
    if (trans[key]) {
      const textSpan = elem.querySelector("span");
      if (textSpan) {
        textSpan.textContent = trans[key];
      } else {
        elem.textContent = trans[key];
      }
    }
  });

  document.querySelectorAll(".nav-menu a").forEach(link => {
    const tabName = link.dataset.tab;
    const key = `nav_${tabName}`;
    const span = link.querySelector("span");
    if (span && trans[key]) {
      span.textContent = trans[key];
    }
  });

  const forecastHeading = document.getElementById("weather-forecast-title-heading");
  if (forecastHeading) {
    forecastHeading.textContent = currentLang === "en" ? "7-Day Local Forecast" : "7-రోజుల స్థానిక వాతావరణ సూచన";
  }

  const langTextSpan = document.querySelector("#langToggleBtn span");
  if (langTextSpan) {
    langTextSpan.textContent = currentLang === "en" ? "తెలుగు (Telugu)" : "English";
  }

  const subTitleSpan = document.querySelector(".logo-text span");
  if (subTitleSpan) {
    subTitleSpan.textContent = trans["app_subtitle"];
  }
}

// ---------- LIGHT & SLATE DARK THEME ----------
const themeToggleBtn = document.getElementById("themeToggleBtn");
themeToggleBtn.addEventListener("click", () => {
  isDarkMode = !isDarkMode;
  document.body.classList.toggle("dark-theme", isDarkMode);
  themeToggleBtn.innerHTML = isDarkMode 
    ? `<i class="fa-solid fa-sun"></i>` 
    : `<i class="fa-solid fa-moon"></i>`;
});

// ---------- GPS DETECTION & REVERSE GEOCODING SERVICE ----------
async function reverseGeocode(lat, lon) {
  try {
    const res = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json&accept-language=en`, {
      headers: { "User-Agent": "KrishakaSevaPlatform/1.0" }
    });
    if (!res.ok) throw new Error("OSM Nominatim API error");
    const data = await res.json();
    if (data && data.address) {
      const addr = data.address;
      return {
        state: addr.state || "Andhra Pradesh",
        district: addr.county || addr.district || addr.state_district || "Nellore",
        mandal: addr.suburb || addr.city_district || addr.municipality || "Nellore East",
        village: addr.village || addr.town || addr.city || addr.suburb || "Maddipadu",
        pin: addr.postcode || "524201"
      };
    }
  } catch (e) {
    console.error("OSM Nominatim Geocoding failed:", e);
  }
  return null;
}

document.getElementById("detectGpsBtn").addEventListener("click", () => {
  if (navigator.geolocation) {
    alert("Requesting browser GPS coordinate permissions...");
    navigator.geolocation.getCurrentPosition(async (position) => {
      detectedLat = position.coords.latitude;
      detectedLon = position.coords.longitude;
      
      const geoResult = await reverseGeocode(detectedLat, detectedLon);
      
      if (geoResult) {
        document.getElementById("reg-state").value = geoResult.state;
        document.getElementById("reg-district").value = geoResult.district;
        document.getElementById("reg-mandal").value = geoResult.mandal;
        document.getElementById("reg-village").value = geoResult.village;
        document.getElementById("reg-pin").value = geoResult.pin;
        alert(`GPS coordinates successfully parsed! Set location: ${geoResult.village}, ${geoResult.district}, ${geoResult.state}.`);
      } else {
        document.getElementById("reg-state").value = "Andhra Pradesh";
        document.getElementById("reg-district").value = "Nellore";
        document.getElementById("reg-mandal").value = "Nellore East";
        document.getElementById("reg-village").value = "Maddipadu";
        document.getElementById("reg-pin").value = "524201";
        alert("GPS coordinates locked, but geocoder timed out. Applied standard default parameters.");
      }
      
      // Update weather immediately using coordinates
      fetchWeatherForCoordinates(detectedLat, detectedLon);
      
      if (registeredFarmer) {
        registeredFarmer.latitude = detectedLat;
        registeredFarmer.longitude = detectedLon;
        if (geoResult) {
          registeredFarmer.location = `${geoResult.village}, ${geoResult.district}, ${geoResult.state}`;
        }
        updateDashboardWithProfile(registeredFarmer);
      }
    }, error => {
      alert("GPS location permission denied or timed out. Defaulting to Nellore coordinates.");
      detectedLat = 14.4426;
      detectedLon = 79.9865;
      document.getElementById("reg-state").value = "Andhra Pradesh";
      document.getElementById("reg-district").value = "Nellore";
      document.getElementById("reg-mandal").value = "Nellore East";
      document.getElementById("reg-village").value = "Maddipadu";
      document.getElementById("reg-pin").value = "524201";
      fetchWeatherForCoordinates(detectedLat, detectedLon);
    });
  }
});

// ---------- SECURE PHONE AUTHENTICATION (OTP) ----------
let authPhoneNum = "";

document.getElementById("sendOtpBtn").addEventListener("click", async () => {
  const phone = document.getElementById("auth-phone-input").value.trim();
  if (!phone) {
    alert("Please enter a valid mobile number.");
    return;
  }
  
  try {
    const res = await fetch(`${BACKEND_URL}/api/auth/send-otp`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ phone: phone })
    });
    
    if (res.ok) {
      const data = await res.json();
      authPhoneNum = phone;
      document.getElementById("auth-step-phone").style.display = "none";
      document.getElementById("auth-step-otp").style.display = "block";
      alert(`Secure OTP verification code sent to ${phone}!
(Demo Fallback OTP: ${data.otp_demo})`);
    } else {
      alert("Error sending OTP code. Please check parameters.");
    }
  } catch (err) {
    alert("Authentication service connection failed: " + err.message);
  }
});

document.getElementById("resendOtpBtn").addEventListener("click", () => {
  document.getElementById("auth-step-phone").style.display = "block";
  document.getElementById("auth-step-otp").style.display = "none";
});

document.getElementById("verifyOtpBtn").addEventListener("click", async () => {
  const otp = document.getElementById("auth-otp-input").value.trim();
  if (!otp || otp.length !== 6) {
    alert("Please enter a valid 6-digit OTP code.");
    return;
  }
  
  try {
    const res = await fetch(`${BACKEND_URL}/api/auth/verify-otp`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ phone: authPhoneNum, otp: otp })
    });
    
    if (res.ok) {
      const data = await res.json();
      if (data.new_user) {
        alert("OTP Verified successfully! You are a new farmer, please fill in your registration details.");
        document.getElementById("auth-portal-box").style.display = "none";
        
        const regForm = document.getElementById("farmerRegistrationForm");
        regForm.style.display = "block";
        
        const regPhone = document.getElementById("reg-phone");
        regPhone.value = authPhoneNum;
        regPhone.readOnly = true;
      } else {
        alert("OTP Verified! Logged in successfully.");
        document.getElementById("auth-portal-box").style.display = "none";
        registeredFarmer = data.profile;
        localStorage.setItem("krushakseva_phone", data.profile.phone);
        updateDashboardWithProfile(data.profile);
        switchTab("dashboard");
      }
    } else {
      const data = await res.json();
      alert("Verification Failed: " + (data.error || "Invalid OTP code."));
    }
  } catch (err) {
    alert("Verification connection failed: " + err.message);
  }
});


// ---------- FARMER REGISTRATION & FIRESTORE PROFILE ----------
const registrationForm = document.getElementById("farmerRegistrationForm");
registrationForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  
  const phone = document.getElementById("reg-phone").value.trim();
  const name = document.getElementById("reg-name").value;
  const location = `${document.getElementById("reg-village").value}, ${document.getElementById("reg-district").value}, ${document.getElementById("reg-state").value}`;
  const land_acres = parseFloat(document.getElementById("reg-acres").value);
  const crop = document.getElementById("reg-crop").value;
  const soil = document.getElementById("reg-soil").value;
  const water = document.getElementById("reg-water").value;
  const waterAvail = document.getElementById("reg-water-availability").value;
  const soilPhVal = parseFloat(document.getElementById("reg-soil-ph").value) || 0.0;
  
  const payload = {
    phone: phone,
    name: name,
    location: location,
    land_size_acres: land_acres,
    crop_type: crop,
    soil_type: soil,
    irrigation_method: water,
    water_availability: waterAvail,
    soil_ph: soilPhVal,
    latitude: detectedLat || 14.4426,
    longitude: detectedLon || 79.9865
  };

  try {
    const res = await fetch(`${BACKEND_URL}/api/farmer-profile`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    
    if (res.ok) {
      const profile = await res.json();
      registeredFarmer = profile;
      localStorage.setItem("krushakseva_phone", profile.phone);
      updateDashboardWithProfile(profile);
      alert("Farmer profile saved successfully! Redirecting to Dashboard.");
      switchTab("dashboard");
    } else {
      alert("Error saving profile details to backend.");
    }
  } catch (err) {
    alert("Connection error when connecting to Flask profile DB: " + err.message);
  }
});

function updateDashboardWithProfile(profile) {
  document.getElementById("dash-greeting").textContent = `Welcome back, ${profile.name}!`;
  document.getElementById("dash-sub").textContent = `Location registered: ${profile.location}`;
  document.getElementById("dash-crop-name").textContent = profile.crop_type || "Rice";
  
  let lat = profile.latitude || 14.4426;
  let lon = profile.longitude || 79.9865;
  
  document.getElementById("badge-name").textContent = profile.name || "Farmer";
  document.getElementById("badge-phone").textContent = profile.phone;
  
  // Fetch real coordinates weather metrics
  fetchWeatherForCoordinates(lat, lon);
  
  // Fetch soil and weather parameters crop suitability recommendations
  loadCropRecommendations(lat, lon, profile.soil_type, profile.water_availability, profile.irrigation_method, profile.soil_ph);
  
  // Fetch location aware mandi rates
  loadMandiMarketData(profile.crop_type || "Rice", lat, lon);
  
  // Toggle analytics view
  toggleAnalyticsState();
}

// ---------- API LIVE FETCH WEATHER SERVICES ----------
async function fetchWeatherForCoordinates(lat, lon) {
  try {
    const res = await fetch(`${BACKEND_URL}/api/weather-alert?lat=${lat}&lon=${lon}&lang=${currentLang}`);
    const data = await res.json();
    if (res.ok && data.weather) {
      // 1. Current Weather Details Card Update
      document.getElementById("dash-temp").textContent = Math.round(data.weather.temp_c);
      document.getElementById("dash-weather-condition").textContent = data.weather.condition;
      
      const iconContainer = document.getElementById("weather-icon-main");
      if (iconContainer) {
        iconContainer.innerHTML = `<i class="fa-solid ${data.weather.icon}"></i>`;
      }
      
      document.getElementById("dash-feels").textContent = `${Math.round(data.weather.feels_like)}°C`;
      document.getElementById("dash-humidity").textContent = `${data.weather.humidity}%`;
      document.getElementById("dash-wind").textContent = `${data.weather.wind_speed} km/h`;
      document.getElementById("dash-wind-dir").textContent = `${data.weather.wind_direction}°`;
      document.getElementById("dash-uv").textContent = data.weather.uv_index;
      document.getElementById("dash-visibility").textContent = `${(data.weather.visibility / 1000).toFixed(1)} km`;
      document.getElementById("dash-pressure").textContent = `${data.weather.pressure} hPa`;
      document.getElementById("dash-rain-prob").textContent = `${data.weather.rain_prob}%`;
      document.getElementById("dash-sunrise").textContent = data.weather.sunrise;
      document.getElementById("dash-sunset").textContent = data.weather.sunset;
      
      // 2. Render 7-Day forecast
      const forecastContainer = document.getElementById("weather-forecast-list");
      if (forecastContainer && data.forecast) {
        forecastContainer.innerHTML = "";
        data.forecast.forEach(day => {
          const dateObj = new Date(day.date);
          // Correct weekday mapping using getDay()
          const dayNamesEn = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
          const dayNamesTe = ["ఆధి", "సోమ", "మంగళ", "బుధ", "గురు", "శుక్ర", "శని"];
          const dayName = currentLang === "te" ? dayNamesTe[dateObj.getDay()] : dayNamesEn[dateObj.getDay()];
          
          const dayCard = document.createElement("div");
          dayCard.className = "forecast-day-card";
          dayCard.style.cssText = "display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 8px; background: var(--bg-card); border-radius: 8px; border: 1px solid var(--border-color);";
          dayCard.innerHTML = `
            <span class="day" style="font-weight:600; font-size:12px; color: var(--text-main);">${dayName}</span>
            <div class="icon" style="font-size:18px; color: var(--primary); margin: 4px 0;"><i class="fa-solid ${day.icon}"></i></div>
            <span class="temp" style="font-size:11px; font-weight:500; color: var(--text-main);">${Math.round(day.temp_max_c)} / ${Math.round(day.temp_min_c)}</span>
            <span style="font-size:9px; color: var(--text-muted);">Rain: ${day.rain_prob}%</span>
          `;
          forecastContainer.appendChild(dayCard);
        });
      }
      
      // 3. Update Notifications alerts box
      const alertBox = document.getElementById("dash-alerts-box");
      if (alertBox) {
        alertBox.innerHTML = "";
        
        // Render severe weather warning alerts
        if (data.alerts && data.alerts.length > 0) {
          data.alerts.forEach(alert => {
            const li = document.createElement("li");
            li.className = "alert-item high-alert";
            li.innerHTML = `
              <i class="fa-solid fa-circle-exclamation"></i>
              <div class="alert-details">
                <strong>${alert.message}</strong>
                <span>${data.suggestions[0] || "Take appropriate field countermeasures immediately."}</span>
              </div>
            `;
            alertBox.appendChild(li);
          });
          
          // Trigger automated Twilio alerts to user's phone
          if (registeredFarmer) {
            triggerTwilioSevereAlerts(registeredFarmer.phone);
          }
        }
        
        // Render agricultural suggestions
        if (data.suggestions && data.suggestions.length > 0) {
          data.suggestions.forEach(sug => {
            const li = document.createElement("li");
            li.className = "alert-item info-alert";
            li.innerHTML = `
              <i class="fa-solid fa-circle-info"></i>
              <div class="alert-details">
                <strong>AI Farming Suggestion</strong>
                <span>${sug}</span>
              </div>
            `;
            alertBox.appendChild(li);
          });
        }
      }
    }
  } catch (err) {
    console.log("Could not load weather details:", err);
  }
}

async function triggerTwilioSevereAlerts(phone) {
  try {
    const res = await fetch(`${BACKEND_URL}/api/trigger-alerts`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ phone: phone })
    });
    const result = await res.json();
    if (result.status === "dispatched") {
      console.log("Outbound Twilio severe weather alerts triggered successfully:", result);
    }
  } catch (err) {
    console.log("Error dispatching outbound calls/texts:", err);
  }
}

// ---------- DYNAMIC COMPARATIVE CROP ADVISOR ----------
async function loadCropRecommendations(lat, lon, soil = "alluvial", waterAvail = "Medium", irrigation = "borewell", ph = 0.0) {
  try {
    const res = await fetch(`${BACKEND_URL}/api/crop-recommendation?lat=${lat}&lon=${lon}&soil_type=${soil}&water_availability=${waterAvail}&irrigation_source=${irrigation}&soil_ph=${ph}&lang=${currentLang}`);
    const data = await res.json();
    if (res.ok && data.recommended_crops) {
      // Clean up fallback grids on the dashboard if present
      console.log("Dynamic advisor crop suitability list loaded:", data.recommended_crops);
    }
  } catch (err) {
    console.log("Could not load suitability rankings:", err);
  }
}

const compareCropsBtn = document.getElementById("compareCropsBtn");
if (compareCropsBtn) {
  compareCropsBtn.addEventListener("click", async () => {
    const checkedBoxes = document.querySelectorAll("input[name='crop_compare_pref']:checked");
    const selectedCrops = Array.from(checkedBoxes).map(cb => cb.value);
    
    if (selectedCrops.length < 3 || selectedCrops.length > 5) {
      alert("Please select between 3 and 5 crops to compare.");
      return;
    }
    
    const loading = document.getElementById("compareCropsLoading");
    const resultBox = document.getElementById("advisorComparisonResultBox");
    const tbody = document.getElementById("cropComparisonTableBody");
    
    if (loading) loading.style.display = "block";
    if (resultBox) resultBox.style.display = "block";
    tbody.innerHTML = "";
    
    try {
      const lat = registeredFarmer ? registeredFarmer.latitude : (detectedLat || 14.4426);
      const lon = registeredFarmer ? registeredFarmer.longitude : (detectedLon || 79.9865);
      
      const res = await fetch(`${BACKEND_URL}/api/crop-recommendation/detailed`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          location: registeredFarmer ? registeredFarmer.location : "Andhra Pradesh",
          land_size: registeredFarmer ? registeredFarmer.land_size_acres : 5.0,
          soil_type: registeredFarmer ? registeredFarmer.soil_type : "Black",
          water_resources: registeredFarmer ? registeredFarmer.irrigation_method : "borewell",
          language: currentLang,
          lat: lat,
          lon: lon,
          selected_crops: selectedCrops
        })
      });
      
      const data = await res.json();
      if (loading) loading.style.display = "none";
      
      if (res.ok && data.comparison) {
        tbody.innerHTML = "";
        data.comparison.forEach(item => {
          const tr = document.createElement("tr");
          tr.innerHTML = `
            <td><strong>${item.crop}</strong></td>
            <td>${Math.round((item.suitability_score || 8) * 10)}%</td>
            <td>${item.soil_compatibility || "—"}</td>
            <td>${item.water_requirement || "—"}</td>
            <td>${item.climate_suitability || "—"}</td>
            <td>${item.expected_investment || item.investment || "—"}</td>
            <td>${item.expected_yield || "—"}</td>
            <td>${item.expected_revenue || item.revenue || "—"}</td>
            <td class="net-profit-val">${item.expected_profit || item.profit || "—"}</td>
            <td>${item.disease_risk || item.risk || "—"}</td>
            <td>${item.local_demand || "—"}</td>
            <td>${item.mandi_price || "—"}</td>
          `;
          tbody.appendChild(tr);
        });
        
        document.getElementById("bestCropNameText").textContent = data.best_crop || selectedCrops[0];
        document.getElementById("bestCropExplanationText").textContent = data.explanation || "";
        document.getElementById("bestCropRecommendationBanner").style.display = "flex";
      }
    } catch (err) {
      if (loading) loading.style.display = "none";
      console.error("Comparison load failed:", err);
    }
  });
}

async function loadMandiMarketData(crop = "Rice", lat = 14.4426, lon = 79.9865) {
  try {
    const res = await fetch(`${BACKEND_URL}/api/market-price?crop=${crop}&lat=${lat}&lon=${lon}`);
    const data = await res.json();
    if (res.ok && data.available) {
      // Update Mandi prices table with location aware 3 closest mandis
      const body = document.getElementById("dash-prices-body");
      if (body) {
        body.innerHTML = "";
        data.nearest_markets.forEach(m => {
          const tr = document.createElement("tr");
          tr.innerHTML = `
            <td>${data.crop}</td>
            <td>₹${m.price.toLocaleString()} / Quintal</td>
            <td class="trend-up"><i class="fa-solid fa-arrow-trend-up"></i> ${data.weekly_trend}</td>
            <td>${m.market} (${m.distance_km.toFixed(1)} km)</td>
          `;
          body.appendChild(tr);
        });
      }

      // Update Market Insights list
      const insightsBox = document.querySelector(".price-insights-card");
      if (insightsBox) {
        insightsBox.innerHTML = `
          <h3>Mandi Market Insights</h3>
          <div class="insight-row">
            <div class="icon"><i class="fa-solid fa-circle-check"></i></div>
            <div class="info">
              <strong>Best Mandi to Sell</strong>
              <span>${data.highest_paying_market}</span>
            </div>
          </div>
          <div class="insight-row">
            <div class="icon"><i class="fa-solid fa-arrow-trend-up"></i></div>
            <div class="info">
              <strong>Lowest Paying Mandi</strong>
              <span>${data.lowest_paying_market}</span>
            </div>
          </div>
          <div class="insight-row">
            <div class="icon"><i class="fa-solid fa-chart-line"></i></div>
            <div class="info">
              <strong>Market Demand & Trend</strong>
              <span>Weekly: ${data.weekly_trend}. Monthly: ${data.monthly_trend}</span>
            </div>
          </div>
        `;
      }

      // Render historical 30-day Chart
      if (data.price_trend_30d) {
        updateMarketTrendChart(data.crop, data.price_trend_30d);
      }
    }
  } catch (err) {
    console.log("Could not load mandi prices:", err);
  }
}

// ---------- AI PROFIT CALCULATOR STATE ----------
document.getElementById("calculateProfitBtn").addEventListener("click", () => {
  const acres = parseFloat(document.getElementById("calc-acres").value) || 1;
  const body = document.getElementById("calc-results-body");
  
  const riceCost = Math.round(9000 * acres);
  const riceRev = Math.round(52000 * acres);
  const riceProfit = riceRev - riceCost;
  
  const nutsCost = Math.round(11000 * acres);
  const nutsRev = Math.round(54800 * acres);
  const nutsProfit = nutsRev - nutsCost;
  
  body.innerHTML = `
    <tr class="best-performer" style="background-color: var(--primary-glow); border-left: 4px solid var(--primary);">
      <td>🌾 Rice (Paddy) 🏆</td>
      <td>₹${riceCost.toLocaleString()}</td>
      <td>${Math.round(24 * acres)} Quintals</td>
      <td>₹${riceRev.toLocaleString()}</td>
      <td class="net-profit-val">₹${riceProfit.toLocaleString()}</td>
      <td>${((riceProfit / riceRev)*100).toFixed(1)}%</td>
    </tr>
    <tr>
      <td>🥜 Groundnut</td>
      <td>₹${nutsCost.toLocaleString()}</td>
      <td>${Math.round(8 * acres)} Quintals</td>
      <td>₹${nutsRev.toLocaleString()}</td>
      <td class="net-profit-val">₹${nutsProfit.toLocaleString()}</td>
      <td>${((nutsProfit / nutsRev)*100).toFixed(1)}%</td>
    </tr>
  `;
});

// ---------- DOCTOR CROP & MULTIMODAL REMEDIES ----------
let doctorRecorder = null;
let doctorChunks = [];
let doctorBlob = null;

const recordSymptomBtn = document.getElementById("recordSymptomBtn");
const stopSymptomBtn = document.getElementById("stopSymptomBtn");
const leafImageInput = document.getElementById("leafImageInput");

recordSymptomBtn.addEventListener("click", async () => {
  doctorChunks = [];
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    doctorRecorder = new MediaRecorder(stream);
    doctorRecorder.ondataavailable = e => doctorChunks.push(e.data);
    doctorRecorder.onstop = () => {
      doctorBlob = new Blob(doctorChunks, { type: "audio/webm" });
      document.getElementById("recordingWave").textContent = "Audio query captured successfully.";
    };
    doctorRecorder.start();
    recordSymptomBtn.disabled = true;
    stopSymptomBtn.disabled = false;
    document.getElementById("recordingWave").style.display = "block";
    document.getElementById("recordingWave").textContent = "Recording symptom description...";
  } catch (err) {
    alert("Microphone capture failed. Please check browser permissions: " + err);
  }
});

stopSymptomBtn.addEventListener("click", () => {
  if (doctorRecorder) {
    doctorRecorder.stop();
  }
  recordSymptomBtn.disabled = false;
  stopSymptomBtn.disabled = true;
});

leafImageInput.addEventListener("change", () => {
  const file = leafImageInput.files[0];
  if (file) {
    document.getElementById("uploadFileName").textContent = file.name;
  } else {
    document.getElementById("uploadFileName").textContent = "No file selected";
  }
});

document.getElementById("getDiagnosisBtn").addEventListener("click", async () => {
  const imageFile = leafImageInput.files[0];
  const audioBlob = doctorBlob;
  
  if (!imageFile && !audioBlob) {
    alert("Please select a leaf photo or record a voice symptom description first.");
    return;
  }
  
  const loading = document.getElementById("diagnosisLoading");
  const resultBox = document.getElementById("diagnosisResultBox");
  
  loading.style.display = "block";
  resultBox.innerHTML = "";
  
  try {
    let response, data;
    const formData = new FormData();
    const isTe = currentLang === "te";
    const lblDisease = isTe ? "కనుగొనబడిన తెగులు" : "Detected Disease";
    const lblConfidence = isTe ? "ఖచ్చితత్వం" : "Confidence";
    const lblSymptoms = isTe ? "లక్షణాలు" : "Symptoms";
    const lblCauses = isTe ? "కారణాలు" : "Causes";
    const lblTreatment = isTe ? "చికిత్స" : "Treatment";
    const lblOrganic = isTe ? "సేంద్రీయ నివారణ" : "Organic Solution";
    const lblChemical = isTe ? "రసాయన నివారణ" : "Chemical Solution";
    const lblPreventive = isTe ? "నివారణ చర్యలు" : "Preventive Measures";
    const lblAiAdvice = isTe ? "AI సిఫార్సులు" : "AI Recommendations";
    const lblVoice = isTe ? "వాయిస్ వివరణ (ప్లే ఆడియో)" : "Spoken Explanation (Play Audio)";
    const lblEscalate = isTe ? "సహాయం అవసరమా?" : "Need Assistance?";
    const valEscalate = isTe ? "సమీప రైతు సేవా కేంద్రాన్ని (RSK) సంప్రదించండి" : "Contact nearest Rythu Seva Kendra (RSK) or Soil Lab";

    formData.append("lang", currentLang);
    formData.append("phone", registeredFarmer ? registeredFarmer.phone : "");
    if (imageFile) {
      formData.append("image", imageFile);
      if (audioBlob) {
        formData.append("audio", audioBlob, "voice.webm");
      }
      response = await fetch(`${BACKEND_URL}/api/photo-query`, { method: "POST", body: formData });
    } else {
      formData.append("audio", audioBlob, "voice.webm");
      response = await fetch(`${BACKEND_URL}/api/voice-query`, { method: "POST", body: formData });
    }
    
    data = await response.json();
    loading.style.display = "none";
    
    if (!response.ok) {
      resultBox.innerHTML = `<div class="report-section-details color-red">Error: ${data.error || "Failed to get advice."}</div>`;
      return;
    }
    
    resultBox.innerHTML = `
      <div class="diagnosis-report" style="background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: 12px; padding: 20px; display: flex; flex-direction: column; gap: 16px; margin-top: 16px; box-sizing: border-box;">
        <div class="report-header-badge" style="display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid var(--border-color); padding-bottom: 12px; flex-wrap: wrap; gap: 8px;">
          <strong style="font-size: 16px; color: var(--primary);"><i class="fa-solid fa-leaf"></i> ${lblDisease}: ${data.disease_label || "Unknown"}</strong>
          <span style="background: var(--primary-glow); color: var(--primary); padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 700;">${data.confidence ? (data.confidence * 100).toFixed(1) + "% " + lblConfidence : ""}</span>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px;">
          <div class="report-section-details" style="background: var(--bg-card); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
            <strong style="color: var(--text-main); font-size: 13px;"><i class="fa-solid fa-circle-info"></i> ${lblSymptoms}</strong>
            <p style="margin: 6px 0 0 0; font-size: 13px; color: var(--text-muted); line-height: 1.5;">${data.symptoms || "—"}</p>
          </div>
          <div class="report-section-details" style="background: var(--bg-card); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
            <strong style="color: var(--text-main); font-size: 13px;"><i class="fa-solid fa-bug"></i> ${lblCauses}</strong>
            <p style="margin: 6px 0 0 0; font-size: 13px; color: var(--text-muted); line-height: 1.5;">${data.causes || "—"}</p>
          </div>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px;">
          <div class="report-section-details" style="background: var(--bg-card); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
            <strong style="color: var(--text-main); font-size: 13px;"><i class="fa-solid fa-kit-medical"></i> ${lblTreatment}</strong>
            <p style="margin: 6px 0 0 0; font-size: 13px; color: var(--text-muted); line-height: 1.5;">${data.treatment || "—"}</p>
          </div>
          <div class="report-section-details" style="background: var(--bg-card); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
            <strong style="color: var(--text-main); font-size: 13px;"><i class="fa-solid fa-seedling"></i> ${lblOrganic}</strong>
            <p style="margin: 6px 0 0 0; font-size: 13px; color: var(--text-muted); line-height: 1.5;">${data.organic_solution || "—"}</p>
          </div>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px;">
          <div class="report-section-details" style="background: var(--bg-card); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
            <strong style="color: var(--text-main); font-size: 13px;"><i class="fa-solid fa-flask"></i> ${lblChemical}</strong>
            <p style="margin: 6px 0 0 0; font-size: 13px; color: var(--text-muted); line-height: 1.5;">${data.chemical_solution || "—"}</p>
          </div>
          <div class="report-section-details" style="background: var(--bg-card); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
            <strong style="color: var(--text-main); font-size: 13px;"><i class="fa-solid fa-shield-halved"></i> ${lblPreventive}</strong>
            <p style="margin: 6px 0 0 0; font-size: 13px; color: var(--text-muted); line-height: 1.5;">${data.preventive_measures || "—"}</p>
          </div>
        </div>

        <div class="report-section-details" style="background: var(--primary-glow); padding: 16px; border-radius: 8px; border: 1px solid var(--primary-glow);">
          <strong style="color: var(--primary); font-size: 14px;"><i class="fa-solid fa-wand-magic-sparkles"></i> ${lblAiAdvice}</strong>
          <p style="margin: 6px 0 0 0; font-size: 13px; color: var(--text-main); line-height: 1.5; font-weight: 500;">${data.advisory_text || "—"}</p>
        </div>

        ${data.audio_reply_url ? `
          <div class="report-section-details" style="background: var(--bg-card); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
            <strong style="color: var(--text-main); font-size: 13px;"><i class="fa-solid fa-volume-high"></i> ${lblVoice}</strong>
            <audio controls src="${BACKEND_URL}${data.audio_reply_url}" style="width:100%; margin-top:8px;"></audio>
          </div>
        ` : ""}

        <div class="report-section-details" style="background: rgba(239, 68, 68, 0.05); padding: 12px; border-radius: 8px; border: 1px solid rgba(239, 68, 68, 0.15);">
          <strong style="color: #ef4444; font-size: 13px;"><i class="fa-solid fa-phone-volume"></i> ${lblEscalate}</strong>
          <p style="margin: 6px 0 0 0; font-size: 13px; color: var(--text-muted); line-height: 1.5;">
            ${valEscalate}: <strong>Nellore RSK (Distance 2.4km)</strong>. Phone: <a href="tel:+919848012345" style="color: var(--primary); text-decoration: none;">+91 98480 12345</a>
          </p>
        </div>
      </div>
    `;
  } catch (err) {
    loading.style.display = "none";
    resultBox.innerHTML = `<div class="report-section-details color-red">Connection error: ${err.message}</div>`;
  }
});

// ---------- SMART IVR PHONE SIMULATOR ----------
let ivrSessionId = null;
let ivrActive = false;
let ivrSeconds = 0;
let ivrTimerInterval = null;
let simulatorMode = "survey";

const dialCallBtn = document.getElementById("dialCallBtn");
const hangCallBtn = document.getElementById("hangCallBtn");
const dialerKeypad = document.getElementById("dialerKeypad");
const simTranscriptBox = document.getElementById("simTranscriptBox");
const simulatorAudioPlayer = document.getElementById("simulatorAudioPlayer");

dialCallBtn.addEventListener("click", async () => {
  ivrActive = true;
  ivrSeconds = 0;
  dialCallBtn.style.display = "none";
  hangCallBtn.style.display = "flex";
  dialerKeypad.style.display = "grid";
  document.getElementById("phoneCallTimer").style.display = "block";
  document.getElementById("phone-call-status").textContent = "Ringing Helpline...";
  simTranscriptBox.innerHTML = `<div class="ai-msg">Call established. Connected to KrishakaSeva Helpdesk...</div>`;
  
  resetChecklist();
  
  ivrTimerInterval = setInterval(() => {
    ivrSeconds++;
    const mins = String(Math.floor(ivrSeconds / 60)).padStart(2, '0');
    const secs = String(ivrSeconds % 60).padStart(2, '0');
    document.getElementById("phoneCallTimer").textContent = `${mins}:${secs}`;
  }, 1000);
  
  try {
    const payload = {
      phone: registeredFarmer ? registeredFarmer.phone : "+918247543026",
      lat: registeredFarmer ? registeredFarmer.latitude : (detectedLat || 14.4426),
      lon: registeredFarmer ? registeredFarmer.longitude : (detectedLon || 79.9865)
    };
    const startUrl = simulatorMode === "sos" ? `${BACKEND_URL}/api/sos/web/start` : `${BACKEND_URL}/api/ivr/web/start`;
    const response = await fetch(startUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    const data = await response.json();
    ivrSessionId = data.session_sid;
    
    document.getElementById("phone-call-status").textContent = "KrishakaSeva Assistant Speaking";
    
    if (data.audio_url) {
      simulatorAudioPlayer.src = `${BACKEND_URL}${data.audio_url}`;
      simulatorAudioPlayer.play();
    }
    
    appendSimTranscript("ai", data.text);
  } catch (err) {
    appendSimTranscript("system", "Error connecting helpline call.");
  }
});

hangCallBtn.addEventListener("click", () => {
  hangUpHelpline();
});

function hangUpHelpline() {
  ivrActive = false;
  clearInterval(ivrTimerInterval);
  document.getElementById("phoneCallTimer").style.display = "none";
  document.getElementById("phone-call-status").textContent = "Call Terminated";
  dialCallBtn.style.display = "flex";
  hangCallBtn.style.display = "none";
  dialerKeypad.style.display = "none";
  simulatorAudioPlayer.pause();
  simulatorAudioPlayer.src = "";
  appendSimTranscript("system", "Call Ended.");
  simulatorMode = "survey";
}

function appendSimTranscript(sender, text) {
  const bubble = document.createElement("div");
  bubble.className = sender === "ai" ? "chat-bubble ai-bubble" : "chat-bubble farmer-bubble";
  bubble.textContent = text;
  simTranscriptBox.appendChild(bubble);
  simTranscriptBox.scrollTop = simTranscriptBox.scrollHeight;
}

document.querySelectorAll(".keypad-key").forEach(key => {
  key.addEventListener("click", async () => {
    if (!ivrActive || !ivrSessionId) return;
    const digit = key.dataset.key;
    
    appendSimTranscript("farmer", `Pressed Key: ${digit}`);
    
    try {
      const stepUrl = simulatorMode === "sos" ? `${BACKEND_URL}/api/sos/web/step` : `${BACKEND_URL}/api/ivr/web/step`;
      const response = await fetch(stepUrl, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          session_sid: ivrSessionId,
          digit: digit,
          phone: registeredFarmer ? registeredFarmer.phone : "+918247543026",
          lat: registeredFarmer ? registeredFarmer.latitude : (detectedLat || 14.4426),
          lon: registeredFarmer ? registeredFarmer.longitude : (detectedLon || 79.9865)
        })
      });
      const data = await response.json();
      
      if (data.profile) {
        updateChecklist(data.profile);
      }
      
      if (data.audio_url) {
        simulatorAudioPlayer.src = `${BACKEND_URL}${data.audio_url}`;
        simulatorAudioPlayer.play();
      }
      
      appendSimTranscript("ai", data.text);
      
      if (data.is_finished) {
        if (data.audio_url) {
          simulatorAudioPlayer.onended = () => {
            setTimeout(() => {
              hangUpHelpline();
              alert("Call finished! Farmer details saved to Firestore profile.");
              if (data.profile) {
                updateDashboardWithProfile(data.profile);
              }
            }, 2000);
          };
        } else {
          setTimeout(() => {
            hangUpHelpline();
            alert("Call finished! Farmer details saved to Firestore profile.");
            if (data.profile) {
              updateDashboardWithProfile(data.profile);
            }
          }, 8000);
        }
      }
    } catch (err) {
      appendSimTranscript("system", "Transmission error during DTMF digit processing.");
    }
  });
});

function resetChecklist() {
  const keys = ["crop_type", "land_size", "soil_type", "water_availability", "problem"];
  keys.forEach(k => {
    const item = document.getElementById(`chk-${k}`);
    if (item) {
      item.classList.remove("verified");
      item.querySelector(".check-value").textContent = "—";
      item.querySelector(".check-icon i").className = "fa-regular fa-circle";
    }
  });
}

function updateChecklist(profile) {
  const mapping = {
    crop_type: profile.crop_type,
    land_size: profile.land_size,
    soil_type: profile.soil_type,
    water_availability: profile.water_availability,
    problem: profile.problem
  };
  
  for (const [key, value] of Object.entries(mapping)) {
    const item = document.getElementById(`chk-${key}`);
    if (item && value && value !== "unknown" && value !== "—") {
      item.classList.add("verified");
      item.querySelector(".check-value").textContent = value;
      item.querySelector(".check-icon i").className = "fa-solid fa-circle-check";
    }
  }
}

// ---------- FLOATING CHATBOT DIALOGUE ----------
const chatbotWidget = document.getElementById("chatbotWidget");
const toggleChatBtn = document.getElementById("toggleChatBtn");
const chatWindow = document.getElementById("chatWindow");
const closeChatBtn = document.getElementById("closeChatBtn");
const chatMessagesBox = document.getElementById("chatMessagesBox");
const chatInputText = document.getElementById("chatInputText");
const sendChatBtn = document.getElementById("sendChatBtn");

toggleChatBtn.addEventListener("click", () => {
  chatWindow.style.display = chatWindow.style.display === "none" ? "flex" : "none";
});

closeChatBtn.addEventListener("click", () => {
  chatWindow.style.display = "none";
});

sendChatBtn.addEventListener("click", () => {
  sendMessageChat();
});

chatInputText.addEventListener("keypress", (e) => {
  if (e.key === "Enter") {
    sendMessageChat();
  }
});

async function sendMessageChat() {
  const text = chatInputText.value.trim();
  if (!text) return;
  
  // User bubble
  const userDiv = document.createElement("div");
  userDiv.className = "user-msg";
  userDiv.textContent = text;
  chatMessagesBox.appendChild(userDiv);
  chatInputText.value = "";
  chatMessagesBox.scrollTop = chatMessagesBox.scrollHeight;
  
  // Add to local history
  chatHistory.push({ role: "user", content: text });
  if (chatHistory.length > 10) {
    chatHistory.shift();
  }

  // Loading indicator
  const loadingDiv = document.createElement("div");
  loadingDiv.className = "ai-msg typing-indicator";
  loadingDiv.textContent = currentLang === "en" ? "KrishakaSeva is typing..." : "కృషక్ సేవా సమాధానం ఇస్తోంది...";
  chatMessagesBox.appendChild(loadingDiv);
  chatMessagesBox.scrollTop = chatMessagesBox.scrollHeight;
  
  try {
    const res = await fetch(`${BACKEND_URL}/api/chat`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        message: text,
        history: chatHistory.slice(0, -1),
        phone: registeredFarmer ? registeredFarmer.phone : ""
      })
    });
    
    const data = await res.json();
    loadingDiv.remove();
    
    const aiDiv = document.createElement("div");
    aiDiv.className = "ai-msg";
    aiDiv.textContent = data.reply || "Error generating response.";
    chatMessagesBox.appendChild(aiDiv);
    chatMessagesBox.scrollTop = chatMessagesBox.scrollHeight;
    
    // Add to history
    chatHistory.push({ role: "assistant", content: data.reply });
  } catch (err) {
    loadingDiv.remove();
    const aiDiv = document.createElement("div");
    aiDiv.className = "ai-msg";
    aiDiv.textContent = currentLang === "en" ? "Connection error. Please try again." : "అనుసంధాన లోపం. దయచేసి మళ్లీ ప్రయత్నించండి.";
    chatMessagesBox.appendChild(aiDiv);
    chatMessagesBox.scrollTop = chatMessagesBox.scrollHeight;
  }
}

// ---------- CHART.JS RENDER LOGIC ----------
function updateMarketTrendChart(cropName, trendArray) {
  const ctx = document.getElementById("marketTrendChart").getContext("2d");
  
  if (marketTrendChartInstance) {
    marketTrendChartInstance.destroy();
  }

  const labels = Array.from({length: 30}, (_, i) => `Day ${i+1}`);

  marketTrendChartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: `${cropName} Mandi Rate Trend (₹ / Quintal)`,
        data: trendArray,
        borderColor: '#10b981',
        backgroundColor: 'rgba(16, 185, 129, 0.08)',
        fill: true,
        tension: 0.3
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          grid: { color: 'rgba(200, 200, 200, 0.05)' },
          ticks: { color: '#94a3b8' }
        },
        x: {
          grid: { display: false },
          ticks: { display: false }
        }
      }
    }
  });
}

function renderAnalyticsCharts() {
  const ctxProfit = document.getElementById("profitTrendChart").getContext("2d");
  const ctxRain = document.getElementById("rainHistoryChart").getContext("2d");
  
  if (profitTrendChartInstance) profitTrendChartInstance.destroy();
  if (rainHistoryChartInstance) rainHistoryChartInstance.destroy();

  profitTrendChartInstance = new Chart(ctxProfit, {
    type: 'bar',
    data: {
      labels: ['Kharif 2024', 'Rabi 2024', 'Kharif 2025', 'Rabi 2025'],
      datasets: [{
        label: 'Net Profits (₹)',
        data: [180000, 140000, 210000, 160000],
        backgroundColor: '#10b981'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false
    }
  });

  rainHistoryChartInstance = new Chart(ctxRain, {
    type: 'line',
    data: {
      labels: ['Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov'],
      datasets: [{
        label: 'Rainfall (mm)',
        data: [110, 190, 240, 150, 80, 20],
        borderColor: '#0ea5e9',
        backgroundColor: 'rgba(14, 165, 233, 0.08)',
        fill: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false
    }
  });
}

function toggleAnalyticsState() {
  const fallbackBox = document.getElementById("analyticsFallbackBox");
  const chartsBox = document.getElementById("analyticsChartsBox");
  
  if (!registeredFarmer) {
    if (fallbackBox) fallbackBox.style.display = "flex";
    if (chartsBox) chartsBox.style.display = "none";
  } else {
    if (fallbackBox) fallbackBox.style.display = "none";
    if (chartsBox) chartsBox.style.display = "grid";
    
    const acres = registeredFarmer.land_size_acres || 1;
    const profitEst = Math.round(43000 * acres);
    document.getElementById("analytics-total-profit").textContent = `₹${profitEst.toLocaleString()} (Est.)`;
    
    const method = (registeredFarmer.irrigation_method || "").toLowerCase();
    const efficiency = method.includes("drip") || method.includes("sprinkler") ? "+45% efficiency" : "+15% efficiency";
    document.getElementById("analytics-water-savings").textContent = efficiency;
    
    renderAnalyticsCharts();
  }
}

// ---------- SOS HELP WIDGET TRIGGER ----------
const sosModal = document.getElementById("sosEmergencyModal");

document.getElementById("emergencyBtn").addEventListener("click", () => {
  sosModal.style.display = "flex";
  
  const lat = registeredFarmer ? registeredFarmer.latitude : (detectedLat || 14.4426);
  const lon = registeredFarmer ? registeredFarmer.longitude : (detectedLon || 79.9865);
  const loc = registeredFarmer ? registeredFarmer.location : "Andhra Pradesh";
  
  document.getElementById("sos-location-text").textContent = loc;
  document.getElementById("sos-lat-text").textContent = parseFloat(lat).toFixed(4);
  document.getElementById("sos-lon-text").textContent = parseFloat(lon).toFixed(4);
});

document.getElementById("closeSosBtn").addEventListener("click", () => {
  sosModal.style.display = "none";
});

sosModal.addEventListener("click", (e) => {
  if (e.target === sosModal) {
    sosModal.style.display = "none";
  }
});

document.getElementById("sendSosBroadcastBtn").addEventListener("click", async () => {
  const phoneVal = registeredFarmer ? registeredFarmer.phone : "+918247543026";
  const eventVal = document.getElementById("sos-emergency-type").value;
  const lat = registeredFarmer ? registeredFarmer.latitude : (detectedLat || 14.4426);
  const lon = registeredFarmer ? registeredFarmer.longitude : (detectedLon || 79.9865);
  const loc = registeredFarmer ? registeredFarmer.location : "Andhra Pradesh";
  
  try {
    const res = await fetch(`${BACKEND_URL}/api/sos/broadcast`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        phone: phoneVal,
        event_type: eventVal,
        latitude: lat,
        longitude: lon,
        location: loc
      })
    });
    
    if (res.ok) {
      alert(`🚨 SOS Broadcast successfully sent! SMS alerts dispatched to ${phoneVal} and the Mandal Extension Officer (+919848012345) containing coordinates: Lat ${parseFloat(lat).toFixed(4)}, Lon ${parseFloat(lon).toFixed(4)}.`);
      sosModal.style.display = "none";
    } else {
      alert("Error broadcasting SOS signal to server.");
    }
  } catch (err) {
    alert("Connection failure: " + err.message);
  }
});

document.getElementById("callFarmerSosBtn").addEventListener("click", async () => {
  const phoneVal = registeredFarmer ? registeredFarmer.phone : "+918247543026";
  
  try {
    const res = await fetch(`${BACKEND_URL}/api/sos/call-farmer`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ phone: phoneVal })
    });
    
    if (res.ok) {
      const data = await res.json();
      sosModal.style.display = "none";
      
      if (data.status === "demo") {
        alert("ℹ️ Twilio credentials missing. Triggering interactive SOS Emergency call simulation in your dashboard instead!");
        simulatorMode = "sos";
        switchTab("ivr");
        setTimeout(() => {
          document.getElementById("dialCallBtn").click();
        }, 300);
      } else {
        alert("✅ Outbound voice call initiated successfully! The system has dialed your phone.");
      }
    } else {
      alert("Error initiating outbound call from server.");
    }
  } catch (err) {
    alert("Connection failure: " + err.message);
  }
});

document.getElementById("triggerEmergencyBtn").addEventListener("click", () => {
  document.getElementById("emergencyBtn").click();
});

document.getElementById("signOutBtn").addEventListener("click", () => {
  if (confirm("Are you sure you want to sign out from KṛṣakaSevā?")) {
    localStorage.removeItem("krushakseva_phone");
    registeredFarmer = null;
    resetChecklist();
    
    document.getElementById("auth-portal-box").style.display = "flex";
    
    document.getElementById("otp-phone-input").value = "";
    document.getElementById("otp-code-input").value = "";
    document.getElementById("otp-container").style.display = "none";
    document.getElementById("send-otp-btn").style.display = "block";
    
    alert("You have signed out successfully.");
    window.location.reload();
  }
});

document.getElementById("callMyNumberBtn").addEventListener("click", async () => {
  const btn = document.getElementById("callMyNumberBtn");
  const statusDiv = document.getElementById("callMyNumberStatus");
  const phoneVal = registeredFarmer ? registeredFarmer.phone : "";
  
  if (!phoneVal) {
    statusDiv.style.display = "flex";
    statusDiv.style.color = "#ef4444";
    statusDiv.textContent = "Unable to initiate the call. Please register first.";
    return;
  }
  
  btn.disabled = true;
  statusDiv.style.display = "flex";
  statusDiv.style.color = "#2563eb";
  statusDiv.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Calling your registered mobile number...`;
  
  try {
    const res = await fetch(`${BACKEND_URL}/api/ivr/trigger-outbound`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ phone: phoneVal })
    });
    
    if (res.ok) {
      const data = await res.json();
      if (data.status === "triggered") {
        statusDiv.style.color = "#16a34a";
        statusDiv.innerHTML = `<i class="fa-solid fa-circle-check"></i> Your call has been initiated. Please answer your phone.`;
      } else {
        statusDiv.style.color = "#ef4444";
        statusDiv.innerHTML = `<i class="fa-solid fa-circle-xmark"></i> Unable to initiate the call. Please try again.`;
      }
    } else {
      statusDiv.style.color = "#ef4444";
      statusDiv.innerHTML = `<i class="fa-solid fa-circle-xmark"></i> Unable to initiate the call. Please try again.`;
    }
  } catch (err) {
    statusDiv.style.color = "#ef4444";
    statusDiv.innerHTML = `<i class="fa-solid fa-circle-xmark"></i> Unable to initiate the call. Please try again.`;
  } finally {
    setTimeout(() => {
      btn.disabled = false;
      statusDiv.style.display = "none";
    }, 6000);
  }
});

// ---------- INITIAL INITIALIZATION ON PAGE LOAD ----------
window.addEventListener("DOMContentLoaded", async () => {
  updateLanguageUI();
  toggleAnalyticsState();
  
  // Immediately request GPS permission on page land!
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(async (position) => {
      detectedLat = position.coords.latitude;
      detectedLon = position.coords.longitude;
      const geoResult = await reverseGeocode(detectedLat, detectedLon);
      if (geoResult) {
        document.getElementById("reg-state").value = geoResult.state;
        document.getElementById("reg-district").value = geoResult.district;
        document.getElementById("reg-mandal").value = geoResult.mandal;
        document.getElementById("reg-village").value = geoResult.village;
        document.getElementById("reg-pin").value = geoResult.pin;
      }
      
      if (registeredFarmer) {
        registeredFarmer.latitude = detectedLat;
        registeredFarmer.longitude = detectedLon;
        if (geoResult) {
          registeredFarmer.location = `${geoResult.village}, ${geoResult.district}, ${geoResult.state}`;
        }
        updateDashboardWithProfile(registeredFarmer);
      } else {
        fetchWeatherForCoordinates(detectedLat, detectedLon);
      }
    }, error => {
      console.log("Initial GPS request declined/failed. Fallback to button triggers.");
    });
  }
  
  // Attempt to load existing user profile from localStorage
  try {
    const savedPhone = localStorage.getItem("krushakseva_phone");
    if (savedPhone) {
      const res = await fetch(`${BACKEND_URL}/api/farmer-profile/${savedPhone}`);
      if (res.ok) {
        const profile = await res.json();
        if (profile && profile.phone) {
          registeredFarmer = profile;
          document.getElementById("auth-portal-box").style.display = "none";
          updateDashboardWithProfile(profile);
        }
      }
    }
  } catch (err) {
    console.log("Auto-login error on reload:", err);
  }
});