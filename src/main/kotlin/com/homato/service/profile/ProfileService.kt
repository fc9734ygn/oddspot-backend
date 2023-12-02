package com.homato.service.profile

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.homato.data.repository.UserRepository
import com.homato.data.util.PostgreSqlErrorCode
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.postgresql.util.PSQLException
import java.sql.SQLException

@Singleton
class ProfileService(
    private val userRepository: UserRepository
) : KoinComponent {

    suspend fun changeUsername(username: String, id: String): Result<Unit, UsernameChangeError> {

        val usernameError = UsernameValidator.validate(username)
        if (usernameError != null) {
            return Err(UsernameChangeError.InvalidUsername(usernameError))
        }

        val dbQuery = userRepository.changeUsername(
            username = username,
            id = id
        )

        return dbQuery.mapError {
            if (it is PSQLException) {
                handleSqlError(it)
            } else {
                UsernameChangeError.Generic
            }
        }
    }

    private fun handleSqlError(psqlException: PSQLException): UsernameChangeError {
        return when (PostgreSqlErrorCode.fromCode(psqlException.sqlState)) {
            PostgreSqlErrorCode.UNIQUE_VIOLATION -> UsernameChangeError.UsernameAlreadyExists
            PostgreSqlErrorCode.FOREIGN_KEY_VIOLATION -> UsernameChangeError.UserNotFound
            else -> UsernameChangeError.Generic
        }
    }
}