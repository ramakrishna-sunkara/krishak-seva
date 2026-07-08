package com.kisanalert.core.utils

import com.kisanalert.domain.model.CropDiseaseDiagnosis

object CropDiseaseDiagnosisValidator {
    private val STRONG_INVALID_MARKERS: List<String> = listOf(
        "irrelevant objects",
        "not a crop",
        "not appear to be a crop",
        "does not appear to be",
        "doesn't appear to be",
        "no crop visible",
        "no plant visible",
        "cannot analyze",
        "unable to analyze",
        "cannot identify",
        "can not identify",
        "image does not show",
        "image doesn't show",
        "does not contain a crop",
        "doesn't contain a crop",
        "invalid image",
        "not an agricultural",
        "non-agricultural"
    )

    private val PHOTO_INSTRUCTION_MARKERS: List<String> = listOf(
        "clearly show",
        "plant parts",
        "good lighting",
        "blurriness",
        "blurry",
        "photographing plants",
        "retake",
        "take photos",
        "take a clear photo",
        "upload a clear",
        "frame when photographing"
    )

    fun isValidCropImage(
        diagnosis: CropDiseaseDiagnosis,
        isValidCropImageFlag: Boolean? = null
    ): Boolean {
        if (isValidCropImageFlag == false) {
            return false
        }
        val combinedText = buildCombinedText(diagnosis).lowercase()
        if (STRONG_INVALID_MARKERS.any { marker -> combinedText.contains(marker) }) {
            return false
        }
        val photoInstructionTipCount = diagnosis.preventionTips.count { tip ->
            val normalizedTip = tip.lowercase()
            PHOTO_INSTRUCTION_MARKERS.any { marker -> normalizedTip.contains(marker) }
        }
        if (diagnosis.isHealthy && photoInstructionTipCount >= 2) {
            return false
        }
        if (diagnosis.isHealthy && photoInstructionTipCount >= 1 && combinedText.contains("ensure images")) {
            return false
        }
        val weakMarkerCount = PHOTO_INSTRUCTION_MARKERS.count { marker -> combinedText.contains(marker) }
        return !(diagnosis.isHealthy && weakMarkerCount >= 3)
    }

    private fun buildCombinedText(diagnosis: CropDiseaseDiagnosis): String {
        return listOf(
            diagnosis.diseaseName,
            diagnosis.symptoms,
            diagnosis.treatmentAdvice,
            diagnosis.preventionTips.joinToString(separator = " ")
        ).joinToString(separator = " ")
    }
}
