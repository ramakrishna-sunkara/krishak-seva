package com.kisanalert.core.ui.localization

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kisanalert.R
import com.kisanalert.domain.model.DashboardAlert
import com.kisanalert.domain.model.DashboardSeedAlerts
import com.kisanalert.domain.model.CropHealthStatus
import com.kisanalert.domain.model.DiseaseSeverity
import com.kisanalert.domain.model.FarmerProfileValidationError
import com.kisanalert.domain.model.FarmingSeason
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.SoilType
import com.kisanalert.domain.model.WaterSource
import com.kisanalert.presentation.notifications.NotificationFilter

@Composable
fun DashboardAlert.localizedTitle(): String {
    return when (id) {
        DashboardSeedAlerts.WELCOME_ID -> stringResource(R.string.alert_welcome_title)
        DashboardSeedAlerts.WEATHER_ID -> stringResource(R.string.alert_weather_monitor_title)
        DashboardSeedAlerts.CROP_HEALTH_ID -> stringResource(R.string.alert_crop_health_title)
        else -> title
    }
}

@Composable
fun DashboardAlert.localizedMessage(): String {
    return when (id) {
        DashboardSeedAlerts.WELCOME_ID -> {
            val parts = message.split("|", limit = 2)
            val crop = parts.getOrElse(0) { "" }
            val village = parts.getOrElse(1) { "" }
            stringResource(R.string.alert_welcome_message, crop, village)
        }
        DashboardSeedAlerts.WEATHER_ID -> {
            val district = message.ifBlank { title }
            stringResource(R.string.alert_weather_monitor_message, district)
        }
        DashboardSeedAlerts.CROP_HEALTH_ID -> stringResource(R.string.alert_crop_health_message)
        else -> message
    }
}

@Composable
fun PreferredLanguage.localizedLabel(): String {
    return when (this) {
        PreferredLanguage.ENGLISH -> stringResource(R.string.language_english)
        PreferredLanguage.TELUGU -> stringResource(R.string.language_telugu)
    }
}

@Composable
fun SoilType.localizedLabel(): String {
    return stringResource(labelResId())
}

@Composable
fun WaterSource.localizedLabel(): String {
    return stringResource(labelResId())
}

@Composable
fun CropHealthStatus.localizedLabel(): String {
    return stringResource(labelResId())
}

@Composable
fun FarmingSeason.localizedLabel(): String {
    return stringResource(labelResId())
}

@Composable
fun NotificationFilter.localizedLabel(): String {
    return stringResource(labelResId())
}

@Composable
fun DiseaseSeverity.localizedLabel(): String {
    return stringResource(labelResId())
}

@Composable
fun FarmerProfileValidationError.localizedMessage(): String {
    return stringResource(messageResId())
}

@StringRes
fun SoilType.labelResId(): Int {
    return when (this) {
        SoilType.LOAMY -> R.string.soil_loamy
        SoilType.CLAY -> R.string.soil_clay
        SoilType.SANDY -> R.string.soil_sandy
        SoilType.RED -> R.string.soil_red
        SoilType.BLACK -> R.string.soil_black
        SoilType.ALLUVIAL -> R.string.soil_alluvial
    }
}

@StringRes
fun WaterSource.labelResId(): Int {
    return when (this) {
        WaterSource.BOREWELL -> R.string.water_borewell
        WaterSource.CANAL -> R.string.water_canal
        WaterSource.RAIN_FED -> R.string.water_rain_fed
        WaterSource.DRIP -> R.string.water_drip
        WaterSource.SPRINKLER -> R.string.water_sprinkler
        WaterSource.RIVER -> R.string.water_river
    }
}

@StringRes
fun CropHealthStatus.labelResId(): Int {
    return when (this) {
        CropHealthStatus.GOOD -> R.string.crop_health_good
        CropHealthStatus.WARNING -> R.string.crop_health_warning
        CropHealthStatus.CRITICAL -> R.string.crop_health_critical
    }
}

@StringRes
fun FarmingSeason.labelResId(): Int {
    return when (this) {
        FarmingSeason.KHARIF -> R.string.season_kharif
        FarmingSeason.RABI -> R.string.season_rabi
        FarmingSeason.ZAID -> R.string.season_zaid
    }
}

@StringRes
fun NotificationFilter.labelResId(): Int {
    return when (this) {
        NotificationFilter.ALL -> R.string.notification_filter_all
        NotificationFilter.WEATHER -> R.string.notification_filter_weather
        NotificationFilter.IRRIGATION -> R.string.notification_filter_irrigation
        NotificationFilter.DISEASE -> R.string.notification_filter_disease
    }
}

@StringRes
fun DiseaseSeverity.labelResId(): Int {
    return when (this) {
        DiseaseSeverity.LOW -> R.string.severity_low
        DiseaseSeverity.MEDIUM -> R.string.severity_medium
        DiseaseSeverity.HIGH -> R.string.severity_high
    }
}

@StringRes
fun FarmerProfileValidationError.messageResId(): Int {
    return when (this) {
        FarmerProfileValidationError.NAME_TOO_SHORT -> R.string.validation_name_short
        FarmerProfileValidationError.INVALID_PINCODE -> R.string.validation_invalid_pincode
        FarmerProfileValidationError.VILLAGE_REQUIRED -> R.string.validation_village_required
        FarmerProfileValidationError.DISTRICT_REQUIRED -> R.string.validation_district_required
        FarmerProfileValidationError.STATE_REQUIRED -> R.string.validation_state_required
        FarmerProfileValidationError.INVALID_FARM_SIZE -> R.string.validation_invalid_farm_size
        FarmerProfileValidationError.SOIL_TYPE_REQUIRED -> R.string.validation_soil_required
        FarmerProfileValidationError.WATER_SOURCE_REQUIRED -> R.string.validation_water_required
        FarmerProfileValidationError.CURRENT_CROP_REQUIRED -> R.string.validation_crop_required
    }
}
