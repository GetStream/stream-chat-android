package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * A wrapper around [Call] that allows to swallow the error and emit another data from a function.
 */
internal class ReturnOnErrorCall<T : Any>(
    private val originalCall: Call<T>,
    private val scope: CoroutineScope,
    private val onErrorReturn: suspend () -> Result<T>,
) : Call<T> {

    private var job: Job? = null

    override fun execute(): Result<T> = runBlocking {
        originalCall.execute().let {
            if (it.isSuccess) it
            else onErrorReturn()
        }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        originalCall.enqueue { result ->
            if (result.isSuccess) callback.onResult(result)
            else job = scope.launch {
                val result = onErrorReturn()
                callback.onResult(result)
            }
        }
    }

    override fun cancel() {
        job?.cancel()
    }
}
