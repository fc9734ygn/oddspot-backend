package com.homato.service.profile

object UsernameValidator {

    private const val minLength = 4
    private const val maxLength = 15
    private val allowedCharacters = Regex("^[a-zA-Z0-9_]*$") // Only letters, digits, and underscores

    fun validate(username: String): String? = when {
        username.length < minLength -> {
            "Username too short. Must be at least $minLength characters."
        }

        username.length > maxLength -> {
            "Username too long. Must be no more than $maxLength characters."
        }

        !username.matches(allowedCharacters) -> {
            "Username contains invalid characters. Only letters, digits, and underscores are allowed."
        }

        else -> null
    }
}