/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.plugin.logic.channel.internal

import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class TypingEventPruner(val coroutineScope: CoroutineScope) {

    private lateinit var channelId: String

    private var onUpdated: (
        rawTypingEvents: Map<String, TypingStartEvent>,
        typingEvent: TypingEvent,
    ) -> Unit = { _, _ -> }

    private val typingEvents = mutableMapOf<String, SelfStoppingTypingEvent>()

    fun initialize(channelId: String) {
        this.channelId = channelId
    }

    fun processEvent(
        userId: String,
        typingStartEvent: TypingStartEvent?,
    ) {
        when (typingStartEvent) {
            null -> removeTypingEvent(userId)
            else -> addTypingEvent(
                userId = userId,
                typingStartEvent = typingStartEvent
            )
        }
    }

    private fun addTypingEvent(userId: String, typingStartEvent: TypingStartEvent) {
        val selfStoppingTypingEvent = SelfStoppingTypingEvent(
            coroutineScope = coroutineScope,
            typingStartEvent = typingStartEvent,
            userId = userId,
            removeUser = { removeTypingEvent(it) }
        )

        typingEvents[userId] = selfStoppingTypingEvent
        onUpdated(getRawTyping(), getTypingEvent())
    }

    private fun removeTypingEvent(userId: String) {
        typingEvents.getOrDefault(userId, null)?.cancelJob()

        typingEvents.remove(userId)
        onUpdated(getRawTyping(), getTypingEvent())
    }

    private fun getRawTyping(): Map<String, TypingStartEvent> = typingEvents.mapValues { it.value.typingStartEvent }

    private fun getTypingEvent(): TypingEvent = typingEvents.values
        .map { it.typingStartEvent }
        .sortedBy { typingStartEvent -> typingStartEvent.createdAt }
        .map { typingStartEvent -> typingStartEvent.user }
        .let { sortedUsers -> TypingEvent(channelId = channelId, users = sortedUsers) }

    fun onTypingEventsUpdated(
        onUpdated: (
            rawTypingEvents: Map<String, TypingStartEvent>,
            typingEvent: TypingEvent,
        ) -> Unit,
    ) {
        this.onUpdated = onUpdated
    }
}

/**
 * A [TypingStartEvent] wrapper that automatically fires
 * off a [TypingStopEvent] counterpart of the original event
 * using [onTypingStopEvent] after [delayTimeMs] has elapsed.
 *
 * @param coroutineScope The coroutine scope used for timed cleaning
 * of stale jobs. Does not use a default value by design because the job
 * it performs is simple and short lived.
 * @param typingStartEvent The event that needs to be wrapped and "cleaned"
 * after a period of time.
 * @param delayTimeMs The period of time it takes before the event is "cleaned".
 * @param onTypingStopEvent The lambda called when the stale typing event is "cleaned".
 */
@InternalStreamChatApi
internal data class SelfStoppingTypingEvent(
    private val coroutineScope: CoroutineScope,
    internal val typingStartEvent: TypingStartEvent,
    private val userId: String,
    private val delayTimeMs: Long = DEFAULT_DELAY_TIME_MS,
    private val removeUser: (userId: String) -> Unit,
) {

    /**
     * The current job.
     * Cancel it before removing an instance.
     */
    private var job: Job? = null

    /**
     * Starts the "cleaning" job.
     */
    init {
        job = coroutineScope.launch {
            delay(delayTimeMs)
            removeUser(userId)
        }
    }

    /**
     * Cancels the currently running job.
     */
    fun cancelJob() {
        job?.cancel()
    }

    companion object {

        /**
         * The default time period before a [TypingStartEvent] is considered
         * stale and needs to be "cleaned".
         */
        const val DEFAULT_DELAY_TIME_MS = 7000L
    }
}
