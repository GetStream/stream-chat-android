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

package io.getstream.chat.android.ui.common.feature.scheduled

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.state.getMessageUsingCache
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerFailedPublishing
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageAction
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageItemState
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageListEvent
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageListState
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import kotlinx.coroutines.channels.Channel as EventChannel

/**
 * Controller of a list of the current user's scheduled messages: all of them, or the ones of a channel. It loads them
 * when connected, follows their changes, and reschedules, sends now and deletes them.
 *
 * @param cid The channel of the scheduled messages, in the format `type:id`, or null for every channel.
 * @param chatClient The [ChatClient] holding the scheduled messages.
 * @param scope The [CoroutineScope] the controller's work runs under, in a child scope that [close] cancels. The given
 * scope itself is never cancelled.
 * @param getMessage Loads a message by its ID, from the local cache first, for the messages the scheduled messages
 * quote.
 */
@InternalStreamChatApi
public class ScheduledMessageListController(
    private val cid: String?,
    private val chatClient: ChatClient = ChatClient.instance(),
    scope: CoroutineScope = CoroutineScope(DispatcherProvider.Main),
    private val getMessage: (id: String) -> Call<Message> = chatClient::getMessageUsingCache,
) : AutoCloseable {

    private val ownScope = CoroutineScope(scope.coroutineContext + SupervisorJob(scope.coroutineContext[Job]))
    private val isLoadFailed = MutableStateFlow(false)

    // Whether they were all loaded once, by this controller or before it, so the list shows them, or that there are
    // none, also once the connection is lost.
    private val hasLoaded = MutableStateFlow(cid == null && chatClient.hasLoadedAllScheduledMessages.value)
    private val channels = MutableStateFlow<Map<String, Channel>>(emptyMap())
    private val quotedMessages = MutableStateFlow<Map<String, Message>>(emptyMap())

    // The messages deleted from this list, which leave it as asked, not as gone.
    private val deletedIds = mutableSetOf<String>()
    private val _events = EventChannel<ScheduledMessageListEvent>(EventChannel.BUFFERED)

    private val scheduledMessages: Flow<List<ScheduledMessage>> = chatClient.scheduledMessages
        .map { scheduledMessages -> scheduledMessages.filter { cid == null || it.cid == cid } }
        .distinctUntilChanged()

    // Only a query of every channel can tell that the client knows them all.
    private val hasLoadedAll: Flow<Boolean> =
        if (cid == null) chatClient.hasLoadedAllScheduledMessages else flowOf(false)

    /**
     * The state of the list.
     */
    public val state: StateFlow<ScheduledMessageListState> = combine(
        scheduledMessages,
        combine(hasLoadedAll, hasLoaded) { hasLoadedAll, hasLoaded -> hasLoadedAll || hasLoaded },
        combine(isLoadFailed, chatClient.clientState.connectionState) { isLoadFailed, connectionState ->
            // Nothing loads while offline.
            isLoadFailed || connectionState is ConnectionState.Offline
        },
        channels,
        quotedMessages,
    ) { scheduledMessages, isKnown, cannotLoad, channels, quotedMessages ->
        val isUnknown = scheduledMessages.isEmpty() && !isKnown
        ScheduledMessageListState(
            items = scheduledMessages
                .sortedWith(compareBy(ScheduledMessage::scheduledAt).thenBy { it.message.text })
                .map { scheduledMessage -> scheduledMessage.toItemState(channels, quotedMessages) },
            isLoading = isUnknown && !cannotLoad,
            isLoadFailed = isUnknown && cannotLoad,
        )
    }.stateIn(scope = ownScope, started = SharingStarted.Eagerly, initialValue = ScheduledMessageListState())

    /**
     * The results of the actions to tell the user.
     */
    public val events: Flow<ScheduledMessageListEvent> = _events.receiveAsFlow()

    init {
        ownScope.launch {
            // Loads again on each reconnection, as the events of other devices can be missed while offline.
            combine(hasLoadedAll, chatClient.clientState.connectionState) { hasLoadedAll, connectionState ->
                !hasLoadedAll && connectionState == ConnectionState.Connected
            }.distinctUntilChanged().filter { it }.collect { load() }
        }
        ownScope.launch {
            scheduledMessages
                .map { scheduledMessages -> scheduledMessages.map(ScheduledMessage::cid).toSet() }
                .distinctUntilChanged()
                .collect { cids -> loadChannels(cids - channels.value.keys) }
        }
        ownScope.launch {
            scheduledMessages
                .map { scheduledMessages -> scheduledMessages.mapNotNull { it.message.replyMessageId }.toSet() }
                .distinctUntilChanged()
                .collect { ids -> (ids - quotedMessages.value.keys).forEach { loadQuotedMessage(it) } }
        }
    }

    /**
     * Loads the scheduled messages, for example again after they failed to load.
     */
    public fun load() {
        isLoadFailed.value = false
        ownScope.launch {
            // Loads every page, as the list shows all of them.
            var next: String? = null
            do {
                when (val result = chatClient.queryScheduledMessages(cid = cid, next = next).await()) {
                    is Result.Success -> next = result.value.next
                    is Result.Failure -> {
                        isLoadFailed.value = true
                        return@launch
                    }
                }
            } while (next != null)
            hasLoaded.value = true
        }
    }

    /**
     * Changes the date the scheduled message with [id] is sent at. When the server answers that the message was
     * already sent or deleted, it leaves the list and [ScheduledMessageListEvent.MessageGone] is sent.
     *
     * @param id The ID of the scheduled message.
     * @param scheduledAt The new date.
     */
    public fun reschedule(id: String, scheduledAt: Date) {
        ownScope.launch {
            when (val result = chatClient.rescheduleMessage(id, scheduledAt).await()) {
                is Result.Success -> if (isGoneOnServer(id)) _events.send(ScheduledMessageListEvent.MessageGone)
                is Result.Failure -> onRefused(id, result.value)
            }
        }
    }

    /**
     * Sends the scheduled message with [id] right away.
     *
     * @param id The ID of the scheduled message.
     */
    public fun sendNow(id: String) {
        ownScope.launch {
            val result = chatClient.sendScheduledMessageNow(id).await()
            if (result is Result.Failure) onRefused(id, result.value)
        }
    }

    /**
     * Deletes the scheduled message with [id].
     *
     * @param id The ID of the scheduled message.
     */
    public fun delete(id: String) {
        deletedIds += id
        ownScope.launch {
            val result = chatClient.deleteScheduledMessage(id).await()
            if (result is Result.Failure) onRefused(id, result.value)
        }
    }

    /**
     * Cancels the work started by this controller, without cancelling the scope it was given.
     */
    override fun close() {
        ownScope.cancel()
    }

    /**
     * Waits for the answer of the server to the change of the scheduled message with [id], which is saved locally
     * first, and tells whether the message left the list without being deleted from it.
     */
    private suspend fun isGoneOnServer(id: String): Boolean {
        val answered = chatClient.scheduledMessages
            .map { scheduledMessages -> scheduledMessages.find { it.id == id } }
            .first { it?.status != ScheduledMessageStatus.PreparingToSchedule }
        return answered == null && id !in deletedIds
    }

    /**
     * Tells why the action on the scheduled message with [id] was refused: the message is no longer there, it quotes
     * a deleted message, or [error].
     */
    private suspend fun onRefused(id: String, error: Error) {
        val scheduledMessage = chatClient.scheduledMessages.value.find { it.id == id }
        val replyMessageId = scheduledMessage?.message?.replyMessageId
        when {
            scheduledMessage == null -> _events.send(ScheduledMessageListEvent.MessageGone)
            // The device refuses a quote of a deleted message: once the quote is loaded again, the row says so.
            replyMessageId != null && loadQuotedMessage(replyMessageId)?.deletedAt != null -> Unit
            else -> _events.send(ScheduledMessageListEvent.ActionFailed(error))
        }
    }

    /**
     * Loads the channels with [cids], for their names and images: a message scheduled on this device has no
     * [ScheduledMessage.channel], and a direct message channel has no name of its own.
     */
    private suspend fun loadChannels(cids: Set<String>) {
        cids.chunked(MAX_CHANNELS_PER_QUERY).forEach { chunk ->
            val request = QueryChannelsRequest(
                filter = Filters.`in`("cid", chunk),
                limit = chunk.size,
                messageLimit = 0,
            ).apply { watch = false }
            val result = chatClient.queryChannels(request).await()
            if (result is Result.Success) channels.update { it + result.value.associateBy(Channel::cid) }
        }
    }

    /**
     * Loads the message with [id] that a scheduled message quotes, for its author and to tell whether it was deleted:
     * a scheduled message from the server has the ID of the quoted message only.
     */
    private suspend fun loadQuotedMessage(id: String): Message? {
        val message = getMessage(id).await().getOrNull() ?: return null
        quotedMessages.update { it + (id to message) }
        return message
    }

    private fun ScheduledMessage.toItemState(
        channels: Map<String, Channel>,
        quotedMessages: Map<String, Message>,
    ): ScheduledMessageItemState {
        val quotedMessage = message.replyMessageId?.let(quotedMessages::get) ?: message.replyTo
        return ScheduledMessageItemState(
            scheduledMessage = this,
            channel = channels[cid],
            quotedMessage = quotedMessage,
            actions = scheduledMessageActions(isQuoteDeleted = quotedMessage?.deletedAt != null),
        )
    }
}

