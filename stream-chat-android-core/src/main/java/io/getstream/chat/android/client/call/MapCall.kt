package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import java.util.concurrent.atomic.AtomicBoolean

internal class MapCall<T : Any, K : Any>(
    private val call: Call<T>,
    private val mapper: (T) -> K
) : Call<K> {

    protected var canceled = AtomicBoolean(false)

    override fun cancel() {
        canceled.set(true)
    }

    override fun execute(): Result<K> {
        val resultA = call.execute()

        return if (resultA.isSuccess) {
            val data = mapper(resultA.data())
            Result(data, null)
        } else {
            val error = resultA.error()
            Result(null, error)
        }
    }

    override fun enqueue(callback: Call.Callback<K>) {
        call.enqueue callback@{
            if (canceled.get()) {
                return@callback
            }

            if (it.isSuccess) {
                val data = mapper(it.data())
                callback.onResult(Result(data, null))
            } else {
                val error = it.error()
                callback.onResult(Result(null, error))
            }
        }
    }
}
