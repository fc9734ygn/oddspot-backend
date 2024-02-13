package com.homato.service.profile

import com.homato.data.util.PostgreSQLErrorCode
import org.postgresql.util.PSQLException

sealed class UsernameChangeError {
    class InvalidUsername(val message: String) : UsernameChangeError()
    object UsernameAlreadyExists : UsernameChangeError()
    object UserNotFound : UsernameChangeError()
    object Generic : UsernameChangeError()
}

fun Throwable.toUsernameChangeError(): UsernameChangeError {
    return if (this is PSQLException) {
        when (PostgreSQLErrorCode.fromCode(this.sqlState)) {
            PostgreSQLErrorCode.UNIQUE_VIOLATION -> UsernameChangeError.UsernameAlreadyExists
            PostgreSQLErrorCode.FOREIGN_KEY_VIOLATION -> UsernameChangeError.UserNotFound
            else -> UsernameChangeError.Generic
        }
    } else {
        UsernameChangeError.Generic
    }
}