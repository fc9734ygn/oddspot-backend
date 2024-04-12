package com.homato.service.profile

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.homato.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState

class ProfileServiceTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val usernameValidator: UsernameValidator = mockk(relaxed = true)
    private lateinit var service: ProfileService

    @BeforeEach
    fun setUp() {
        service = ProfileService(userRepository, usernameValidator)
    }

    @Test
    fun `changeUsername() when validator gives error`() = runTest {
        every { usernameValidator.validate(any()) } returns "Error"

        val result = service.changeUsername("invalid_username", "id")
        assertTrue(result is Err && result.error is UsernameChangeError.InvalidUsername)
    }

    @Test
    fun `changeUsername() when username already exists`() = runTest {
        every { usernameValidator.validate(any()) } returns null
        coEvery { userRepository.changeUsername(any(), any()) } returns Err(
            PSQLException(
                "Unique violation",
                PSQLState.UNIQUE_VIOLATION
            )
        )

        val result = service.changeUsername("existing_username", "id")
        assertTrue(result is Err && result.error is UsernameChangeError.UsernameAlreadyExists)
    }

    @Test
    fun `changeUsername() when user doesn't exist`() = runTest {
        every { usernameValidator.validate(any()) } returns null
        coEvery { userRepository.changeUsername(any(), any()) } returns Err(
            PSQLException(
                "Foreign key violation",
                PSQLState.FOREIGN_KEY_VIOLATION
            )
        )

        val result = service.changeUsername("username", "non_existing_id")
        assertTrue(result is Err && result.error is UsernameChangeError.UserNotFound)
    }

    @Test
    fun `changeUsername() when repository gives other error`() = runTest {
        every { usernameValidator.validate(any()) } returns null
        coEvery { userRepository.changeUsername(any(), any()) } returns Err(
            PSQLException(
                "Some other exception",
                PSQLState.IO_ERROR
            )
        )


        val result = service.changeUsername("username", "id")
        assertTrue(result is Err && result.error is UsernameChangeError.Generic)
    }

    @Test
    fun `changeUsername() success`() = runTest {
        every { usernameValidator.validate(any()) } returns null
        coEvery { userRepository.changeUsername(any(), any()) } returns Ok(Unit)

        val result = service.changeUsername("new_username", "id")
        assertTrue(result is Ok)
    }

    @Test
    fun `deleteAccount() success`() = runTest {
        coEvery { userRepository.deleteAccount(any()) } returns Ok(Unit)

        val result = service.deleteAccount("id")
        assertTrue(result is Ok)
    }

    @Test
    fun `deleteAccount() failure`() = runTest {
        coEvery { userRepository.deleteAccount(any()) } returns Err(Throwable())

        val result = service.deleteAccount("id")
        assertTrue(result is Err)
    }

}