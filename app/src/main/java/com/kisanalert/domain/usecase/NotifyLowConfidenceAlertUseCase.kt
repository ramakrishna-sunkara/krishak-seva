package com.kisanalert.domain.usecase

import com.kisanalert.BuildConfig
import com.kisanalert.core.constants.CropDoctorConstants
import com.kisanalert.data.remote.LowConfidenceAlertDataSource
import com.kisanalert.data.remote.LowConfidenceAlertRequestMapper
import com.kisanalert.di.ApplicationScope
import com.kisanalert.domain.model.CropDoctorScan
import com.kisanalert.domain.repository.AuthRepository
import com.kisanalert.domain.repository.CropDoctorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotifyLowConfidenceAlertUseCase @Inject constructor(
    private val lowConfidenceAlertDataSource: LowConfidenceAlertDataSource,
    private val cropDoctorRepository: CropDoctorRepository,
    private val authRepository: AuthRepository,
    private val getCurrentFarmerProfileUseCase: GetCurrentFarmerProfileUseCase,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    private val notifiedScanIds: MutableSet<String> = mutableSetOf()

    fun execute(scan: CropDoctorScan) {
        if (!scan.diagnosis.isLowConfidence(CropDoctorConstants.LOW_CONFIDENCE_THRESHOLD_PERCENT)) {
            return
        }
        if (!notifiedScanIds.add(scan.id)) {
            return
        }
        applicationScope.launch {
            val profile = getCurrentFarmerProfileUseCase.execute() ?: return@launch
            val authUser = authRepository.getCurrentUser()
            val scanWithPhoto = cropDoctorRepository.ensureScanImageStorageUrl(scan = scan)
            val request = LowConfidenceAlertRequestMapper.map(
                scan = scanWithPhoto,
                profile = profile,
                authUser = authUser,
                appVersion = BuildConfig.VERSION_NAME
            )
            lowConfidenceAlertDataSource.submitAlert(request = request)
        }
    }
}
