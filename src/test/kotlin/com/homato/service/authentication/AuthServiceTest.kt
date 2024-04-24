package com.homato.service.authentication

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.homato.data.model.User
import com.homato.data.repository.UserRepository
import com.homato.service.authentication.hashing.HashingService
import com.homato.service.authentication.hashing.SaltedHash
import com.homato.service.authentication.token.TokenConfig
import com.homato.service.authentication.token.TokenService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.SQLException
import java.util.*

class AuthServiceTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val hashingService: HashingService = mockk(relaxed = true)
    private val tokenService: TokenService = mockk(relaxed = true)
    private val tokenConfig: TokenConfig = mockk(relaxed = true)
    private val usernameGenerator: UsernameGenerator = mockk(relaxed = true)
    private lateinit var service: AuthService

    @BeforeEach
    fun setUp() {
        service = AuthService(
            hashingService,
            userRepository,
            tokenService,
            tokenConfig,
            usernameGenerator
        )
    }

    @Test
    fun `register success`() = runTest {
        coEvery { usernameGenerator.generateUsername() } returns "uniqueUsername"
        coEvery { userRepository.checkExistenceByUsername(any()) } returns Ok(false)
        coEvery { userRepository.insertUser(any(), any(), any(), any(), any()) } returns Ok(Unit)

        val result = service.register("test@example.com", "StrongPassword123!")
        assertTrue(result is Ok)
    }

    @Test
    fun `register with invalid email`() = runTest {
        val result = service.register("invalid_email", "StrongPassword123!")
        assertTrue(result is Err && result.error == RegisterError.InvalidEmail)
    }

    @Test
    fun `register with invalid password`() = runTest {
        val result = service.register("test@example.com", "weak")
        assertTrue(result is Err && result.error == RegisterError.InvalidPassword)
    }

    @Test
    fun `register when user already exists`() = runTest {
        coEvery { usernameGenerator.generateUsername() } returns "uniqueUsername"
        coEvery { userRepository.checkExistenceByUsername(any()) } returns Ok(false)
        coEvery { userRepository.insertUser(any(), any(), any(), any(), any()) } returns Err(
            SQLException(
                "Unique violation",
                "23505"
            )
        )

        val result = service.register("test@example.com", "StrongPassword123!")
        assertTrue(result is Err && result.error == RegisterError.UserAlreadyExists)
    }

    @Test
    fun `register fails to create unique username`() = runTest {
        coEvery { usernameGenerator.generateUsername() } returnsMany listOf("username1", "username2")
        coEvery { userRepository.checkExistenceByUsername(any()) } returns Ok(true)

        val result = service.register("test@example.com", "StrongPassword123!")
        assertTrue(result is Err && result.error == RegisterError.Generic)
    }

    @Test
    fun `register with database error`() = runTest {
        coEvery { usernameGenerator.generateUsername() } returns "uniqueUsername"
        coEvery { userRepository.checkExistenceByUsername(any()) } returns Ok(false)
        coEvery { userRepository.insertUser(any(), any(), any(), any(), any()) } returns Err(
            SQLException(
                "Database error",
                "55000"
            )
        )

        val result = service.register("test@example.com", "StrongPassword123!")
        assertTrue(result is Err && result.error == RegisterError.Generic)
    }

    @Test
    fun `register fails due to username existence check error`() = runTest {
        coEvery { usernameGenerator.generateUsername() } returns "uniqueUsername"
        coEvery {
            userRepository.checkExistenceByUsername("uniqueUsername")
        } returns Err(Throwable("Database connectivity issue"))

        val result = service.register("test@example.com", "StrongPassword123!")
        assertTrue(result is Err)
    }

    @Test
    fun `login success`() = runTest {
        val user = User(
            id = UUID.randomUUID(),
            username = "userTest",
            passwordHash = "hashedPassword",
            salt = "salt",
            email = "test@example.com",
            avatar = null
        )
        val saltedHash = SaltedHash(hash = user.passwordHash, salt = user.salt)
        val token = "generatedToken"

        coEvery { userRepository.getByEmail("test@example.com") } returns Ok(user)
        coEvery { hashingService.verify("StrongPassword123!", saltedHash) } returns true
        coEvery { tokenService.generate(any(), any()) } returns token

        val result = service.login("test@example.com", "StrongPassword123!")
        assertTrue(result is Ok && result.value.jwt == token)
    }

    @Test
    fun `login user not found`() = runTest {
        coEvery { userRepository.getByEmail("test@example.com") } returns Err(Throwable("User not found"))

        val result = service.login("test@example.com", "StrongPassword123!")
        assertTrue(result is Err && result.error == LoginError.UserNotFound)
    }

    @Test
    fun `login invalid credentials`() = runTest {
        val user = User(
            id = UUID.randomUUID(),
            username = "userTest",
            passwordHash = "hashedPassword",
            salt = "salt",
            email = "test@example.com",
            avatar = null
        )
        val saltedHash = SaltedHash(hash = user.passwordHash, salt = user.salt)

        coEvery { userRepository.getByEmail("test@example.com") } returns Ok(user)
        coEvery { hashingService.verify("WrongPassword", saltedHash) } returns false

        val result = service.login("test@example.com", "WrongPassword")
        assertTrue(result is Err && result.error == LoginError.InvalidCredentials)
    }


}