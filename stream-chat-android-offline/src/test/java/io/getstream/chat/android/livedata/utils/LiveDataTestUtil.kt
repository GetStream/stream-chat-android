package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.robolectric.shadows.ShadowLooper

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
