package com.homato.data.repository

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.homato.Database
import com.homato.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import java.util.*

@Singleton
class UserRepository(private val database: Database) : KoinComponent {

    suspend fun getByEmail(email: String): Result<User?, Throwable> = withContext(Dispatchers.IO) {
        runCatching {
            database.userQueries.selectByEmail(email).executeAsOneOrNull()?.let {
                User(UUID.fromString(it.id), it.email, it.password_hash, it.salt)
            }
        }
    }

    suspend fun insertUser(
        email: String,
        passwordHash: String,
        salt: String
    ): Result<Unit, Throwable> = withContext(Dispatchers.IO) {
        runCatching {
            database.userQueries.insert(UUID.randomUUID().toString(), email, passwordHash, salt)
        }
    }
}