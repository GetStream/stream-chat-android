package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result

internal class WrappedValueCall<T : Any>(private val value: T) : Call<T> {
    override fun execute(): Result<T> = Result(value)

    override fun enqueue(callback: (Result<T>) -> Unit) = callback(execute())

    override fun cancel() = Unit
}
