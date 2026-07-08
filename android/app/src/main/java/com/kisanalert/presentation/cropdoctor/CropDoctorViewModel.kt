package com.kisanalert.presentation.cropdoctor

import androidx.lifecycle.viewModelScope
import com.kisanalert.core.constants.CropDoctorErrors
import com.kisanalert.core.utils.CropDoctorErrorMapper
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.usecase.AnalyzeCropDiseaseUseCase
import com.kisanalert.domain.usecase.GetCropDoctorHistoryUseCase
import com.kisanalert.domain.usecase.GetCurrentFarmerProfileUseCase
import com.kisanalert.domain.usecase.NotifyLowConfidenceAlertUseCase
import com.kisanalert.domain.model.CropDoctorScan
import com.kisanalert.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CropDoctorEvent {
    data object ScreenOpened : CropDoctorEvent
    data class CameraPermissionResult(val isGranted: Boolean) : CropDoctorEvent
    data class ImageCaptured(val localPath: String) : CropDoctorEvent
    data class CaptureFailed(val message: String) : CropDoctorEvent
    data object AnalyzeCapturedImage : CropDoctorEvent
    data object RetryAnalysis : CropDoctorEvent
    data object RetakePhoto : CropDoctorEvent
    data class SelectHistoryScan(val scanId: String) : CropDoctorEvent
    data object DismissError : CropDoctorEvent
}

@HiltViewModel
class CropDoctorViewModel @Inject constructor(
    private val getCropDoctorHistoryUseCase: GetCropDoctorHistoryUseCase,
    private val analyzeCropDiseaseUseCase: AnalyzeCropDiseaseUseCase,
    private val getCurrentFarmerProfileUseCase: GetCurrentFarmerProfileUseCase,
    private val notifyLowConfidenceAlertUseCase: NotifyLowConfidenceAlertUseCase
) : MviViewModel<CropDoctorEvent, CropDoctorUiState>(
    initialState = CropDoctorUiState()
) {
    init {
        onEvent(CropDoctorEvent.ScreenOpened)
    }

    override fun onEvent(event: CropDoctorEvent) {
        when (event) {
            CropDoctorEvent.ScreenOpened -> loadScreenData()
            is CropDoctorEvent.CameraPermissionResult -> {
                setState { currentState ->
                    currentState.copy(hasCameraPermission = event.isGranted)
                }
            }
            is CropDoctorEvent.ImageCaptured -> {
                setState { currentState ->
                    currentState.copy(
                        capturedImagePath = event.localPath,
                        showCamera = false,
                        latestScan = null,
                        errorMessage = null,
                        analysisErrorCode = null
                    )
                }
            }
            is CropDoctorEvent.CaptureFailed -> {
                setState { currentState ->
                    currentState.copy(errorMessage = event.message)
                }
            }
            CropDoctorEvent.AnalyzeCapturedImage -> analyzeCapturedImage()
            CropDoctorEvent.RetryAnalysis -> analyzeCapturedImage()
            CropDoctorEvent.RetakePhoto -> {
                setState { currentState ->
                    currentState.copy(
                        capturedImagePath = null,
                        latestScan = null,
                        showCamera = true,
                        errorMessage = null,
                        isOfflineMode = false,
                        analysisErrorCode = null
                    )
                }
            }
            is CropDoctorEvent.SelectHistoryScan -> selectHistoryScan(scanId = event.scanId)
            CropDoctorEvent.DismissError -> {
                setState { currentState ->
                    currentState.copy(errorMessage = null, analysisErrorCode = null)
                }
            }
        }
    }

    private fun loadScreenData() {
        viewModelScope.launch {
            setState { currentState -> currentState.copy(isLoading = true) }
            val profile = getCurrentFarmerProfileUseCase.execute()
            val currentCrop = profile?.currentCrop.orEmpty()
            when (val historyResult = getCropDoctorHistoryUseCase.execute()) {
                is Result.Success -> setState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        currentCrop = currentCrop,
                        scanHistory = historyResult.data
                    )
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        currentCrop = currentCrop,
                        errorMessage = historyResult.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun analyzeCapturedImage() {
        val imagePath = currentState.capturedImagePath
        if (imagePath.isNullOrBlank()) {
            setState { currentState ->
                currentState.copy(errorMessage = CropDoctorErrors.NO_CAPTURE)
            }
            return
        }
        viewModelScope.launch {
            setState { currentState ->
                currentState.copy(isAnalyzing = true, errorMessage = null, analysisErrorCode = null)
            }
            when (val result = analyzeCropDiseaseUseCase.execute(imageLocalPath = imagePath)) {
                is Result.Success -> {
                    notifyLowConfidenceIfNeeded(scan = result.data)
                    setState { currentState ->
                        currentState.copy(
                            isAnalyzing = false,
                            latestScan = result.data,
                            scanHistory = listOf(result.data) + currentState.scanHistory.filter { scan ->
                                scan.id != result.data.id
                            },
                            isOfflineMode = false,
                            analysisErrorCode = null
                        )
                    }
                }
                is Result.Error -> {
                    val errorCode = CropDoctorErrorMapper.resolveErrorCode(result.exception)
                        .let { mappedCode ->
                            if (CropDoctorErrors.isKnownCode(result.message)) {
                                result.message!!
                            } else {
                                mappedCode
                            }
                        }
                    val isInlineError = CropDoctorErrors.shouldShowInlineCard(errorCode)
                    setState { currentState ->
                        currentState.copy(
                            isAnalyzing = false,
                            latestScan = null,
                            analysisErrorCode = if (isInlineError) errorCode else null,
                            errorMessage = if (isInlineError) null else errorCode
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun notifyLowConfidenceIfNeeded(scan: CropDoctorScan) {
        notifyLowConfidenceAlertUseCase.execute(scan = scan)
    }

    private fun selectHistoryScan(scanId: String) {
        val selectedScan = currentState.scanHistory.firstOrNull { scan -> scan.id == scanId } ?: return
        setState { currentState ->
            currentState.copy(
                latestScan = selectedScan,
                capturedImagePath = selectedScan.imageLocalPath,
                showCamera = false,
                isOfflineMode = !selectedScan.diagnosis.isFromCloud
            )
        }
    }
}
