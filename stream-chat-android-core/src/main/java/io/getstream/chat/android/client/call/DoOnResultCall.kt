package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class DoOnResultCall<T : Any>(
    private val originalCall: Call<T>,
    private val scope: CoroutineScope,
    private val consumer: (Result<T>) -> Unit,
) : Call<T> {

    private var job: Job? = null

    override fun execute(): Result<T> {
        return originalCall.execute().also(consumer)
    }

    override fun enqueue(callback: Call.Callback<T>) {
        originalCall.enqueue { result ->
            callback.onResult(result)
            job = scope.launch { consumer(result) }
        }
    }

    override fun cancel() {
        job?.cancel()
    }
}
