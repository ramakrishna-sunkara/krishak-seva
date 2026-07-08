package com.kisanalert.core.utils

import com.kisanalert.domain.model.FarmingQuestionAnswer
import com.kisanalert.domain.model.PreferredLanguage

object LocalFarmingQuestionAdvisor {
    fun buildOfflineAnswer(
        question: String,
        cropName: String,
        language: PreferredLanguage
    ): FarmingQuestionAnswer {
        val normalizedQuestion = question.lowercase()
        val answer = when {
            normalizedQuestion.contains("irrigation") || normalizedQuestion.contains("water") ->
                buildIrrigationAnswer(cropName = cropName, language = language)
            normalizedQuestion.contains("fertilizer") || normalizedQuestion.contains("nutrient") ->
                buildFertilizerAnswer(cropName = cropName, language = language)
            normalizedQuestion.contains("pest") || normalizedQuestion.contains("insect") ->
                buildPestAnswer(cropName = cropName, language = language)
            normalizedQuestion.contains("rain") || normalizedQuestion.contains("weather") ->
                buildWeatherAnswer(cropName = cropName, language = language)
            else -> buildGeneralAnswer(cropName = cropName, language = language)
        }
        val suggestions = buildSuggestions(language = language, cropName = cropName)
        return FarmingQuestionAnswer(
            answer = answer,
            followUpSuggestions = suggestions,
            isFromCloud = false
        )
    }

    private fun buildIrrigationAnswer(cropName: String, language: PreferredLanguage): String {
        return if (language == PreferredLanguage.TELUGU) {
            "$cropName కోసం ఉదయం లేదా సాయంత్రం నీటి పారుదల చేయండి. మట్టి తేమను తనిఖీ చేసి అవసరమైనప్పుడు మాత్రమే నీరు పెట్టండి."
        } else {
            "For $cropName, irrigate early morning or evening. Check soil moisture before watering and avoid over-irrigation."
        }
    }

    private fun buildFertilizerAnswer(cropName: String, language: PreferredLanguage): String {
        return if (language == PreferredLanguage.TELUGU) {
            "$cropName కోసం NPK ఎరువును రెండు విడతల్లో వేయండి. మొక్కల బలం మరియు దశను బట్టి మోతాదు సర్దుబాటు చేయండి."
        } else {
            "Apply NPK fertilizer for $cropName in split doses based on crop stage. Add organic compost to improve soil health."
        }
    }

    private fun buildPestAnswer(cropName: String, language: PreferredLanguage): String {
        return if (language == PreferredLanguage.TELUGU) {
            "$cropName పంటలో చీడపీడలు కనిపిస్తే వెంటనే వేప నూనె ద్రావణం (5ml/L) చల్లండి. ప్రభావిత ఆకులను తొలగించండి."
        } else {
            "If pests appear on $cropName, spray neem oil solution (5 ml per liter) and remove heavily affected leaves."
        }
    }

    private fun buildWeatherAnswer(cropName: String, language: PreferredLanguage): String {
        return if (language == PreferredLanguage.TELUGU) {
            "వర్షం అనుకూలంగా ఉన్నప్పుడు $cropName కోసం నీటి పారుదల తగ్గించండి. గాలి బలంగా ఉంటే పంటను కాపాడుకోండి."
        } else {
            "During rainy weather, reduce irrigation for $cropName and protect harvested produce from moisture damage."
        }
    }

    private fun buildGeneralAnswer(cropName: String, language: PreferredLanguage): String {
        return if (language == PreferredLanguage.TELUGU) {
            "$cropName పంటకు స్థానిక వ్యవసాయ అధికారిని సంప్రదించండి. నేల తేమ, పోషకాలు మరియు పంట ఆరోగ్యాన్ని వారానికి రెండుసార్లు పర్యవేక్షించండి."
        } else {
            "For $cropName, monitor soil moisture and crop health twice a week. Consult your local agricultural officer for field-specific advice."
        }
    }

    private fun buildSuggestions(language: PreferredLanguage, cropName: String): List<String> {
        return buildStarterSuggestions(language = language, cropName = cropName)
    }

    fun buildStarterSuggestions(language: PreferredLanguage, cropName: String): List<String> {
        return if (language == PreferredLanguage.TELUGU) {
            listOf(
                "$cropName కోసం నీటి పారుదల ఎప్పుడు చేయాలి?",
                "ఎరువు ఎప్పుడు వేయాలి?",
                "పంటలో పురుగులు ఎలా నియంత్రించాలి?"
            )
        } else {
            listOf(
                "When should I irrigate $cropName?",
                "What fertilizer schedule should I follow?",
                "How do I control pests in my field?"
            )
        }
    }
}
