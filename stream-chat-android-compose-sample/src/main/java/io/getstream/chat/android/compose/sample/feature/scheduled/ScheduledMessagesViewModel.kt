/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.feature.scheduled

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.User
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import io.getstream.chat.android.models.Channel as ChatChannel

/**
 * Lists the current user's scheduled messages, failed first, then the ones waiting for the connection, then the others
 * by day, and runs the actions each status allows.
 * Loads them from the server while connected, unless [ChatClient.hasLoadedAllScheduledMessages] says they are already
 * loaded: after that, the local changes and the events keep the list up to date until the connection is lost.
 */
internal class ScheduledMessagesViewModel(
    private val client: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val isLoadFailed = MutableStateFlow(false)

    // Whether this screen loaded them once, so it shows them, or that there are none, until it loads again.
    private val hasLoaded = MutableStateFlow(false)
    private val channels = MutableStateFlow<Map<String, ChatChannel>>(emptyMap())
    private val _events = Channel<ScheduledMessagesEvent>(Channel.BUFFERED)

    /** The state of the scheduled messages screen. */
    val state: StateFlow<ScheduledMessagesViewState> = combine(
        client.scheduledMessages,
        combine(client.hasLoadedAllScheduledMessages, hasLoaded) { hasLoadedAll, hasLoaded ->
            hasLoadedAll || hasLoaded
        },
        isLoadFailed,
        channels,
        client.clientState.connectionState,
    ) { scheduledMessages, isKnown, isLoadFailed, channels, connectionState ->
        scheduledMessagesViewState(
            scheduledMessages = scheduledMessages,
            isKnown = isKnown,
            isLoadFailed = isLoadFailed,
            channels = channels,
            isOffline = connectionState is ConnectionState.Offline,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = scheduledMessagesViewState(
            scheduledMessages = client.scheduledMessages.value,
            isKnown = client.hasLoadedAllScheduledMessages.value,
            isLoadFailed = false,
            channels = emptyMap(),
            isOffline = client.clientState.connectionState.value is ConnectionState.Offline,
        ),
    )

    /** The current user, to name the direct message channels after their other members. */
    val currentUser: StateFlow<User?> = client.clientState.user

    /** The results of the actions, to show to the user. */
    val events: Flow<ScheduledMessagesEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(client.hasLoadedAllScheduledMessages, client.clientState.connectionState) { hasLoadedAll, state ->
                !hasLoadedAll && state == ConnectionState.Connected
            }.distinctUntilChanged().filter { it }.collect { load() }
        }
        viewModelScope.launch {
            client.scheduledMessages
                .map { scheduledMessages -> scheduledMessages.map(ScheduledMessage::cid).toSet() }
                .distinctUntilChanged()
                .collect { cids -> loadChannels(cids - channels.value.keys) }
        }
    }

    /** Loads the scheduled messages again, after they failed to load. */
    fun onRetryLoadClick() {
        load()
    }

    /** Changes the date when the scheduled message with [id] is sent. */
    fun onRescheduleClick(id: String, scheduledAt: Date) {
        change(id) { client.rescheduleMessage(id, scheduledAt) }
    }

    /** Sends the scheduled message with [id] right away. */
    fun onSendNowClick(id: String) {
        change(id, isSendNow = true) { client.sendScheduledMessageNow(id) }
    }

    /** Sends again the changes of the scheduled message with [id] that failed. */
    fun onRetryClick(id: String) {
        change(id) { client.resendScheduledMessage(id) }
    }

    /** Tells the user that a message the server is sending can't be changed. */
    fun onSendingMessageClick() {
        viewModelScope.launch { _events.send(ScheduledMessagesEvent.StillSending) }
    }

    /** Deletes the scheduled message with [id]. */
    fun onDeleteClick(id: String) {
        viewModelScope.launch {
            when (val result = client.deleteScheduledMessage(id).await()) {
                is Result.Success -> _events.send(ScheduledMessagesEvent.Deleted)
                is Result.Failure -> _events.send(ScheduledMessagesEvent.Error(result.value.message))
            }
        }
    }

    /** Loads every page, as only the last one tells [ChatClient.hasLoadedAllScheduledMessages]. */
    private fun load() {
        isLoadFailed.value = false
        viewModelScope.launch {
            var next: String? = null
            do {
                when (val result = client.queryScheduledMessages(next = next).await()) {
                    is Result.Success -> next = result.value.next
                    is Result.Failure -> {
                        isLoadFailed.value = true
                        _events.send(ScheduledMessagesEvent.Error(result.value.message))
                        return@launch
                    }
                }
            } while (next != null)
            hasLoaded.value = true
        }
    }

    /**
     * Loads the channels of the scheduled messages, for their names: a message scheduled on this device has no
     * [ScheduledMessage.channel], and a direct message channel has no name of its own.
     */
    private suspend fun loadChannels(cids: Set<String>) {
        cids.chunked(MaxChannelsPerQuery).forEach { chunk ->
            val request = QueryChannelsRequest(
                filter = Filters.`in`("cid", chunk),
                limit = chunk.size,
                messageLimit = 0,
            ).apply { watch = false }
            val result = client.queryChannels(request).await()
            if (result is Result.Success) channels.update { it + result.value.associateBy(ChatChannel::cid) }
        }
    }

    private fun change(id: String, isSendNow: Boolean = false, call: () -> Call<ScheduledMessage>) {
        viewModelScope.launch {
            when (val result = call().await()) {
                is Result.Success -> _events.send(client.answerOf(id, isSendNow))
                is Result.Failure -> _events.send(ScheduledMessagesEvent.Error(result.value.message))
            }
        }
    }
}

