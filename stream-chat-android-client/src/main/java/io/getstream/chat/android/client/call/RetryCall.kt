package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.retry.CallRetryService
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * A wrapper around [Call] that allows retrying the original call based on [io.getstream.chat.android.client.utils.retry.RetryPolicy].
 *
 * @param originalCall The original call.
 * @param scope Coroutine scope where the call should be run.
 * @param callRetryService A service responsible for retrying calls based on [io.getstream.chat.android.client.utils.retry.RetryPolicy].
 */
internal class RetryCall<T : Any>(
    private val originalCall: Call<T>,
    private val scope: CoroutineScope,
    private val callRetryService: CallRetryService,
) : Call<T> {

    private var job: Job? = null

    override fun execute(): Result<T> = runBlocking {
        callRetryService.runAndRetry {
            originalCall
        }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        scope.launch {
            val result = callRetryService.runAndRetry {
                originalCall
            }
            withContext(DispatcherProvider.Main) {
                callback.onResult(result)
            }
        }
    }

    override fun cancel() {
        job?.cancel()
    }
}
