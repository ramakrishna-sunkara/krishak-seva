package com.kisanalert.core.constants

object FirestoreCollections {
    const val FARMER_PROFILES: String = "farmer_profiles"
    const val CROP_RECOMMENDATIONS: String = "crop_recommendations"
}

object FarmOptions {
    val INDIAN_STATES: List<String> = listOf(
        "Andhra Pradesh",
        "Arunachal Pradesh",
        "Assam",
        "Bihar",
        "Chhattisgarh",
        "Goa",
        "Gujarat",
        "Haryana",
        "Himachal Pradesh",
        "Jharkhand",
        "Karnataka",
        "Kerala",
        "Madhya Pradesh",
        "Maharashtra",
        "Manipur",
        "Meghalaya",
        "Mizoram",
        "Nagaland",
        "Odisha",
        "Punjab",
        "Rajasthan",
        "Sikkim",
        "Tamil Nadu",
        "Telangana",
        "Tripura",
        "Uttar Pradesh",
        "Uttarakhand",
        "West Bengal"
    )
    val STATE_DISTRICTS: Map<String, List<String>> = mapOf(
        "Telangana" to listOf(
            "Adilabad",
            "Bhadradri Kothagudem",
            "Hyderabad",
            "Jagtial",
            "Jangaon",
            "Jayashankar Bhupalpally",
            "Jogulamba Gadwal",
            "Kamareddy",
            "Karimnagar",
            "Khammam",
            "Komaram Bheem",
            "Mahabubabad",
            "Mahabubnagar",
            "Mancherial",
            "Medak",
            "Medchal-Malkajgiri",
            "Mulugu",
            "Nagarkurnool",
            "Nalgonda",
            "Narayanpet",
            "Nirmal",
            "Nizamabad",
            "Peddapalli",
            "Rajanna Sircilla",
            "Rangareddy",
            "Sangareddy",
            "Siddipet",
            "Suryapet",
            "Vikarabad",
            "Wanaparthy",
            "Warangal",
            "Yadadri Bhuvanagiri"
        ),
        "Andhra Pradesh" to listOf(
            "Anantapur",
            "Chittoor",
            "East Godavari",
            "Guntur",
            "Krishna",
            "Kurnool",
            "Nellore",
            "Prakasam",
            "Srikakulam",
            "Visakhapatnam",
            "Vizianagaram",
            "West Godavari",
            "YSR Kadapa"
        )
    )
    val COMMON_CROPS: List<String> = listOf(
        "Rice",
        "Cotton",
        "Maize",
        "Wheat",
        "Sugarcane",
        "Groundnut",
        "Soybean",
        "Chilli",
        "Tomato",
        "Onion",
        "Turmeric",
        "Paddy",
        "Bajra",
        "Jowar",
        "Pulses",
        "Vegetables",
        "Fruits",
        "Other"
    )
}
