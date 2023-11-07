package com.homato.service.authentication

import com.homato.service.util.Outcome

interface  AuthService {
    suspend fun login(email: String, password: String): Outcome<String, LoginError>
    suspend fun register(email: String, password: String): Outcome<Unit, RegisterError>
}