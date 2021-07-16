package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class DoOnStartCall<T : Any>(
    private val originCall: Call<T>,
    private val scope: CoroutineScope,
    private val sideEffect: () -> Unit,
) : Call<T> {

    private var job: Job? = null

    override fun execute(): Result<T> {
        sideEffect()
        return originCall.execute()
    }

    override fun enqueue(callback: Call.Callback<T>) {
        job = scope.launch {
            sideEffect()
            originCall.enqueue(callback)
        }
    }

    override fun cancel() {
        job?.cancel()
    }
}