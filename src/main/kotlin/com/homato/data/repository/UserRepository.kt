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

    suspend fun deleteAccount(userId: String): Result<Unit, Throwable> = withContext(Dispatchers.IO) {
        runCatching {
            database.userQueries.transaction {
                database.userQueries.createDeletedUserIfNeeded(
                    id = DELETED_USER_ID,
                    email = DELETED_USER_EMAIL,
                    username = DELETED_USER_USERNAME,
                    password_hash = DELETED_USER_PASSWORD_HASH,
                    salt = DELETED_USER_SALT
                )
                database.spotQueries.updateSpotsForDeletedUser(
                    DELETED_USER_ID,
                    userId
                )
                database.visitQueries.updateVisitsForDeletedUser(
                    DELETED_USER_ID,
                    userId
                )
                database.userQueries.deleteUser(userId)
            }
        }
    }

    companion object {
        const val DELETED_USER_ID = "deleted_user_id"
        const val DELETED_USER_EMAIL = "deleted_user_email"
        const val DELETED_USER_USERNAME = "deleted_user_username"
        const val DELETED_USER_PASSWORD_HASH = "deleted_user_password_hash"
        const val DELETED_USER_SALT = "deleted_user_salt"
    }
}