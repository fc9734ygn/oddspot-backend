package com.homato.service.authentication.hashing

data class SaltedHash(
    val hash: String,
    val salt: String
)
