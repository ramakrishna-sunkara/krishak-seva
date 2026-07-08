package com.kisanalert.domain.usecase

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.PincodeLookupResult
import com.kisanalert.domain.repository.PincodeRepository
import javax.inject.Inject

class LookupPincodeUseCase @Inject constructor(
    private val pincodeRepository: PincodeRepository
) {
    suspend fun execute(pincode: String): Result<PincodeLookupResult> {
        return pincodeRepository.lookupPincode(pincode)
    }
}
