package com.homato.data.repository

import com.github.michaelbull.result.*
import com.homato.BACKBLAZE_APPLICATION_KEY
import com.homato.BACKBLAZE_APPLICATION_KEY_ID
import com.homato.BACKBLAZE_SPOT_MAIN_IMAGE_BUCKET_ID
import com.homato.data.model.response.backblaze.B2UploadFileResponse
import com.homato.data.model.response.backblaze.B2UploadUrlResponse
import com.homato.data.model.response.backblaze.BackBlazeAuthResponse
import com.homato.data.util.constructFileUrl
import com.homato.data.util.generateUniqueFileName
import com.homato.util.sha1
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import java.io.File

@Singleton
class FileRepository(
    private val client: HttpClient
) : KoinComponent {

    suspend fun uploadSpotMainImage(
        filePath: String,
        contentType: ContentType
    ): Result<String, Throwable> {
        val authResponse = authorizeBackBlazeAccount()
            .getOrElse { return Err(it) }

        val uploadUrlResponse = getUploadUrl(
            baseApiUrl = authResponse.apiUrl,
            authorizationToken = authResponse.authorizationToken
        ).getOrElse { return Err(it) }

        val uploadFileResponse = uploadFile(
            uploadUrl = uploadUrlResponse.uploadUrl,
            authorizationToken = uploadUrlResponse.authorizationToken,
            filePath = filePath,
            contentType = contentType
        ).getOrElse { return Err(it) }

        val fileUrl = constructFileUrl(
            downloadUrl = authResponse.downloadUrl,
            fileName = uploadFileResponse.fileName
        )

        return Ok(fileUrl)
    }

    private suspend fun authorizeBackBlazeAccount(
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
            response.body<BackBlazeAuthResponse>()
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
                response.body<B2UploadUrlResponse>()
            }
        }

    private suspend fun uploadFile(
        uploadUrl: String,
        authorizationToken: String,
        filePath: String,
        contentType : ContentType
    ): Result<B2UploadFileResponse, Throwable> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(filePath)
                val fileBytes = file.readBytes()
                val sha1Checksum = fileBytes.sha1()
                val contentLength = fileBytes.size //+ sha1Checksum.toByteArray().size
                val response = client.post(uploadUrl) {
                    headers {
                        append(HttpHeaders.Authorization, authorizationToken)
                        append("X-Bz-File-Name", generateUniqueFileName())
                        append(HttpHeaders.ContentType, contentType.toString())
                        append(HttpHeaders.ContentLength, contentLength.toString())
                        append("X-Bz-Content-Sha1", sha1Checksum)
                    }
                    setBody(fileBytes)
                }
                response.body<B2UploadFileResponse>()
            }
        }
}
