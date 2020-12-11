package io.getstream.chat.android.livedata.utils

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
internal fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)

    try {
        afterObserve.invoke()

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }
    } finally {
        this.removeObserver(observer)
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}

internal suspend fun waitForSetUser(
    client: ChatClient,
    user: User,
    token: String,
    timeMillis: Long = 5000
) {
    val lock = CompletableDeferred<Unit>()
    client.setUser(
        user,
        token,
        object : InitConnectionListener() {
            override fun onError(error: ChatError) {
                super.onError(error)
                lock.complete(Unit)
            }

            override fun onSuccess(data: ConnectionData) {
                super.onSuccess(data)
                lock.complete(Unit)
            }
        }
    )
    // TODO: this makes all tests very slow, someone should investigate why this doesn't work properly
    // Workaround to have `setUser()` process completed ¯\_(ツ)_/¯
    delay(timeMillis)
    // trigger the event loop to run
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    withTimeout(timeMillis) { lock.await() }
}
