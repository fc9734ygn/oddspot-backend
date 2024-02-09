package com.homato.service.profile

import org.koin.core.component.KoinComponent

private const val MIN_LENGTH = 4
private const val MAX_LENGTH = 15

class UsernameValidator : KoinComponent {

    private val allowedCharacters = Regex("^[a-zA-Z0-9_]*$") // Only letters, digits, and underscores

    fun validate(username: String): String? = when {
        username.length < MIN_LENGTH -> {
            "Username too short. Must be at least $MIN_LENGTH characters."
        }

        username.length > MAX_LENGTH -> {
            "Username too long. Must be no more than $MAX_LENGTH characters."
        }

        !username.matches(allowedCharacters) -> {
            "Username contains invalid characters. Only letters, digits, and underscores are allowed."
        }

        else -> null
    }
}