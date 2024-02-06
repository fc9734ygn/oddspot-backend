package com.homato.util

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

inline fun <V, E> Result<V?, E>.getOrElseNotNull(transform: (E?) -> V): V {
    return when (this) {
        is Ok -> value ?: transform(null)
        is Err -> transform(error)
    }
}