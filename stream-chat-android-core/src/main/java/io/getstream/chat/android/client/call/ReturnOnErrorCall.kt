package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * A wrapper around [Call] that swallows the error and emits new data from [onErrorReturn].
 */
public class ReturnOnErrorCall<T : Any>(
    private val originalCall: Call<T>,
    private val scope: CoroutineScope,
    private val onErrorReturn: suspend (originalError: ChatError) -> Result<T>,
) : Call<T> {

    private var job: Job? = null

    override fun execute(): Result<T> = runBlocking {
        originalCall.execute().let {
            if (it.isSuccess) it
            else onErrorReturn(it.error())
        }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        originalCall.enqueue { originalResult ->
            if (originalResult.isSuccess) callback.onResult(originalResult)
            else job = scope.launch {
                val result = onErrorReturn(originalResult.error())
                withContext(DispatcherProvider.Main) {
                    callback.onResult(result)
                }
            }
        }
    }

    override fun cancel() {
        job?.cancel()
    }
}
