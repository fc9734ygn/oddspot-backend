package com.homato.data.repository

import com.github.michaelbull.result.*
import com.homato.BACKBLAZE_APPLICATION_KEY
import com.homato.BACKBLAZE_APPLICATION_KEY_ID
import com.homato.BACKBLAZE_SPOT_MAIN_IMAGE_BUCKET_ID
import com.homato.BACKBLAZE_SPOT_MAIN_IMAGE_BUCKET_NAME
import com.homato.data.model.response.backblaze.B2UploadFileResponse
import com.homato.data.model.response.backblaze.BackBlazeAuthResponse
import com.homato.data.model.response.backblaze.B2UploadUrlResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import java.io.File
import java.security.MessageDigest
import java.util.*

@Singleton
class FileRepository(
    private val client: HttpClient
) : KoinComponent {

    suspend fun authorizeBackBlazeAccount(
        applicationKeyId: String = System.getenv(BACKBLAZE_APPLICATION_KEY_ID),
        applicationKey: String = System.getenv(BACKBLAZE_APPLICATION_KEY)
    ): Result<BackBlazeAuthResponse, Throwable> = withContext(Dispatchers.IO) {
        runCatching {
            val authValue = "$applicationKeyId:$applicationKey".encodeBase64()
            val url = "https://api.backblazeb2.com/b2api/v2/b2_authorize_account"
            val response = client.get(url) {
                headers {
                    append(HttpHeaders.Authorization, "Basic $authValue")
                }
            }
            Json.decodeFromString<BackBlazeAuthResponse>(response.bodyAsText())
        }
    }

    private suspend fun getUploadUrl(
        bucketId: String = System.getenv(BACKBLAZE_SPOT_MAIN_IMAGE_BUCKET_ID),
        baseApiUrl: String,
        authorizationToken: String
    ): Result<B2UploadUrlResponse, Throwable> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "$baseApiUrl/b2api/v2/b2_get_upload_url"
                val response: HttpResponse = client.get(url) {
                    headers {
                        append(HttpHeaders.Authorization, authorizationToken)
                    }
                    parameter("bucketId", bucketId)
                }
                Json.decodeFromString<B2UploadUrlResponse>(response.bodyAsText())
            }
        }

    private suspend fun uploadFile(
        uploadUrl: String,
        authorizationToken: String,
        filePath: String
    ): Result<B2UploadFileResponse, Throwable> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(filePath)
                val fileBytes = file.readBytes()
                val sha1Checksum = fileBytes.sha1()
                val contentLength = fileBytes.size + sha1Checksum.toByteArray().size
                val response = client.post(uploadUrl) {
                    headers {
                        append(HttpHeaders.Authorization, authorizationToken)
                        append("X-Bz-File-Name", file.name.formatAsFileName())
                        append(HttpHeaders.ContentType, "b2/x-auto")
                        append(HttpHeaders.ContentLength, contentLength.toString())
                        append("X-Bz-Content-Sha1", sha1Checksum)
                    }
                    setBody(fileBytes)
                }
                Json.decodeFromString<B2UploadFileResponse>(response.bodyAsText())
            }
        }

    suspend fun uploadSpotMainImage(filePath: String): Result<String, Throwable> {
        val authResponse = authorizeBackBlazeAccount()
            .getOrElse { return Err(it) }

        val uploadUrlResponse = getUploadUrl(
            baseApiUrl = authResponse.apiUrl,
            authorizationToken = authResponse.authorizationToken
        ).getOrElse { return Err(it) }

        val uploadFileResponse = uploadFile(
            uploadUrl = uploadUrlResponse.uploadUrl,
            authorizationToken = uploadUrlResponse.authorizationToken,
            filePath = filePath
        ).getOrElse { return Err(it) }

        val fileUrl = constructFileUrl(
            downloadUrl = authResponse.downloadUrl,
            fileName = uploadFileResponse.fileName
        )

        return Ok(fileUrl)
    }

    fun generateUniqueFileName(): String {
        // Generate a UUID and convert it to a string
        var formattedName = UUID.randomUUID().toString()

        // Apply the same formatting rules
        formattedName = formattedName
            .replace(Regex("[\u0000-\u001F\u007F\\\\]"), "") // Remove disallowed characters
            .trim('/') // Remove leading and trailing '/'

        while (formattedName.contains("//")) {
            formattedName = formattedName.replace("//", "/") // Replace double '//' with single '/'
        }

        // Ensure the name is within the 1024-byte limit by truncating if necessary
        val byteLimit = 1024
        val nameBytes = formattedName.toByteArray()
        if (nameBytes.size > byteLimit) {
            formattedName = String(nameBytes, 0, byteLimit, Charsets.UTF_8)
        }

        return formattedName
    }


    fun ByteArray.sha1(): String {
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(this)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun constructFileUrl(
        downloadUrl: String,
        bucketName: String = System.getenv(BACKBLAZE_SPOT_MAIN_IMAGE_BUCKET_NAME),
        fileName: String
    ): String {
        return "$downloadUrl/file/$bucketName/$fileName"
    }

}
