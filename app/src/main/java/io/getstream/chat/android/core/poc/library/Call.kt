package io.getstream.chat.android.core.poc.library

interface Call<T> {
    fun execute(): Result<T>
    fun enqueue(callback: (Result<T>) -> Unit)
    fun cancel()
    fun <K> map(mapper: (T) -> K): Call<K>
}