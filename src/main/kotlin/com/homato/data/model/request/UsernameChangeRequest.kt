package com.homato.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UsernameChangeRequest(
    val username: String
)
