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

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A class used to keep track of typing events
 * and remove stale ones that were not explicitly stopped by the sender
 * due to technical difficulties. (e.g. process death, loss of Internet
 * connection)
 *
 * @property coroutineScope The scope used to launch jobs.
 * @param delayTimeMs The period of time it takes before an individual stale
 * typing event is removed.
 */
internal class TypingEventPruner(
    private val coroutineScope: CoroutineScope,
    private val delayTimeMs: Long = DEFAULT_DELAY_TIME_MS,
) {

    /**
     * The id of the channel for which the typing
     * events are being pruned.
     */
    private lateinit var channelId: String

    /**
     * Triggered when typing events have been updated.
     *
     * Use [setOnTypingEventsUpdatedListener] to set a listener externally.
     */
    private var onUpdated: (
        rawTypingEvents: Map<String, TypingStartEvent>,
        typingEvent: TypingEvent,
    ) -> Unit = { _, _ -> }

    /**
     * A mutable map of typing events.
     *
     * Each event is capable of timed death using [TimedTypingStartEvent.removeTypingEvent]
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val typingEvents = mutableMapOf<String, TimedTypingStartEvent>()

    /**
     * Initializes the pruner by passing in and setting
     * the ID of the channel that the pruner is responsible for.
     *
     * @param channelId The ID of the channel the pruner is responsible for.
     */
    fun initialize(channelId: String) {
        this.channelId = channelId
    }

    /**
     * Called when a typing event needs to be processed.
     *
     * Send in a null value of [typingStartEvent] to signal that
     * a typing event should be removed.
     *
     * Please take care not to send in a typing event if the currently
     * logged in user is typing.
     *
     * @param userId The ID of the user tied to the typing event.
     * @param typingStartEvent The typing event that is being processed.
     */
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

    /**
     * Adds a typing event to [typingEvents].
     *
     * It will cancel the timed death of an typing event with the same key (userId)
     * if such exists.
     *
     *
     * @param userId The ID of the user tied to the typing event.
     * @param typingStartEvent The typing event will be added to [typingEvents].
     */
    private fun addTypingEvent(userId: String, typingStartEvent: TypingStartEvent) {
        // Create a new self stopping event
        val timedTypingStartEvent = TimedTypingStartEvent(
            coroutineScope = coroutineScope,
            typingStartEvent = typingStartEvent,
            userId = userId,
            delayTimeMs = DEFAULT_DELAY_TIME_MS,
            removeTypingEvent = {
                removeTypingEvent(it)
            }
        )

        // Cancel the self stopping event you are replacing if one exists
        typingEvents.getOrDefault(userId, null)?.cancelJob()

        // Replace the old self stopping event and call
        // the updated typing events listener
        typingEvents[userId] = timedTypingStartEvent
        onUpdated(getRawTyping(), getTypingEvent())
    }

    /**
     * Removes a typing event with the given key (userId)
     * if such exists in [typingEvents].
     *
     * @param userId The ID of the user tied to the typing event.
     */
    private fun removeTypingEvent(userId: String) {
        typingEvents.getOrDefault(userId, null)?.cancelJob()

        typingEvents.remove(userId)
        onUpdated(getRawTyping(), getTypingEvent())
    }

    /**
     * Produces an up to date raw typing map.
     *
     * Used to set a value to
     * [io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState.rawTyping].
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getRawTyping(): Map<String, TypingStartEvent> = typingEvents.mapValues { it.value.typingStartEvent }

    /**
     * Produces an up to date [TypingEvent] instance.
     *
     * Used to set a value to
     * [io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState.typing].
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getTypingEvent(): TypingEvent = typingEvents.values
        .map { it.typingStartEvent }
        .sortedBy { typingStartEvent -> typingStartEvent.createdAt }
        .map { typingStartEvent -> typingStartEvent.user }
        .let { sortedUsers -> TypingEvent(channelId = channelId, users = sortedUsers) }

    /**
     * Sets the listener used for typing updates.
     *
     * @param onUpdated The lambda that will be called whenever the
     * list of typing events is updated.
     */
    fun setOnTypingEventsUpdatedListener(
        onUpdated: (
            rawTypingEvents: Map<String, TypingStartEvent>,
            typingEvent: TypingEvent,
        ) -> Unit,
    ) {
        this.onUpdated = onUpdated
    }

    /**
     * Clears all existing typing updates and posts
     * the resulting updated lists.
     */
    fun clear() {
        typingEvents.clear()

        onUpdated(getRawTyping(), getTypingEvent())
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
 * A [TypingStartEvent] wrapper that automatically calls [removeTypingEvent]
 * when a timer set to [delayTimeMs] elapses.
 *
 * @param coroutineScope The coroutine scope used for timed cleaning
 * of stale jobs.
 * @param typingStartEvent The event that needs to be wrapped and removed
 * after a period of time.
 * @param userId The ID of the user tied to the typing event.
 * @param delayTimeMs The period of time it takes before the event is removed.
 * @param removeTypingEvent The lambda called when the stale typing event should be removed.
 */
@InternalStreamChatApi
internal data class TimedTypingStartEvent(
    private val coroutineScope: CoroutineScope,
    internal val typingStartEvent: TypingStartEvent,
    private val userId: String,
    private val delayTimeMs: Long,
    private val removeTypingEvent: (userId: String) -> Unit,
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
            removeTypingEvent(userId)
        }
    }

    /**
     * Cancels the currently running job.
     */
    fun cancelJob() {
        job?.cancel()
    }
}
