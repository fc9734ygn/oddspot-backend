package com.homato.util

import java.security.MessageDigest


fun ByteArray.sha1(): String {
    val md = MessageDigest.getInstance("SHA-1")
    val digest = md.digest(this)
    return digest.joinToString("") { "%02x".format(it) }
}