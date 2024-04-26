package com.homato.service.profile

import com.github.michaelbull.result.*
import com.homato.BACKBLAZE_SPOT_AVATAR_IMAGE_BUCKET_ID
import com.homato.data.repository.FileRepository
import com.homato.data.repository.UserRepository
import com.homato.util.Environment
import io.ktor.http.*
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

@Singleton
class ProfileService(
    private val userRepository: UserRepository,
    private val usernameValidator: UsernameValidator,
    private val fileRepository: FileRepository,
    private val environment: Environment
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

    suspend fun deleteAccount(userId: String): Result<Unit, Throwable> {
        return userRepository.deleteAccount(userId)
    }

    suspend fun changeAvatar(
        userId: String,
        filePath: String,
        fileContentType: ContentType
    ): Result<String, Throwable> {

        val url = fileRepository.uploadImageToBucket(
            filePath,
            fileContentType,
            environment.getVariable(BACKBLAZE_SPOT_AVATAR_IMAGE_BUCKET_ID)
        ).getOrElse {
            return Err(it)
        }

        return userRepository.changeAvatar(
            userId = userId,
            url = url
        ).map {
            url
        }
    }
}