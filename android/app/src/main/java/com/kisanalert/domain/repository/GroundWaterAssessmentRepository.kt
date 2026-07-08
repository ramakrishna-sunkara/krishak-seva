package com.kisanalert.domain.repository

import com.kisanalert.domain.model.GroundWaterDistrictAssessment

interface GroundWaterAssessmentRepository {
    suspend fun getDistrictAssessment(
        state: String,
        district: String
    ): GroundWaterDistrictAssessment?
}
