package com.homato.data.model.response.backblaze

import kotlinx.serialization.Serializable

// https://www.backblaze.com/apidocs/b2-get-upload-url
@Serializable
data class B2UploadUrlResponse(
    val bucketId: String,
    val uploadUrl: String,
    val authorizationToken: String
)