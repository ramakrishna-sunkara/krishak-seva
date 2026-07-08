package com.kisanalert.presentation.cropdoctor

import com.kisanalert.domain.model.CropDoctorScan

data class CropDoctorUiState(
    val isLoading: Boolean = true,
    val hasCameraPermission: Boolean = false,
    val isAnalyzing: Boolean = false,
    val currentCrop: String = "",
    val capturedImagePath: String? = null,
    val latestScan: CropDoctorScan? = null,
    val scanHistory: List<CropDoctorScan> = emptyList(),
    val showCamera: Boolean = true,
    val analysisErrorCode: String? = null,
    val errorMessage: String? = null,
    val isOfflineMode: Boolean = false
)
