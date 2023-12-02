package com.homato.service.authentication

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.homato.data.repository.UserRepository
import com.homato.service.authentication.hashing.HashingService
import com.homato.service.authentication.hashing.SaltedHash
import com.homato.service.authentication.token.TokenClaim
import com.homato.service.authentication.token.TokenConfig
import com.homato.service.authentication.token.TokenService
import com.homato.util.getOrElseNotNull
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

@Singleton
class AuthService(
    private val hashingService: HashingService,
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val tokenConfig: TokenConfig
) : KoinComponent {

    fun getUserId(call: ApplicationCall): String? {
        val principal = call.principal<JWTPrincipal>()
        return principal?.getClaim("userId", String::class)
    }

    suspend fun register(email: String, password: String): Result<Unit, RegisterError> {
        val emailError = SignUpValidator.validateEmail(email)
        if (emailError != null) {
            return Err(RegisterError.InvalidEmail)
        }

        val passwordError = SignUpValidator.validatePassword(password)
        if (passwordError != null) {
            return Err(RegisterError.InvalidPassword)
        }

        val saltedHash = hashingService.generateSaltedHash(password)
        val result = userRepository.insertUser(
            email = email,
            username = email,
            passwordHash = saltedHash.hash,
            salt = saltedHash.salt
        )
        return result.mapError {
            RegisterError.UserAlreadyExists
        }
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