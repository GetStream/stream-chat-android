package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result

public class CacheAwareCall<T : Any>(
    private val originalCall: Call<T>,
    public val creationTime: Long,
    private val interval: Long,
    private val observers: MutableList<Call.Callback<T>> = mutableListOf(),
) : Call<T> {

    private var isExecuted: Boolean = false

    override fun execute(): Result<T> {
        return if (isExecutionAllowed() && !isExecuted) {
            isExecuted = true
            originalCall.execute()
        } else {
            originalCall.clone().execute()
        }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        val call = if (isExecutionAllowed()) {
            observers.add(callback)
            this
        } else {
            clone()
        }

        call.enqueue { result ->
            observers.forEach { observer ->
                observer.onResult(result)
            }

            observers.clear()
        }
    }

    override fun clone(): Call<T> {
        val clonedObservers = mutableListOf<Call.Callback<T>>().apply {
            addAll(observers)
        }

        return CacheAwareCall(
            originalCall.clone(),
            System.currentTimeMillis(),
            interval,
            clonedObservers
        )
    }

    override fun cancel() {
        originalCall.cancel()
    }

    private fun isExecutionAllowed(): Boolean =
        System.currentTimeMillis() > creationTime + interval
}
