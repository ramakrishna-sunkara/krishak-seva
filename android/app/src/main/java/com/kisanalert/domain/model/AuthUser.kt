package com.kisanalert.domain.model

data class AuthUser(
    val userId: String,
    val phoneNumber: String? = null,
    val isAnonymous: Boolean = false
)
