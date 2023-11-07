package com.homato.data.model

import java.util.*

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val salt: String
)
