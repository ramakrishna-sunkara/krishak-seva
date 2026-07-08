package com.kisanalert.data.remote.api

import com.kisanalert.data.remote.dto.IndiaPostPincodeResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface IndiaPostApi {
    @GET("pincode/{pincode}")
    suspend fun getPostOfficesByPincode(
        @Path("pincode") pincode: String
    ): List<IndiaPostPincodeResponseDto>
}
