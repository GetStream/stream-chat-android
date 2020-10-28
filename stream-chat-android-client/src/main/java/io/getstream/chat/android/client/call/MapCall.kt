package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result

internal class MapCall<T: Any, K: Any>(
    private val call: Call<T>,
    private val mapper: (T) -> K
) : ChatCallImpl<K>() {
    override fun execute(): Result<K> {
        val resultA = call.execute()

        return if (resultA.isSuccess) {
            val data = mapper(resultA.data())
            nextHandler?.invoke(data)
            Result(data, null)
        } else {
            val error = resultA.error()
            errorHandler?.invoke(error)
            Result(null, error)
        }
    }

    override fun enqueue(callback: (Result<K>) -> Unit) {
        call.enqueue {
            if (!canceled) {
                if (it.isSuccess) {
                    val data = mapper(it.data())
                    nextHandler?.invoke(data)
                    callback(Result(data, null))
                } else {
                    val error = it.error()
                    errorHandler?.invoke(error)
                    callback(Result(null, error))
                }
            }
        }
    }
}
