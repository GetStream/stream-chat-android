package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import java.util.concurrent.atomic.AtomicBoolean

internal class FlatMapCall<T : Any, K : Any>(
    private val call: Call<T>,
    private val mapper: (T) -> Call<K>,
) : Call<K> {

    private var canceled = AtomicBoolean(false)

    override fun execute(): Result<K> {
        val resultA = call.execute()

        return if (resultA.isSuccess) {
            return mapper(resultA.data()).execute()
        } else {
            val error = resultA.error()
            Result(error)
        }
    }

    override fun enqueue(callback: Call.Callback<K>) {
        call.enqueue callback@{
            if (canceled.get()) {
                return@callback
            }

            if (it.isSuccess) {
                val resultCall = mapper(it.data())
                resultCall.enqueue(callback)
            } else {
                val error = it.error()
                callback.onResult(Result(error))
            }
        }
    }

    override fun cancel() {
        canceled.set(true)
    }
}
