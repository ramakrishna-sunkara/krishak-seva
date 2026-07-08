package com.kisanalert.domain.model

data class PostOfficeLocation(
    val name: String,
    val district: String,
    val state: String,
    val block: String,
    val pincode: String
)

data class PincodeLookupResult(
    val pincode: String,
    val postOffices: List<PostOfficeLocation>,
    val districts: List<String>
)
