package com.homato.util

import java.security.MessageDigest

private const val ALGORITHM_SHA_1 = "SHA-1"

fun ByteArray.sha1(): String {
    val md = MessageDigest.getInstance(ALGORITHM_SHA_1)
    val digest = md.digest(this)
    return digest.joinToString("") { "%02x".format(it) }
}