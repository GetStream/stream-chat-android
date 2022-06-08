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

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

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
    private val cleanStaleEventMs: Long = SelfStoppingTypingEvent.DEFAULT_DELAY_TIME_MS,
    private val onEventFired: (event: ChatEvent) -> Unit,
) : ChatEventCache {

    /**
     *  A list of currently typing users. Only [SelfStoppingTypingEvent]s
     *  are stored here, which are by design self "cleaning" [TypingStartEvent]s.
     *
     *  [TypingStopEvent] are not stored, but their processing removes their
     *  [SelfStoppingTypingEvent] counterpart.
     */
    private val _currentlyTypingUsers = mutableMapOf<String, SelfStoppingTypingEvent>()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val currentlyTypingUsers: Map<String, SelfStoppingTypingEvent> = _currentlyTypingUsers

    /**
     * Processes the incoming chat event accordingly.
     *
     * @param event The chat event to be processed.
     */
    override fun processEvent(event: ChatEvent) {
        when (event) {
            is TypingStartEvent -> {
                processTypingStartEvent(event)
            }
            is TypingStopEvent -> {
                processTypingStopEvent(event)
            }
            else -> {
                throw IllegalArgumentException(
                    "This class should be only used to cache typing events. " +
                        "All other events will remain unprocessed."
                )
            }
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
        val keyValue = typingEventToKey(typingStartEvent.user, typingStartEvent.cid)

        // Cancel the previous timed death
        _currentlyTypingUsers.getOrDefault(keyValue, null)?.cancelJob()

        _currentlyTypingUsers[keyValue] = SelfStoppingTypingEvent(
            coroutineScope = coroutineScope,
            typingStartEvent = typingStartEvent,
            delayTimeMs = cleanStaleEventMs
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
        val keyValue = typingEventToKey(typingStopEvent.user, typingStopEvent.cid)

        // Cancel the previous timed death
        _currentlyTypingUsers.getOrDefault(keyValue, null)?.cancelJob()

        _currentlyTypingUsers.remove(keyValue)

        // Signal that the event has been processed
        onEventFired(typingStopEvent)
    }

    /**
     * Cancels [coroutineScope].
     */
    fun cancel() {
        coroutineScope.cancel()
    }
}

/**
 * A small utility function that transforms [TypingStartEvent] and [TypingStopEvent]
 * into a unique templated string to be used as a key value.
 */
internal fun typingEventToKey(user: User, cid: String) = "${user.id}:$cid"

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
    private val typingStartEvent: TypingStartEvent,
    private val delayTimeMs: Long = DEFAULT_DELAY_TIME_MS,
    private val onTypingStopEvent: (TypingStopEvent) -> Unit,
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
            onTypingStopEvent(typingStartEvent.toTypingStopEvent())
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
