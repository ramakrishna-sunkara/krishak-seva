package com.kisanalert.core.utils

object DistrictNameMatcher {
    fun normalizeDistrictName(value: String): String {
        return value.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
    }

    fun matchesDistrict(
        profileDistrict: String,
        candidateDistrict: String,
        districtAliases: List<String>
    ): Boolean {
        val normalizedProfileDistrict: String = normalizeDistrictName(profileDistrict)
        if (normalizedProfileDistrict.isBlank()) {
            return false
        }
        val candidates: List<String> = buildList {
            add(candidateDistrict)
            addAll(districtAliases)
        }
        return candidates.any { candidate ->
            val normalizedCandidate: String = normalizeDistrictName(candidate)
            normalizedCandidate.isNotBlank() && (
                normalizedCandidate == normalizedProfileDistrict ||
                    normalizedCandidate.contains(normalizedProfileDistrict) ||
                    normalizedProfileDistrict.contains(normalizedCandidate)
                )
        }
    }
}
