package com.homato.service.util

sealed class Outcome<T, E> {
    data class Success<T, E>(val data: T) : Outcome<T, E>()
    data class Failure<T, E>(val error: E) : Outcome<T, E>()
}