package com.kisanalert.core.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.kisanalert.domain.model.PreferredLanguage
import java.util.Locale

object LocaleManager {
    fun applyApplicationLocale(localeCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(localeCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun resolveLocaleCode(
        profileLanguageCode: String?,
        storedLocaleCode: String?
    ): String {
        if (!storedLocaleCode.isNullOrBlank()) {
            return storedLocaleCode
        }
        if (!profileLanguageCode.isNullOrBlank()) {
            return profileLanguageCode
        }
        return PreferredLanguage.TELUGU.code
    }

    fun toPreferredLanguage(localeCode: String): PreferredLanguage {
        return PreferredLanguage.entries.firstOrNull { language ->
            language.code == localeCode
        } ?: PreferredLanguage.TELUGU
    }

    fun getDisplayLocale(localeCode: String): Locale {
        return Locale.forLanguageTag(localeCode)
    }
}
