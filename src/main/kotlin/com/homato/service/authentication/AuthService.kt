package com.homato.service.authentication

import com.homato.data.repository.UserRepository
import com.homato.service.authentication.hashing.HashingService
import com.homato.service.authentication.hashing.SaltedHash
import com.homato.service.authentication.token.TokenClaim
import com.homato.service.authentication.token.TokenConfig
import com.homato.service.authentication.token.TokenService
import com.homato.service.util.Outcome
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

@Singleton
class AuthService(
    private val hashingService: HashingService,
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val tokenConfig: TokenConfig
) : KoinComponent {

    suspend fun register(email: String, password: String): Outcome<Unit, RegisterError> {
        val emailError = SignUpValidator.validateEmail(email)
        if (emailError != null) {
            return Outcome.Failure(RegisterError.InvalidEmail)
        }

        val passwordError = SignUpValidator.validatePassword(password)
        if (passwordError != null) {
            return Outcome.Failure(RegisterError.InvalidPassword)
        }

        val saltedHash = hashingService.generateSaltedHash(password)
        val inserted = userRepository.insertUser(
            email = email,
            passwordHash = saltedHash.hash,
            salt = saltedHash.salt
        )
        return if (inserted) Outcome.Success(Unit) else Outcome.Failure(RegisterError.UserAlreadyExists)
    }

    suspend fun login(email: String, password: String): Outcome<String, LoginError> {
        val user = userRepository.getByEmail(email) ?: return Outcome.Failure(LoginError.UserNotFound)

        val isValidPassword = hashingService.verify(
            value = password,
            saltedHash = SaltedHash(
                hash = user.passwordHash,
                salt = user.salt
            )
        )

        if (!isValidPassword) {
            return Outcome.Failure(LoginError.InvalidCredentials)
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        return Outcome.Success(token)
    }
}