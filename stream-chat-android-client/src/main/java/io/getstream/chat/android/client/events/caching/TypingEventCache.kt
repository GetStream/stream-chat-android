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

package io.getstream.chat.android.client.events.caching

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.caching.TypingEventKeyValue.Companion.toStartTypingEventKeyValue
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Caches [TypingStartEvent]s and automatically "cleans" stale ones
 * after [cleanStaleEventMs] has elapsed by sending out a [TypingStopEvent]
 * counterpart of the original event.
 *
 * @param coroutineScope The coroutine scope used for timed cleaning
 * of stale jobs.
 * @param cleanStaleEventMs The time it takes before a [TypingStartEvent] is
 * considered stale and should be "cleaned".
 * @param onEventFired The lambda called after a typing event has been processed
 * and cached if needed.
 */
internal class TypingEventCache(
    private val coroutineScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
    private val cleanStaleEventMs: Long = EphemeralStartTypingEvent.DEFAULT_DELAY_TIME_MS,
    private var onEventFired: (event: ChatEvent) -> Unit,
) : ChatEventCache {

    /**
     *  A list of currently typing users. Only [EphemeralStartTypingEvent]s
     *  are stored here, which are by design self "cleaning" [TypingStartEvent]s.
     *
     *  [TypingStopEvent] are not stored, but their processing removes their
     *  [EphemeralStartTypingEvent] counterpart.
     */
    private val currentlyTypingUsers = mutableMapOf<TypingEventKeyValue, EphemeralStartTypingEvent>()

    /**
     * Processes the incoming chat event accordingly.
     *
     * @param event The chat event to be processed.
     */
    override fun processEvent(event: ChatEvent) {
        if (event is TypingStartEvent) {
            processTypingStartEvent(event)
        } else if (event is TypingStopEvent) {
            processTypingStopEvent(event)
        }
    }

    /**
     * Processes chat events by stopping the old "cleaning" job held
     * by the previous value stored in the map under the same key
     * and starts a new timed "cleaning" job.
     *
     * @param typingStartEvent The typing start event to be processed.
     */
    private fun processTypingStartEvent(typingStartEvent: TypingStartEvent) {
        val keyValue = typingStartEvent.toStartTypingEventKeyValue()

        // Cancel the previous timed death
        currentlyTypingUsers.getOrDefault(keyValue, null)?.job?.cancel()

        currentlyTypingUsers[keyValue] = EphemeralStartTypingEvent(
            coroutineScope = coroutineScope,
            typingStartEvent = typingStartEvent
        ) { typingStopEvent -> processTypingStopEvent(typingStopEvent) }

        // Signal that the event has been processed
        onEventFired(typingStartEvent)
    }

    /**
     * Cancels the old "cleaning" job held by the previous
     * value stored in the map under the same key and removes it.
     *
     * @param typingStopEvent The typing stop event to be processed.
     */
    private fun processTypingStopEvent(typingStopEvent: TypingStopEvent) {
        val keyValue = typingStopEvent.toStartTypingEventKeyValue()

        // Cancel the previous timed death
        currentlyTypingUsers.getOrDefault(keyValue, null)?.job?.cancel()

        currentlyTypingUsers.remove(keyValue)

        // Signal that the event has been processed
        onEventFired(typingStopEvent)
    }

    /**
     * Sets on event fired.
     *
     * @param onEvent The new lambda designed to listen to events being fired.
     */
    override fun setOnEventFired(onEvent: (event: ChatEvent) -> Unit) {
        onEventFired = onEvent
    }
}

/**
 * Used to provide key value equality for distinct typing events.
 * A single value such as a [User] is not a good qualifier
 * because a single user could type in multiple channels across
 * the time span of a few seconds.
 *
 * @param user The user currently the typing event is tied to.
 * @param cid The channel the the typing event is tied to.
 * @param channelType The channel type the typing event is tied to.
 */
private data class TypingEventKeyValue(
    val user: User,
    val cid: String,
    val channelType: String,
) {
    companion object {

        /**
         * Converts [TypingStartEvent] to [TypingEventKeyValue].
         */
        fun TypingStartEvent.toStartTypingEventKeyValue() = TypingEventKeyValue(
            user = this.user,
            cid = this.cid,
            channelType = this.channelType
        )

        /**
         * Converts [TypingStopEvent] to [TypingEventKeyValue].
         */
        fun TypingStopEvent.toStartTypingEventKeyValue() = TypingEventKeyValue(
            user = this.user,
            cid = this.cid,
            channelType = this.channelType
        )
    }
}

/**
 * Essentially a [TypingStartEvent] wrapper that automatically
 * initiates event death after a certain period of time by firing
 * off a [TypingStopEvent] counterpart of the original event
 * using [onDeath].
 *
 * @param coroutineScope The coroutine scope used for timed cleaning
 * of stale jobs. Does not use a default value by design because the job
 * it performs is simple and short lived.
 * @param typingStartEvent The event that needs to be wrapped and "cleaned"
 * after a period of time.
 * @param delayTimeMs The period of time it takes before the event is "cleaned".
 * @param onDeath The lambda called when the stale typing event is "cleaned".
 */
private data class EphemeralStartTypingEvent(
    private val coroutineScope: CoroutineScope,
    private val typingStartEvent: TypingStartEvent,
    private val delayTimeMs: Long = DEFAULT_DELAY_TIME_MS,
    private val onDeath: (TypingStopEvent) -> Unit,
) {

    /**
     * The current job.
     * Cancel it before removing an instance.
     */
    var job: Job? = null

    /**
     * Starts the "cleaning" job.
     */
    init {
        job = coroutineScope.launch {
            delay(delayTimeMs)
            onDeath(typingStartEvent.toTypingStopEvent())
        }
    }

    companion object {

        /**
         * The default time period before a [TypingStartEvent] is considered
         * stale and needs to be "cleaned".
         */
        const val DEFAULT_DELAY_TIME_MS = 7000L
    }
}

/**
 * Used for manually sending a stop typing event after the
 * LRU cache timeout has expired.
 */
private fun TypingStartEvent.toTypingStopEvent() = TypingStopEvent(
    type = this.type,
    createdAt = this.createdAt,
    user = this.user,
    cid = this.cid,
    channelType = this.channelType,
    channelId = this.channelId,
    parentId = this.parentId
)
