package io.getstream.chat.android.core.poc.library

data class Result<T>(val data: T?, val error: ClientError?) {
    fun isSuccess(): Boolean {
        return data != null
    }

    fun data(): T {
        return data!!
    }

    fun error(): ClientError {
        return error!!
    }
}