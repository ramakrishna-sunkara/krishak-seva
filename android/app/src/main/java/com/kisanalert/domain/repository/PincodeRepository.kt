package com.kisanalert.domain.repository

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.PincodeLookupResult

interface PincodeRepository {
    suspend fun lookupPincode(pincode: String): Result<PincodeLookupResult>
}
