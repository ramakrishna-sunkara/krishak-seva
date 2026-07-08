package com.kisanalert.presentation.cropdoctor

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kisanalert.R
import com.kisanalert.core.constants.CropDoctorErrors

@StringRes
fun cropDoctorErrorStringRes(errorCode: String): Int {
    return when (errorCode) {
        CropDoctorErrors.INVALID_CROP_IMAGE -> R.string.crop_doctor_invalid_image_message
        CropDoctorErrors.QUOTA_EXCEEDED -> R.string.crop_doctor_error_quota_exceeded
        CropDoctorErrors.SERVICE_UNAVAILABLE -> R.string.crop_doctor_error_service_unavailable
        CropDoctorErrors.TIMEOUT -> R.string.crop_doctor_error_timeout
        CropDoctorErrors.UNAUTHENTICATED -> R.string.crop_doctor_error_unauthenticated
        CropDoctorErrors.NOT_CONFIGURED -> R.string.crop_doctor_error_not_configured
        CropDoctorErrors.NO_IMAGE -> R.string.crop_doctor_error_no_image
        CropDoctorErrors.IMAGE_NOT_FOUND -> R.string.crop_doctor_error_image_not_found
        CropDoctorErrors.NO_CAPTURE -> R.string.crop_doctor_error_no_capture
        else -> R.string.crop_doctor_error_generic
    }
}

@StringRes
fun cropDoctorErrorTitleRes(errorCode: String): Int {
    return when (errorCode) {
        CropDoctorErrors.INVALID_CROP_IMAGE -> R.string.crop_doctor_invalid_image_title
        CropDoctorErrors.QUOTA_EXCEEDED -> R.string.crop_doctor_error_quota_title
        else -> R.string.crop_doctor_error_service_title
    }
}

@Composable
fun cropDoctorErrorMessage(errorCode: String): String {
    return stringResource(cropDoctorErrorStringRes(errorCode))
}

@Composable
fun cropDoctorErrorTitle(errorCode: String): String {
    return stringResource(cropDoctorErrorTitleRes(errorCode))
}
