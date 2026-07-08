package com.kisanalert.domain.usecase

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.DashboardData
import com.kisanalert.domain.repository.DashboardRepository
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend fun execute(forceRefresh: Boolean = false): Result<DashboardData> {
        val userId = getCurrentUserIdUseCase.execute()
            ?: return Result.Error(message = "User session not found. Please sign in again.")
        return dashboardRepository.refreshDashboard(userId)
    }
}
