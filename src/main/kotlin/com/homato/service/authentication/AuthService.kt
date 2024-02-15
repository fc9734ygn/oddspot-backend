package com.homato.service.authentication

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.fold
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

        var attempt = 0
        val maxAttempts = 5
        while (attempt < maxAttempts) {
            val username = usernameGenerator.generateUsername()
            val result = userRepository.insertUser(
                email = email,
                username = username,
                passwordHash = saltedHash.hash,
                salt = saltedHash.salt
            )

            result.fold(
                success = {
                    return Ok(Unit)
                },
                failure = {
                    if (it !is SQLException) {
                        return Err(RegisterError.Generic)
                    } else {
                        val errorCode = PostgreSQLErrorCode.fromCode(it.sqlState)
                        if (errorCode != PostgreSQLErrorCode.UNIQUE_VIOLATION) {
                            return Err(RegisterError.Generic)
                        } else {
                            // If it's a unique violation, try again with a new username
                            attempt++
                        }
                    }
                }
            )
        }

        return Err(RegisterError.UserAlreadyExists)
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
}