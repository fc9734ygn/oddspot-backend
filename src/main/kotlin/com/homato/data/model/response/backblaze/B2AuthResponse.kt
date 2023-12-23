package com.homato.data.model.response.backblaze

import kotlinx.serialization.Serializable

// https://www.backblaze.com/apidocs/b2-authorize-account
@Serializable
data class BackBlazeAuthResponse(
    val accountId: String,
    val authorizationToken: String,
    val allowed: Allowed,
    val apiUrl: String,
    val downloadUrl: String,
    val recommendedPartSize: Int,
    val absoluteMinimumPartSize: Int,
    val s3ApiUrl: String
)

@Serializable
data class Allowed(
    val capabilities: List<String>,
    val bucketId: String?,
    val bucketName: String?,
    val namePrefix: String?
)