package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.flatMap
import io.getstream.chat.android.client.utils.onSuccess
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class WithPreconditionCall<T : Any>(
    private val originalCall: Call<T>,
    private val scope: CoroutineScope,
    private val precondition: suspend () -> Result<Unit>,
) : Call<T> {
    private var job: Job? = null

    override fun execute(): Result<T> = runBlocking {
        val preconditionResult = precondition.invoke()
        return@runBlocking preconditionResult.flatMap { originalCall.execute() }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        job = scope.launch {
            val result = precondition.invoke()
                .onSuccess { originalCall.enqueue(callback) }
            if (result.isError) {
                withContext(DispatcherProvider.Main) {
                    callback.onResult(Result.error(result.error()))
                }
            }
        }
    }

    override fun cancel() {
        job?.cancel()
    }
}
