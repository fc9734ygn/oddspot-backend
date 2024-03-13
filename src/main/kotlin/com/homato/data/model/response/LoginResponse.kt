package com.homato.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val id: String,
    val username: String,
    val imageUrl: String?,
    val email: String,
    val jwt: String,
)