/**
 * The actions the status of the scheduled message allows.
 *
 * @param isQuoteDeleted Whether the message quotes a deleted message, so it can only be edited or deleted.
 */
internal fun ScheduledMessage.scheduledMessageActions(isQuoteDeleted: Boolean): List<ScheduledMessageAction> {
    val actions = when (status) {
        ScheduledMessageStatus.Sending -> emptyList()
        ScheduledMessageStatus.Failed -> failure.failedActions()
        ScheduledMessageStatus.Scheduled,
        ScheduledMessageStatus.PreparingToSchedule,
        -> if (isUploading) emptyList() else PendingActions
        else -> listOf(ScheduledMessageAction.Delete)
    }
    // The device refuses to reschedule or send now a quote of a deleted message, until an edit removes the quote.
    return if (isQuoteDeleted) actions - QuoteDeletedRefusedActions else actions
}

/**
 * Whether the attachments of the scheduled message are uploading, before it is sent to the server.
 */
@InternalStreamChatApi
public val ScheduledMessage.isUploading: Boolean
    get() = status == ScheduledMessageStatus.PreparingToSchedule &&
        message.attachments.any { it.upload != null && !it.isUploaded }

/** Whether the local file of the attachment was uploaded, as the client checks it before sending the message. */
private val Attachment.isUploaded: Boolean
    get() = uploadState == Attachment.UploadState.Success && assetUrl != null

