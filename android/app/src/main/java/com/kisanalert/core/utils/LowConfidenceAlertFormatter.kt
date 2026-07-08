package com.kisanalert.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object LowConfidenceAlertFormatter {
    fun formatFarmerInfo(farmerName: String, phoneNumber: String?): String {
        val resolvedName = farmerName.trim().ifBlank { "Farmer" }
        val resolvedPhone = formatPhoneNumber(phoneNumber)
        return "$resolvedName / $resolvedPhone"
    }

    fun formatConfidence(confidencePercent: Int): String {
        return "${confidencePercent.coerceIn(0, 100)}%"
    }

    fun formatPhoneNumber(phoneNumber: String?): String {
        val digitsOnly = phoneNumber.orEmpty().filter { character -> character.isDigit() }
        if (digitsOnly.isEmpty()) {
            return "N/A"
        }
        val nationalNumber = when {
            digitsOnly.length > 10 && digitsOnly.startsWith("91") -> digitsOnly.takeLast(10)
            else -> digitsOnly
        }
        return if (nationalNumber.length == 10) {
            "+91-$nationalNumber"
        } else {
            phoneNumber.orEmpty().ifBlank { "N/A" }
        }
    }

    fun formatFarmSize(farmSizeAcres: Double): String {
        return String.format(Locale.US, "%.2f", farmSizeAcres)
    }

    fun formatCoordinate(coordinate: Double?): String {
        return coordinate?.let { value ->
            String.format(Locale.US, "%.6f", value)
        }.orEmpty()
    }

    fun formatPreventionTips(preventionTips: List<String>): String {
        return preventionTips
            .map { tip -> tip.trim() }
            .filter { tip -> tip.isNotEmpty() }
            .joinToString(separator = " | ")
    }

    fun formatScannedAt(scannedAtMillis: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(scannedAtMillis))
    }
}
