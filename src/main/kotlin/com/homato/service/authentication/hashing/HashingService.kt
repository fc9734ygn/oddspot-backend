package com.homato.service.authentication.hashing

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import java.security.SecureRandom

@Singleton
class HashingService : KoinComponent {

    fun generateSaltedHash(value: String, saltLength: Int = DEFAULT_SALT_LENGTH): SaltedHash {
        val salt = SecureRandom.getInstance(ALGORITHM_SHA1PRNG).generateSeed(saltLength)
        val saltAsHex = Hex.encodeHexString(salt)
        val hash = DigestUtils.sha256Hex("$saltAsHex$value")
        return SaltedHash(
            hash = hash,
            salt = saltAsHex
        )
    }

    fun verify(value: String, saltedHash: SaltedHash): Boolean {
        return DigestUtils.sha256Hex(saltedHash.salt + value) == saltedHash.hash
    }

    companion object {
        private const val ALGORITHM_SHA1PRNG = "SHA1PRNG"
        private const val DEFAULT_SALT_LENGTH = 32
    }
}