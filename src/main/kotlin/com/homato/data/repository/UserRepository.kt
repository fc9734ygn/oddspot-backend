package com.homato.data.repository

import com.homato.data.model.User

interface UserRepository {
    suspend fun getByEmail(email: String): User?
    suspend fun insertUser(email: String, passwordHash: String, salt: String): Boolean
}