/**
 * The state of the scheduled messages screen.
 *
 * @param groups The scheduled messages, in groups.
 * @param isLoading Whether the scheduled messages are loading, with none to show yet.
 * @param isLoadFailed Whether the scheduled messages could not load, with none to show: the load failed, or the
 * device is offline.
 * @param isOffline Whether the device is offline, so changes wait for the connection.
 */
internal data class ScheduledMessagesViewState(
    val groups: List<ScheduledMessagesGroup>,
    val isLoading: Boolean,
    val isLoadFailed: Boolean,
    val isOffline: Boolean,
)

/**
 * Scheduled messages shown under one header.
 */
internal sealed interface ScheduledMessagesGroup {

    /** The scheduled messages of the group, by date. */
    val items: List<ScheduledMessageItemState>

    /** The messages that failed. */
    data class Failed(override val items: List<ScheduledMessageItemState>) : ScheduledMessagesGroup

    /** The messages that wait for the connection to be scheduled. */
    data class Waiting(override val items: List<ScheduledMessageItemState>) : ScheduledMessagesGroup

    /** The messages sent on the day of [date]. */
    data class Day(val date: Date, override val items: List<ScheduledMessageItemState>) : ScheduledMessagesGroup
}

/**
 * A scheduled message in the list.
 *
 * @param scheduledMessage The scheduled message.
 * @param actions The actions its status allows.
 * @param channel The channel of the message, or null until it is loaded.
 */
internal data class ScheduledMessageItemState(
    val scheduledMessage: ScheduledMessage,
    val actions: List<ScheduledMessageAction>,
    val channel: ChatChannel? = null,
)

private fun scheduledMessagesViewState(
    scheduledMessages: List<ScheduledMessage>,
    isKnown: Boolean,
    isLoadFailed: Boolean,
    channels: Map<String, ChatChannel>,
    isOffline: Boolean,
): ScheduledMessagesViewState {
    val items = scheduledMessages.map { ScheduledMessageItemState(it, it.actions, channels[it.cid]) }
    val isUnknown = scheduledMessages.isEmpty() && !isKnown
    // Nothing loads while offline.
    val cannotLoad = isLoadFailed || isOffline
    return ScheduledMessagesViewState(
        groups = scheduledMessagesGroups(items, isOffline),
        isLoading = isUnknown && !cannotLoad,
        isLoadFailed = isUnknown && cannotLoad,
        isOffline = isOffline,
    )
}

/**
 * Groups [items], which are by date: the failed ones first, then, while offline, the ones waiting for the connection,
 * then the others by the day of [TimeZone] they are sent.
 */
internal fun scheduledMessagesGroups(
    items: List<ScheduledMessageItemState>,
    isOffline: Boolean,
    timeZone: TimeZone = TimeZone.getDefault(),
): List<ScheduledMessagesGroup> {
    val (failed, others) = items.partition { it.scheduledMessage.status == ScheduledMessageStatus.Failed }
    val (waiting, pending) = others.partition {
        isOffline && it.scheduledMessage.status == ScheduledMessageStatus.PreparingToSchedule
    }
    val days = pending
        .groupBy { it.scheduledMessage.scheduledAt.startOfDay(timeZone) }
        .map { (day, dayItems) -> ScheduledMessagesGroup.Day(day, dayItems) }
    return listOfNotNull(
        ScheduledMessagesGroup.Failed(failed).takeIf { failed.isNotEmpty() },
        ScheduledMessagesGroup.Waiting(waiting).takeIf { waiting.isNotEmpty() },
    ) + days
}

private fun Date.startOfDay(timeZone: TimeZone): Date = Calendar.getInstance(timeZone).apply {
    time = this@startOfDay
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.time

private const val MaxChannelsPerQuery = 30
