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
internal class ReturnOnResultCall<T : Any>(
    private val originalCall: Call<T>,
    private val scope: CoroutineScope,
    private val onSuccessReturn: suspend (T) -> Result<T> = { data -> Result.success(data) },
    private val onErrorReturn: suspend (ChatError) -> Result<T> = { error -> Result.error(error) },
) : Call<T> {

    private var job: Job? = null

    override fun execute(): Result<T> = runBlocking {
        originalCall.execute().let {
            if (it.isSuccess) onSuccessReturn(it.data())
            else onErrorReturn(it.error())
        }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        originalCall.enqueue { originalResult ->
            job = scope.launch {
                val result = if (originalResult.isSuccess) onSuccessReturn(originalResult.data())
                else onErrorReturn(originalResult.error())
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
