package com.kisanalert.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IndiaPostPincodeResponseDto(
    @SerialName("Status") val status: String,
    @SerialName("Message") val message: String,
    @SerialName("PostOffice") val postOffice: List<IndiaPostOfficeDto>? = null
)

@Serializable
data class IndiaPostOfficeDto(
    @SerialName("Name") val name: String,
    @SerialName("District") val district: String,
    @SerialName("State") val state: String,
    @SerialName("Block") val block: String? = null,
    @SerialName("Pincode") val pincode: String
)
