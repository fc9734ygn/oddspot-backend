package com.homato.service.profile

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.homato.data.repository.UserRepository
import com.homato.data.util.PostgreSQLErrorCode
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.postgresql.util.PSQLException

@Singleton
class ProfileService(
    private val userRepository: UserRepository,
    private val usernameValidator: UsernameValidator
) : KoinComponent {

    suspend fun changeUsername(username: String, id: String): Result<Unit, UsernameChangeError> {

        val usernameError = usernameValidator.validate(username)
        if (usernameError != null) {
            return Err(UsernameChangeError.InvalidUsername(usernameError))
        }

        return userRepository.changeUsername(
            username = username,
            id = id
        ).mapError { mapChangeUsernameError(it) }
    }

    private fun mapChangeUsernameError(throwable: Throwable): UsernameChangeError {
        return if (throwable is PSQLException) {
            when (PostgreSQLErrorCode.fromCode(throwable.sqlState)) {
                PostgreSQLErrorCode.UNIQUE_VIOLATION -> UsernameChangeError.UsernameAlreadyExists
                PostgreSQLErrorCode.FOREIGN_KEY_VIOLATION -> UsernameChangeError.UserNotFound
                else -> UsernameChangeError.Generic
            }
        } else {
            UsernameChangeError.Generic
        }
    }
}