package com.kisanalert.data.repository

import com.kisanalert.core.utils.Result
import com.kisanalert.data.remote.api.IndiaPostApi
import com.kisanalert.data.remote.dto.IndiaPostOfficeDto
import com.kisanalert.domain.model.PincodeLookupResult
import com.kisanalert.domain.model.PostOfficeLocation
import com.kisanalert.domain.repository.PincodeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PincodeRepositoryImpl @Inject constructor(
    private val indiaPostApi: IndiaPostApi
) : PincodeRepository {
    override suspend fun lookupPincode(pincode: String): Result<PincodeLookupResult> {
        return try {
            val responseList = indiaPostApi.getPostOfficesByPincode(pincode)
            val response = responseList.firstOrNull()
                ?: return Result.Error(message = "No location data found for this pincode.")
            if (response.status != "Success" || response.postOffice.isNullOrEmpty()) {
                return Result.Error(
                    message = response.message.ifBlank { "Invalid pincode. Please check and try again." }
                )
            }
            val postOffices = response.postOffice.map { office: IndiaPostOfficeDto ->
                PostOfficeLocation(
                    name = office.name,
                    district = office.district,
                    state = office.state,
                    block = office.block.orEmpty(),
                    pincode = office.pincode
                )
            }
            val districts = postOffices.map { office -> office.district }.distinct().sorted()
            Result.Success(
                PincodeLookupResult(
                    pincode = pincode,
                    postOffices = postOffices,
                    districts = districts
                )
            )
        } catch (exception: Exception) {
            Result.Error(message = "Unable to fetch location. Check internet and try again.")
        }
    }
}
