package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class DoOnStartCall<T : Any>(
    private val originalCall: Call<T>,
    private val scope: CoroutineScope,
    private val sideEffect: suspend () -> Unit,
) : Call<T> {

    private var job: Job? = null

    override fun execute(): Result<T> = runBlocking {
        sideEffect()
        originalCall.execute()
    }

    override fun enqueue(callback: Call.Callback<T>) {
        job = scope.launch {
            sideEffect()
            originalCall.enqueue(callback)
        }
    }

    override fun cancel() {
        job?.cancel()
    }
}