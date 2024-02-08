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
                User(
                    id = UUID.fromString(it.id),
                    email = it.email,
                    username = it.username,
                    passwordHash = it.password_hash,
                    salt = it.salt
                )
            }
        }
    }

    suspend fun insertUser(
        email: String,
        username: String,
        passwordHash: String,
        salt: String
    ): Result<Unit, Throwable> = withContext(Dispatchers.IO) {
        runCatching {
            database.userQueries.insert(
                id = UUID.randomUUID().toString(),
                email = email,
                username = username,
                password_hash = passwordHash,
                salt = salt
            )
        }
    }

    suspend fun changeUsername(username: String, id: String): Result<Unit, Throwable> = withContext(Dispatchers.IO) {
        runCatching {
            database.userQueries.changeUsername(
                username = username,
                id = id
            )
        }
    }
}