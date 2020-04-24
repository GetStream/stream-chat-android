package io.getstream.chat.android.livedata.utils

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.User
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.robolectric.shadows.ShadowLooper

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getOrAwaitValue(
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

fun waitForSetUser(
    client: ChatClient,
    user: User,
    token: String,
    time: Long = 4,
    timeUnit: TimeUnit = TimeUnit.SECONDS
) {
    val latch = CountDownLatch(1)
    client.events().subscribe {
        System.out.println("event received " + it.toString())
        if (it is ConnectedEvent) {
            latch.countDown()
        }
    }
    client.setUser(user, token)
    Thread.sleep(1000)
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("setUser never completed")
    }
}
