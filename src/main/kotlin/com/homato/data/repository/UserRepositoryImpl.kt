package com.homato.data.repository

import com.homato.Database
import com.homato.data.model.User
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import java.util.*

@Singleton
class UserRepositoryImpl(private val database: Database) : UserRepository, KoinComponent {

    override suspend fun getByEmail(email: String): User? {
        database.userQueries.selectByEmail(email).executeAsOneOrNull()?.let {
            return User(UUID.fromString(it.id), it.email, it.password_hash, it.salt)
        }
        return null
    }

    override suspend fun insertUser(
        email: String,
        passwordHash: String,
        salt: String
    ): Boolean {
        return try {
            database.userQueries.insert(UUID.randomUUID().toString(), email, passwordHash, salt)
            true
        } catch (e: Exception) {
            false
        }
    }
}