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
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.UserGroup
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date

/**
 * Schedules the content of the composer of a channel or a thread, edits a scheduled message in the composer, and
 * tells the state of the scheduled messages of the channel or the thread for the banner.
 *
 * @param cid The channel, in the format `type:id`.
 * @param editedMessageId The ID of the scheduled message to edit in the composer, or null.
 * @param isScheduledAgain Whether the edited message failed to send and is scheduled again as a new message, which
 * replaces it.
 * @param resolveAttachments Copies the attachments picked in the composer to local files, so they can be uploaded.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class ChannelScheduledMessagesViewModel(
    private val cid: String,
    editedMessageId: String?,
    val isScheduledAgain: Boolean,
    private val resolveAttachments: suspend (List<Attachment>) -> List<Attachment>,
    private val client: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val parentId = MutableStateFlow<String?>(null)
    private val _editedMessage = MutableStateFlow<ScheduledMessage?>(null)
    private val _events = Channel<ScheduledMessagesEvent>(Channel.BUFFERED)
    private var isSubmitting = false

    // The names of the users the edited message mentions, to tell whether their mention is still in the text.
    private var mentionedUserNames: Map<String, String> = emptyMap()

    /** The scheduled message edited in the composer, or null. */
    val editedMessage: StateFlow<ScheduledMessage?> = _editedMessage.asStateFlow()

    /** The banner about the scheduled messages of the channel or the thread, or null while editing or with none. */
    val banner: StateFlow<ScheduledMessagesBanner?> = combine(
        parentId.flatMapLatest { client.scheduledMessages(cid, it) },
        client.clientState.connectionState,
        _editedMessage,
    ) { scheduledMessages, connectionState, editedMessage ->
        if (editedMessage == null) scheduledMessagesBanner(scheduledMessages, connectionState) else null
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Whether the device is offline, so a scheduled message waits for the connection. */
    val isOffline: StateFlow<Boolean> = client.clientState.connectionState
        .map { it is ConnectionState.Offline }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** The results of the actions, and the changes of the composer content. */
    val events: Flow<ScheduledMessagesEvent> = _events.receiveAsFlow()

    init {
        editedMessageId?.let(::startEditing)
        viewModelScope.launch {
            client.scheduledMessages.collect { scheduledMessages ->
                val editedMessage = _editedMessage.value ?: return@collect
                // The changes this view model submits end the edit themselves.
                if (isSubmitting) return@collect
                val current = scheduledMessages.find { it.id == editedMessage.id }
                when (current?.status) {
                    null -> endEditing(ScheduledMessagesEvent.EditedMessageDeleted)
                    ScheduledMessageStatus.Sending -> endEditing(ScheduledMessagesEvent.EditedMessageSent)
                    else -> _editedMessage.value = current
                }
            }
        }
    }

    /**
     * Tells the banner about the scheduled messages of the thread with [parentId], or of the channel when it is null.
     */
    fun onThreadChanged(parentId: String?) {
        this.parentId.value = parentId
    }

    /**
     * Schedules [message], built by the composer, at [scheduledAt]. When the edited message is scheduled again, the
     * new message replaces it.
     */
    fun onScheduleClick(message: Message, scheduledAt: Date) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        val replaced = _editedMessage.value?.takeIf { isScheduledAgain }
        var deleteError: Error? = null
        submit(
            endsEditing = replaced != null,
            // Told after the answer, so the snackbar of the answer does not hide it.
            afterAnswer = { deleteError?.let { _events.send(ScheduledMessagesEvent.Error(it.message)) } },
        ) {
            // A new message, with a new ID, that keeps the content of the one it replaces.
            val content = replaced?.withContentOf(message)?.copy(id = "") ?: message.withResolvedAttachments()
            client.scheduleMessage(channelType, channelId, content, scheduledAt).await().also { result ->
                // Deleted right away, as deletes are local first too, so leaving the screen does not cancel it.
                if (result is Result.Success && replaced != null) {
                    deleteError = (client.deleteScheduledMessage(replaced.id).await() as? Result.Failure)?.value
                }
            }
        }
    }

    /**
     * Saves [message], built by the composer, as the new content of the edited message, and keeps its date.
     */
    fun onSaveEditClick(message: Message) {
        val edited = _editedMessage.value ?: return
        submit { client.updateScheduledMessage(edited.id, edited.withContentOf(message)).await() }
    }

    /**
     * Saves [message], built by the composer, as the new content of the edited message, sent at [scheduledAt].
     */
    fun onRescheduleEditClick(message: Message, scheduledAt: Date) {
        val edited = _editedMessage.value ?: return
        submit { client.updateScheduledMessage(edited.id, edited.withContentOf(message), scheduledAt).await() }
    }

    /**
     * Stops editing the scheduled message, without saving the content of the composer.
     */
    fun onCancelEditClick() {
        viewModelScope.launch { endEditing() }
    }

    private fun startEditing(id: String) {
        viewModelScope.launch {
            // After a restart, the saved scheduled messages load in the background.
            val scheduledMessage = withTimeoutOrNull(EditedMessageTimeoutMs) {
                client.scheduledMessages.mapNotNull { list -> list.find { it.id == id } }.first()
            }
            if (scheduledMessage == null) {
                _events.send(ScheduledMessagesEvent.EditedMessageDeleted)
                return@launch
            }
            _editedMessage.value = scheduledMessage
            val quotedMessage = scheduledMessage.message.replyTo
                ?: scheduledMessage.message.replyMessageId?.let { client.getMessage(it).await().getOrNull() }
            mentionedUserNames = loadUserNames(scheduledMessage.message.mentionedUsersIds)
            _events.send(ScheduledMessagesEvent.EditInComposer(scheduledMessage, quotedMessage))
        }
    }

    private suspend fun endEditing(event: ScheduledMessagesEvent? = null) {
        _editedMessage.value = null
        _events.send(ScheduledMessagesEvent.ClearComposer)
        event?.let { _events.send(it) }
    }

    private suspend fun loadUserNames(ids: List<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        val request = QueryUsersRequest(filter = Filters.`in`("id", ids), offset = 0, limit = ids.size)
        return client.queryUsers(request).await().getOrNull().orEmpty().associate { it.id to it.name }
    }

    /**
     * Runs [change] and, once it is saved locally, empties the composer and ends the edit. Then tells the user the
     * answer of the server, or that the change waits for the connection, and runs [afterAnswer]. Does nothing while
     * another change runs.
     */
    private fun submit(
        endsEditing: Boolean = true,
        afterAnswer: suspend () -> Unit = {},
        change: suspend () -> Result<ScheduledMessage>,
    ) {
        if (isSubmitting) return
        isSubmitting = true
        viewModelScope.launch {
            val result = change()
            if (result is Result.Success && endsEditing) endEditing()
            isSubmitting = false
            when (result) {
                is Result.Success -> {
                    if (!endsEditing) _events.send(ScheduledMessagesEvent.ClearComposer)
                    _events.send(client.answerOf(result.value.id))
                    afterAnswer()
                }
                is Result.Failure -> _events.send(ScheduledMessagesEvent.Error(result.value.message))
            }
        }
    }

    private suspend fun Message.withResolvedAttachments(): Message = copy(attachments = resolveAttachments(attachments))

    /**
     * The content of the scheduled message with the changes of the composer, keeping its custom data. The composer
     * knows only the mentions picked while editing, so a mention of the scheduled message is kept while it is still
     * in the text.
     */
    private suspend fun ScheduledMessage.withContentOf(composed: Message): Message {
        val text = composed.text
        val keptUserIds = message.mentionedUsersIds.filter { id ->
            // A user whose name could not be loaded is kept.
            val name = mentionedUserNames[id] ?: return@filter true
            text.mentions(name.ifEmpty { id })
        }
        return message.copy(
            text = text,
            attachments = resolveAttachments(composed.attachments),
            replyMessageId = composed.replyMessageId,
            replyTo = composed.replyTo,
            showInChannel = composed.showInChannel,
            mentionedUsersIds = (keptUserIds + composed.mentionedUsersIds).distinct(),
            mentionedHere = composed.mentionedHere || message.mentionedHere && text.mentions("here"),
            mentionedChannel = composed.mentionedChannel || message.mentionedChannel && text.mentions("channel"),
            mentionedRoles = (message.mentionedRoles.filter(text::mentions) + composed.mentionedRoles).distinct(),
            mentionedGroups = (message.mentionedGroups.filter(text::mentions) + composed.mentionedGroups)
                .distinctBy(UserGroup::id),
            // A client can't send the mml of a message a backend scheduled.
            extraData = message.extraData - "mml",
        )
    }
}

