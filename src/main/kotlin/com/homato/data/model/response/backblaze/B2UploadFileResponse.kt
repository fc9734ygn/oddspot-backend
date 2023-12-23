package com.homato.data.model.response.backblaze

import kotlinx.serialization.Serializable

// https://www.backblaze.com/apidocs/b2-upload-file
@Serializable
data class B2UploadFileResponse(
    val accountId: String,
    val action: String,
    val bucketId: String,
    val contentLength: String,
    val contentSha1: String?,
    val contentMd5: String?,
    val contentType: String,
    val fileId: String?,
    val fileInfo: Map<String, String>, // Assuming a simple key-value mapping
    val fileName: String,
    // Assuming fileRetention, legalHold, and serverSideEncryption as nullable properties
//    val fileRetention: FileRetention?,
//    val legalHold: LegalHold?,
    val replicationStatus: String?,
//    val serverSideEncryption: ServerSideEncryption?,
    val uploadTimestamp: Long
)

//@Serializable
//data class FileRetention(
//    // Define properties of FileRetention based on response structure
//)
//
//@Serializable
//data class LegalHold(
//    // Define properties of LegalHold based on response structure
//)
//
//@Serializable
//data class ServerSideEncryption(
//    // Define properties of ServerSideEncryption based on response structure
//)