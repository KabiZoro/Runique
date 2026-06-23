package com.kabi.core.domain.util

typealias RootError = Error

sealed interface Result<out D, out E : RootError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : RootError>(val error: E) : Result<Nothing, E>
}

inline fun <T, E : RootError, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

fun <T, E : RootError> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return  map {  }
}

typealias EmptyResult<E> = Result<Unit, E>