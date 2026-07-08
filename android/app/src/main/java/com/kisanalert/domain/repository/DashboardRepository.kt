package com.kisanalert.domain.repository

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.DashboardData
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeDashboard(userId: String): Flow<DashboardData?>
    suspend fun refreshDashboard(userId: String): Result<DashboardData>
}
