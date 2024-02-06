package com.homato.data.model.response.backblaze

import kotlinx.serialization.Serializable

// https://www.backblaze.com/apidocs/b2-upload-file
@Serializable
data class B2UploadFileResponse(
    val accountId: String,
    val action: String,
    val bucketId: String,
    val contentLength: Long,
    val contentSha1: String?,
    val contentType: String,
    val fileId: String?,
    val fileName: String,
    val uploadTimestamp: Long
)
