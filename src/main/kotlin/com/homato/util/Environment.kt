package com.homato.util

import org.koin.core.annotation.Singleton

// Class that provides environmental variables
// Main reason for it is to be able to mock it in tests
@Singleton
class Environment {
    fun getVariable(key: String): String {
        return System.getenv(key) ?: throw Exception("Environment variable $key not found")
    }
}