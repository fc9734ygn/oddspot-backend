package com.homato.service.authentication

import com.github.michaelbull.result.*
import com.homato.data.repository.UserRepository
import com.homato.data.util.PostgreSQLErrorCode
import com.homato.service.authentication.hashing.HashingService
import com.homato.service.authentication.hashing.SaltedHash
import com.homato.service.authentication.token.TokenClaim
import com.homato.service.authentication.token.TokenConfig
import com.homato.service.authentication.token.TokenService
import com.homato.util.getOrElseNotNull
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import java.sql.SQLException

@Singleton
class AuthService(
    private val hashingService: HashingService,
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val tokenConfig: TokenConfig,
    private val usernameGenerator: UsernameGenerator
) : KoinComponent {

    suspend fun register(
        email: String,
        password: String
    ): Result<Unit, RegisterError> {
        val emailError = SignUpValidator.validateEmail(email)
        if (emailError != null) {
            return Err(RegisterError.InvalidEmail)
        }

        val passwordError = SignUpValidator.validatePassword(password)
        if (passwordError != null) {
            return Err(RegisterError.InvalidPassword)
        }

        val saltedHash = hashingService.generateSaltedHash(password)

        val username = createUniqueUsername().getOrElse {
            return Err(RegisterError.Generic)
        }

        val result = userRepository.insertUser(
            email = email,
            username = username,
            passwordHash = saltedHash.hash,
            salt = saltedHash.salt
        )

        return result.mapError {
            val isUniqueConstraintsViolation = it is SQLException &&
                    PostgreSQLErrorCode.fromCode(it.sqlState) == PostgreSQLErrorCode.UNIQUE_VIOLATION
            if (isUniqueConstraintsViolation) {
                return Err(RegisterError.UserAlreadyExists)
            } else {
                return Err(RegisterError.Generic)
            }
        }
    }

    private suspend fun createUniqueUsername(): Result<String, Throwable> {
        var attempt = 0
        val maxAttempts = UNIQUE_USERNAME_ATTEMPTS_MAX
        while (attempt < maxAttempts) {
            val username = usernameGenerator.generateUsername()
            userRepository.checkExistenceByUsername(username).fold(
                success = { alreadyExists ->
                    if (alreadyExists) {
                        attempt++
                    } else {
                        return Ok(username)
                    }
                },
                failure = {
                    return Ok(username)
                }
            )
        }
        return Err(Throwable("Failed to generate a unique username"))
    }

    suspend fun login(email: String, password: String): Result<String, LoginError> {
        val user = userRepository.getByEmail(email).getOrElseNotNull {
            return Err(LoginError.UserNotFound)
        }

        val isValidPassword = hashingService.verify(
            value = password,
            saltedHash = SaltedHash(
                hash = user.passwordHash,
                salt = user.salt
            )
        )

        if (!isValidPassword) {
            return Err(LoginError.InvalidCredentials)
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        return Ok(token)
    }

    companion object {
        const val UNIQUE_USERNAME_ATTEMPTS_MAX = 10
    }
}