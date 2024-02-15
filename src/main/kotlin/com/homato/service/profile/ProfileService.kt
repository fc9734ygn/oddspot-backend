package com.homato.service.profile

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.homato.data.repository.UserRepository
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

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
        ).mapError { it.toUsernameChangeError() }
    }

    suspend fun deleteAccount(userId: String) : Result<Unit, Throwable> {
        return userRepository.deleteAccount(userId)
    }
}