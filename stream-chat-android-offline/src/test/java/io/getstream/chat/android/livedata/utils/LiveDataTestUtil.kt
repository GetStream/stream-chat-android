package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.delay
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.atomic.AtomicBoolean

internal suspend fun waitForSetUser(
    client: ChatClient,
    user: User,
    token: String,
    timeMillis: Long = 5000,
) {
    val attemptDuration = 10L
    var attempt = timeMillis / attemptDuration

    var success = AtomicBoolean(false)
    client.connectUser(user, token).enqueue {
        success.set(true)
    }
    while (!success.get() && --attempt > 0) {
        delay(attemptDuration)
        // trigger the event loop to run
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    }
}
