package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import java.util.concurrent.atomic.AtomicBoolean

internal class MapCall<T : Any, K : Any>(
    private val call: Call<T>,
    private val successMapper: (T) -> K,
    private val errorMapper: ((ChatError) -> K)? = null,
) : Call<K> {

    private var canceled = AtomicBoolean(false)

    override fun cancel() {
        canceled.set(true)
    }

    override fun execute(): Result<K> {
        val resultA = call.execute()

        return if (resultA.isSuccess) {
            val data = successMapper(resultA.data())
            Result(data)
        } else {
            val error = resultA.error()
            errorMapper?.let {
                Result(it(error))
            } ?: Result(error)
        }
    }

    override fun enqueue(callback: Call.Callback<K>) {
        call.enqueue callback@{
            if (canceled.get()) {
                return@callback
            }

            if (it.isSuccess) {
                val data = successMapper(it.data())
                callback.onResult(Result(data))
            } else {
                val error = it.error()
                callback.onResult(Result(error))
            }
        }
    }
}
