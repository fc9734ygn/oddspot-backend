package com.homato.data.model

import java.util.*

data class User(
    val id: UUID,
    val email: String,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val avatar : String?
)
