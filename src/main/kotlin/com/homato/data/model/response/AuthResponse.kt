package com.homato.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String
)
