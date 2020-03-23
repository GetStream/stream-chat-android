package com.getstream.sdk.chat.livedata

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

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
    client.setUser(user, token, object: InitConnectionListener() {
        override fun onSuccess(data: ConnectionData) {
            System.out.println("setUser onSuccess")
        }

        override fun onError(error: ChatError) {
            System.out.println("setUser onError" + error.toString())
        }
    })
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("setUser never completed")
    }
}