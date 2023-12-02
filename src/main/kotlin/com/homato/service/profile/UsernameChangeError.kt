package com.homato.service.profile

sealed class UsernameChangeError {
    class InvalidUsername(val message: String) : UsernameChangeError()
    object UsernameAlreadyExists : UsernameChangeError()
    object UserNotFound : UsernameChangeError()
}
