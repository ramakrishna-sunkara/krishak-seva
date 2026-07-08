package com.kisanalert.domain.usecase

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.CropDoctorScan
import com.kisanalert.domain.repository.CropDoctorRepository
import javax.inject.Inject

class GetCropDoctorHistoryUseCase @Inject constructor(
    private val cropDoctorRepository: CropDoctorRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend fun execute(): Result<List<CropDoctorScan>> {
        val userId = getCurrentUserIdUseCase.execute()
            ?: return Result.Error(message = "Please sign in to view scan history.")
        return Result.Success(cropDoctorRepository.getScanHistory(userId))
    }
}

class AnalyzeCropDiseaseUseCase @Inject constructor(
    private val cropDoctorRepository: CropDoctorRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getCurrentFarmerProfileUseCase: GetCurrentFarmerProfileUseCase,
    private val getPreferredLanguageUseCase: GetPreferredLanguageUseCase
) {
    suspend fun execute(imageLocalPath: String): Result<CropDoctorScan> {
        val userId = getCurrentUserIdUseCase.execute()
            ?: return Result.Error(message = "Please sign in to analyze crop images.")
        val profile = getCurrentFarmerProfileUseCase.execute()
            ?: return Result.Error(message = "Complete farmer registration before using Crop Doctor.")
        val language = getPreferredLanguageUseCase.execute()
        return cropDoctorRepository.analyzeCropImage(
            userId = userId,
            imageLocalPath = imageLocalPath,
            cropName = profile.currentCrop,
            farmerName = profile.name,
            village = profile.village,
            district = profile.district,
            state = profile.state,
            languageCode = language.code
        )
    }
}