/**
 * What the banner of a channel or a thread tells about its scheduled messages.
 */
internal sealed interface ScheduledMessagesBanner {

    /** The number of messages that are pending. */
    val count: Int

    /** Messages are scheduled. */
    data class Pending(override val count: Int) : ScheduledMessagesBanner

    /** Messages wait for the connection to be scheduled. */
    data class Waiting(override val count: Int) : ScheduledMessagesBanner

    /** Messages failed. */
    data class Failed(override val count: Int) : ScheduledMessagesBanner
}

/**
 * The banner for [scheduledMessages]: the failed ones first, then the ones waiting for the connection, then the
 * pending ones. Null when there are none.
 */
internal fun scheduledMessagesBanner(
    scheduledMessages: List<ScheduledMessage>,
    connectionState: ConnectionState,
): ScheduledMessagesBanner? {
    val failed = scheduledMessages.count { it.status == ScheduledMessageStatus.Failed }
    val waiting = scheduledMessages.count { it.status == ScheduledMessageStatus.PreparingToSchedule }
    val pending = scheduledMessages.size - failed
    return when {
        failed > 0 -> ScheduledMessagesBanner.Failed(failed)
        waiting > 0 && connectionState is ConnectionState.Offline -> ScheduledMessagesBanner.Waiting(waiting)
        pending > 0 -> ScheduledMessagesBanner.Pending(pending)
        else -> null
    }
}

/**
 * Whether the text mentions [group], by its name or its ID.
 */
private fun String.mentions(group: UserGroup): Boolean =
    mentions(group.id) || group.name.isNotEmpty() && mentions(group.name)

/**
 * Whether the text has "@[token]" as a whole word, like the composer writes a mention.
 */
private fun String.mentions(token: String): Boolean = Regex("@${Regex.escape(token)}(?!\\w)").containsMatchIn(this)

private const val EditedMessageTimeoutMs = 5_000L
