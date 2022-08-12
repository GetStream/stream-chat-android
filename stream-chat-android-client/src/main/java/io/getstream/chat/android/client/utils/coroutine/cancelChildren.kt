package io.getstream.chat.android.client.utils.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

internal fun CoroutineContext.cancelChildrenExcept(exclude: Job?, cause: CancellationException? = null) {
    this[Job]?.children?.forEach {
        if (exclude != it) {
            it.cancel(cause)
        }
    }
}