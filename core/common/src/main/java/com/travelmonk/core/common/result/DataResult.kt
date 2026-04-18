package com.travelmonk.core.common.result

sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Error(val exception: Throwable, val message: String? = null) : DataResult<Nothing>()
    data object Loading : DataResult<Nothing>()
}

inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) action(data)
    return this
}

inline fun <T> DataResult<T>.onError(action: (Throwable, String?) -> Unit): DataResult<T> {
    if (this is DataResult.Error) action(exception, message)
    return this
}
