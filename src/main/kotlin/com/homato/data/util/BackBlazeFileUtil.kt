package com.homato.data.util

import com.homato.BACKBLAZE_SPOT_MAIN_IMAGE_BUCKET_NAME
import java.util.*

fun generateUniqueFileName(): String {
    // https://www.backblaze.com/docs/cloud-storage-files
    var formattedName = UUID.randomUUID().toString()
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

fun constructFileUrl(
    downloadUrl: String,
    bucketName: String = System.getenv(BACKBLAZE_SPOT_MAIN_IMAGE_BUCKET_NAME),
    fileName: String
): String {
    return "$downloadUrl/file/$bucketName/$fileName"
}