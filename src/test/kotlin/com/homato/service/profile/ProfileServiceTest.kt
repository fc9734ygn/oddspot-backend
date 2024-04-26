package com.homato.service.profile

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.homato.data.model.User
import com.homato.data.repository.FileRepository
import com.homato.data.repository.UserRepository
import com.homato.util.Environment
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState
import java.util.*

class ProfileServiceTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val usernameValidator: UsernameValidator = mockk(relaxed = true)
    private val fileRepository: FileRepository = mockk(relaxed = true)
    private val environment: Environment = mockk(relaxed = true)
    private lateinit var service: ProfileService

    @BeforeEach
    fun setUp() {
        service = ProfileService(userRepository, usernameValidator, fileRepository, environment)
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

    @Test
    fun `changeAvatar() success`() = runTest {
        coEvery { fileRepository.uploadImageToBucket(any(), any(), any(), any()) } returns Ok("url")
        coEvery { userRepository.changeAvatar(any(), any()) } returns Ok(Unit)
        coEvery { userRepository.getUserById(any()) } returns Ok(
            User(
                id = UUID.randomUUID(),
                email = "email",
                username = "username",
                passwordHash = "passwordHash",
                salt = "salt",
                avatar = "avatar"
            )
        )
        coEvery { fileRepository.deleteImageFromBucket(any()) } returns Ok(Unit)

        val result = service.changeAvatar("id", "filePath", ContentType.Image.JPEG)
        assertTrue(result is Ok)
    }

    @Test
    fun `changeAvatar() fails when file upload fails`() = runTest {
        val fileUploadException = Exception("File upload failed")
        coEvery { fileRepository.uploadImageToBucket(any(), any(), any(), any()) } returns Err(fileUploadException)
        coEvery { userRepository.changeAvatar(any(), any()) } returns Ok(Unit)
        coEvery { userRepository.getUserById(any()) } returns Ok(
            User(
                id = UUID.randomUUID(),
                email = "email",
                username = "username",
                passwordHash = "passwordHash",
                salt = "salt",
                avatar = "avatar"
            )
        )
        val result = service.changeAvatar("id", "filePath", ContentType.Image.JPEG)
        assertTrue(result is Err && result.error == fileUploadException)
    }

    @Test
    fun `changeAvatar() fails when repository fails to change avatar`() = runTest {
        val dbException = Exception("Database broke")
        coEvery { fileRepository.uploadImageToBucket(any(), any(), any(), any()) } returns Ok("url")
        coEvery { userRepository.changeAvatar(any(), any()) } returns Err(dbException)
        coEvery { userRepository.getUserById(any()) } returns Ok(
            User(
                id = UUID.randomUUID(),
                email = "email",
                username = "username",
                passwordHash = "passwordHash",
                salt = "salt",
                avatar = "avatar"
            )
        )
        coEvery { fileRepository.deleteImageFromBucket(any()) } returns Ok(Unit)


        val result = service.changeAvatar("id", "filePath", ContentType.Image.JPEG)
        assertTrue(result is Err && result.error == dbException)
    }

    @Test
    fun `changeAvatar() fails when repository fails to fetch existing user`() = runTest {
        val dbException = Exception("Database broke")
        coEvery { fileRepository.uploadImageToBucket(any(), any(), any(), any()) } returns Ok("url")
        coEvery { userRepository.changeAvatar(any(), any()) } returns Ok(Unit)
        coEvery { userRepository.getUserById(any()) } returns Err(dbException)

        val result = service.changeAvatar("id", "filePath", ContentType.Image.JPEG)
        assertTrue(result is Err && result.error == dbException)
    }

    @Test
    fun `changeAvatar() fails when repository fails to delete previous avatar image`() = runTest {
        val dbException = Exception("Database broke")
        coEvery { fileRepository.uploadImageToBucket(any(), any(), any(), any()) } returns Ok("url")
        coEvery { userRepository.changeAvatar(any(), any()) } returns Ok(Unit)
        coEvery { userRepository.getUserById(any()) } returns Ok(
            User(
                id = UUID.randomUUID(),
                email = "email",
                username = "username",
                passwordHash = "passwordHash",
                salt = "salt",
                avatar = "avatar"
            )
        )
        coEvery { fileRepository.deleteImageFromBucket(any()) } returns Err(dbException)

        val result = service.changeAvatar("id", "filePath", ContentType.Image.JPEG)
        assertTrue(result is Err && result.error == dbException)
    }

}