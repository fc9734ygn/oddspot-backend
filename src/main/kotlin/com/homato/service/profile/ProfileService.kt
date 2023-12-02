package com.homato.service.profile

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.homato.data.repository.UserRepository
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
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

        val dbQuery = userRepository.changeUsername(username = username, id = id)

        return dbQuery.mapError {
            when (it) {
                is SQLException -> {
                    if (it.message?.contains("duplicate key value violates unique constraint") == true) {
                        UsernameChangeError.UsernameAlreadyExists
                    } else {
                        UsernameChangeError.UserNotFound
                    }
                }

                else -> UsernameChangeError.UserNotFound
            }
        }
    }

}