private fun ScheduledMessageFailure?.failedActions(): List<ScheduledMessageAction> = when (this) {
    // The server can't change a message it failed to send: it is edited and scheduled again as a new message.
    is ServerFailedPublishing -> if (reason == ServerFailedPublishing.Reason.ChannelDeleted) {
        listOf(ScheduledMessageAction.CopyText, ScheduledMessageAction.Delete)
    } else {
        listOf(ScheduledMessageAction.EditAndReschedule, ScheduledMessageAction.Delete)
    }
    // The server is sending the message, or it failed to send: it ends sent or failed, never editable again.
    is ServerRejectedScheduling -> if (reason in NotEditableReasons) {
        listOf(ScheduledMessageAction.Delete)
    } else {
        FailedChangeActions
    }
    // The latest changes did not reach the server: an edit or a reschedule sends them again.
    else -> FailedChangeActions
}

private val PendingActions = listOf(
    ScheduledMessageAction.Edit,
    ScheduledMessageAction.Reschedule,
    ScheduledMessageAction.SendNow,
    ScheduledMessageAction.Delete,
)

private val QuoteDeletedRefusedActions = setOf(ScheduledMessageAction.Reschedule, ScheduledMessageAction.SendNow)

private val FailedChangeActions = listOf(
    ScheduledMessageAction.Edit,
    ScheduledMessageAction.Reschedule,
    ScheduledMessageAction.Delete,
)

private val NotEditableReasons = setOf(
    ServerRejectedScheduling.Reason.ScheduledMessageSending,
    ServerRejectedScheduling.Reason.ScheduledMessageNotEditable,
)

/** The largest number of channels to load with one query. */
private const val MAX_CHANNELS_PER_QUERY = 30
