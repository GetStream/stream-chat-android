package io.getstream.chat.android.livedata.utils

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

public interface Call2<T : Any> {

    @WorkerThread
    public fun execute(): Result<T>

    @UiThread
    public fun enqueue(callback: (Result<T>) -> Unit = {})

    public fun cancel()
}

internal class CallImpl2<T : Any>(
    var runnable: suspend () -> Result<T>,
    var scope: CoroutineScope = GlobalScope
) : Call2<T> {

    var canceled: Boolean = false

    override fun cancel() {
        canceled = true
    }

    override fun execute(): Result<T> {
        return runBlocking(scope.coroutineContext) { runnable() }
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        scope.launch(scope.coroutineContext) {
            if (!canceled) {
                val result = runnable()
                callback(result)
            }
        }
    }
}
