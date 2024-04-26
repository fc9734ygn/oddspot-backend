package com.homato.data.repository

import com.github.michaelbull.result.*
import com.homato.BACKBLAZE_APPLICATION_KEY
import com.homato.BACKBLAZE_APPLICATION_KEY_ID
import com.homato.Database
import com.homato.data.model.response.backblaze.B2UploadFileResponse
import com.homato.data.model.response.backblaze.B2UploadUrlResponse
import com.homato.data.model.response.backblaze.BackBlazeAuthResponse
import com.homato.data.util.constructFileUrl
import com.homato.data.util.generateUniqueFileName
import com.homato.util.sha1
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import java.io.File

@Singleton
class FileRepository(
    private val client: HttpClient,
    private val database: Database
) : KoinComponent {

    suspend fun uploadImageToBucket(
        filePath: String,
        contentType: ContentType,
        bucketId: String,
        bucketName: String
    ): Result<String, Throwable> {
        val authResponse = authorizeBackBlazeAccount()
            .getOrElse { return Err(it) }

        val uploadUrlResponse = getUploadUrl(
            bucketId = bucketId,
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
            fileName = uploadFileResponse.fileName,
            bucketName = bucketName
        )

        database.fileQueries.insert(
            file_id  = uploadFileResponse.fileId!!,
            url  = fileUrl,
            file_name  = uploadFileResponse.fileName,
            upload_timestamp  = uploadFileResponse.uploadTimestamp,
            bucket_id = bucketId,
        )

        return Ok(fileUrl)
    }

    suspend fun deleteImageFromBucket(
        fileUrl: String,
    ): Result<Unit, Throwable> {
        val authResponse = authorizeBackBlazeAccount()
            .getOrElse { return Err(it) }

        val file = database.fileQueries.selectByUrl(fileUrl).executeAsOne()

        deleteFile(
            fileId = file.file_id,
            authorizationToken = authResponse.authorizationToken,
            baseApiUrl = authResponse.apiUrl,
            fileName = file.file_name
        ).getOrElse { return Err(it) }

        return Ok(Unit)
    }

    private suspend fun authorizeBackBlazeAccount(
        applicationKeyId: String = System.getenv(BACKBLAZE_APPLICATION_KEY_ID),
        applicationKey: String = System.getenv(BACKBLAZE_APPLICATION_KEY)
    ): Result<BackBlazeAuthResponse, Throwable> = withContext(Dispatchers.IO) {
        runCatching {
            val authValue = "$applicationKeyId:$applicationKey".encodeBase64()
            client.get {
                url(BACKBLAZE_AUTH_URL)
                headers {
                    append(HttpHeaders.Authorization, "$BASIC_AUTH_PREFIX $authValue")
                }
            }.body<BackBlazeAuthResponse>()
        }
    }

    private suspend fun getUploadUrl(
        bucketId: String,
        baseApiUrl: String,
        authorizationToken: String
    ): Result<B2UploadUrlResponse, Throwable> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "$baseApiUrl/$UPLOAD_URL_PATH"
                client.get {
                    url(url)
                    headers {
                        append(HttpHeaders.Authorization, authorizationToken)
                    }
                    parameter("bucketId", bucketId)
                }.body<B2UploadUrlResponse>()
            }
        }

    private suspend fun uploadFile(
        uploadUrl: String,
        authorizationToken: String,
        filePath: String,
        contentType: ContentType
    ): Result<B2UploadFileResponse, Throwable> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(filePath)
                val fileBytes = file.readBytes()
                val sha1Checksum = fileBytes.sha1()
                val contentLength = fileBytes.size
                client.post {
                    url(uploadUrl)
                    headers {
                        append(HttpHeaders.Authorization, authorizationToken)
                        append(X_BZ_FILE_NAME, generateUniqueFileName())
                        append(HttpHeaders.ContentType, contentType.toString())
                        append(HttpHeaders.ContentLength, contentLength.toString())
                        append(X_BZ_CONTENT_SHA1, sha1Checksum)
                    }
                    setBody(fileBytes)
                }.body<B2UploadFileResponse>()
            }
        }

    private suspend fun deleteFile(
        fileId: String,
        authorizationToken: String,
        fileName: String,
        baseApiUrl: String,
    ): Result<Unit, Throwable> = withContext(Dispatchers.IO) {
        runCatching {
            client.post {
                url("$baseApiUrl/$DELETE_URL_PATH")
                headers {
                    append(HttpHeaders.Authorization, authorizationToken)
                }
                parameter("fileId", fileId)
                parameter("fileName", fileName)
            }
            database.fileQueries.delete(fileId)
        }
    }

    companion object {
        private const val BACKBLAZE_AUTH_URL = "https://api.backblazeb2.com/b2api/v2/b2_authorize_account"
        private const val UPLOAD_URL_PATH = "b2api/v2/b2_get_upload_url"
        private const val DELETE_URL_PATH = "b2api/v3/b2_delete_file_version"
        private const val BASIC_AUTH_PREFIX = "Basic"
        private const val X_BZ_FILE_NAME = "X-Bz-File-Name"
        private const val X_BZ_CONTENT_SHA1 = "X-Bz-Content-Sha1"
    }
}
