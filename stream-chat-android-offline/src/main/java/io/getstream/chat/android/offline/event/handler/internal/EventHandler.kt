package io.getstream.chat.android.offline.event.handler.internal

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.User

internal interface EventHandler {

    fun initialize(user: User)

    fun startListening()

    fun stopListening()

    suspend fun replayEventsForActiveChannels()

    @VisibleForTesting
    suspend fun handleEvent(vararg event: ChatEvent)
}