package com.kisanalert.data.repository

import com.kisanalert.core.utils.DistrictNameMatcher
import com.kisanalert.data.local.GroundWaterAssetDataSource
import com.kisanalert.data.local.GroundWaterLookupRecord
import com.kisanalert.domain.model.GroundWaterDistrictAssessment
import com.kisanalert.domain.repository.GroundWaterAssessmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroundWaterAssessmentRepositoryImpl @Inject constructor(
    private val groundWaterAssetDataSource: GroundWaterAssetDataSource
) : GroundWaterAssessmentRepository {
    override suspend fun getDistrictAssessment(
        state: String,
        district: String
    ): GroundWaterDistrictAssessment? {
        val normalizedState: String = DistrictNameMatcher.normalizeDistrictName(state)
        if (normalizedState.isBlank() || district.isBlank()) {
            return null
        }
        val records: List<GroundWaterLookupRecord> = groundWaterAssetDataSource.loadLookupRecords()
        return records.firstOrNull { record ->
            DistrictNameMatcher.normalizeDistrictName(record.state) == normalizedState &&
                DistrictNameMatcher.matchesDistrict(
                    profileDistrict = district,
                    candidateDistrict = record.district,
                    districtAliases = record.districtAliases
                )
        }?.assessment
    }
}
