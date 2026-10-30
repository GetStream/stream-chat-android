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

package io.getstream.chat.android.client.scheduled

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.attachment.AttachmentUploader
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ScheduledMessageCreatedEvent
import io.getstream.chat.android.client.events.ScheduledMessageDeletedEvent
import io.getstream.chat.android.client.events.ScheduledMessageFailedEvent
import io.getstream.chat.android.client.events.ScheduledMessageSentEvent
import io.getstream.chat.android.client.events.ScheduledMessageUpdatedEvent
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.scheduled.LocalScheduledMessage.PendingChange
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.QueryScheduledMessagesResult
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageQueryStatus
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

/**
 * Keeps the current user's scheduled messages, and changes them locally first: a change shows in [scheduledMessages]
 * right away, and the [ScheduledMessageSender] sends it to the server once the attachments are uploaded and the
 * device is online. Events and query results keep the local messages in line with the server.
 *
 * @param getRepositoryFacade Function to provide the [RepositoryFacade] of the connected user.
 * @param onSentAsMessage Called with the message the server sent right away, because its date had passed.
 */
internal class ScheduledMessageManager(
    private val api: ChatApi,
    attachmentUploader: AttachmentUploader,
    private val clientState: ClientState,
    private val getRepositoryFacade: () -> RepositoryFacade,
    private val now: () -> Date,
    onSentAsMessage: suspend (Message) -> Unit,
) {

    private val store = ScheduledMessageStore()
    private val validator = ScheduledMessageValidator(getRepositoryFacade, now)
    private val sender = ScheduledMessageSender(store, api, attachmentUploader, clientState, onSentAsMessage)
    private val lifecycleMutex = Mutex()
    private var senderJob: Job? = null
    private var connectionJob: Job? = null
    private val _hasLoadedAll = MutableStateFlow(false)

    // Counts the resets of [hasLoadedAll], so a query that ends after one does not set it again.
    private val hasLoadedAllResets = AtomicInteger(0)

    /** The scheduled messages to show: the pending ones first, then the failed ones, each by date. */
    val scheduledMessages: StateFlow<List<ScheduledMessage>> = store.scheduledMessages

    /**
     * True once a query of every channel and status returned all of them in a single page, until the connection is
     * lost or [stop], as events can be missed while not connected.
     */
    val hasLoadedAll: StateFlow<Boolean> = _hasLoadedAll

    /**
     * Loads the scheduled messages saved for the connected user, and starts sending their pending changes in [scope].
     * Does nothing when it is already started.
     */
    suspend fun start(scope: CoroutineScope) = lifecycleMutex.withLock {
        if (senderJob?.isActive == true) return@withLock
        store.load(getRepositoryFacade().scheduledMessageRepository)
        senderJob = sender.start(scope)
        connectionJob = clientState.connectionState
            .filter { it != ConnectionState.Connected }
            .onEach {
                hasLoadedAllResets.incrementAndGet()
                _hasLoadedAll.value = false
            }
            .launchIn(scope)
    }

    /**
     * Stops sending, and removes the scheduled messages from memory. The repository keeps them.
     */
    suspend fun stop() = lifecycleMutex.withLock {
        senderJob?.cancel()
        senderJob = null
        connectionJob?.cancel()
        connectionJob = null
        hasLoadedAllResets.incrementAndGet()
        _hasLoadedAll.value = false
        store.clear()
    }

    suspend fun schedule(cid: String, message: Message, scheduledAt: Date): Result<ScheduledMessage> {
        val others = store.messages.value.values.filter { it.id != message.id }
        val error = validator.validateContent(cid, message)
            ?: validator.validateScheduledAt(cid, scheduledAt)
            ?: validator.validateLimits(cid, others)
        error?.let { return Result.Failure(it) }
        val now = now()
        val content = message.copy(
            cid = cid,
            user = clientState.user.value ?: message.user,
            createdAt = scheduledAt,
            updatedAt = now,
        )
        var exists = false
        val scheduled = store.update(message.id) { current ->
            exists = current != null
            current ?: LocalScheduledMessage(
                id = message.id,
                cid = cid,
                channel = null,
                message = content,
                scheduledAt = scheduledAt,
                serverStatus = ScheduledMessageStatus.Scheduled,
                publishingFailure = null,
                pendingChange = PendingChange.Create,
                hasPendingScheduledAt = false,
                isPendingSendNow = false,
                isDeleted = false,
                isCreateSent = false,
                localFailure = null,
                createdAt = now,
                updatedAt = now,
            )
        }
        if (exists) {
            return Result.Failure(Error.GenericError("A scheduled message with id: ${message.id} already exists."))
        }
        return Result.Success(requireNotNull(scheduled).toScheduledMessage())
    }

    suspend fun update(id: String, message: Message, scheduledAt: Date?): Result<ScheduledMessage> {
        val current = store.messages.value[id] ?: return notFound(id)
        val error = validator.validateContent(current.cid, message)
            ?: scheduledAt?.let { validator.validateScheduledAt(current.cid, it) }
        error?.let { return Result.Failure(it) }
        return change(id) { scheduled, now ->
            scheduled.copy(
                message = message.copy(
                    id = id,
                    cid = scheduled.cid,
                    user = scheduled.message.user,
                    createdAt = scheduled.message.createdAt,
                    updatedAt = now,
                ),
            ).reschedule(scheduledAt)
        }
    }

    suspend fun reschedule(id: String, scheduledAt: Date): Result<ScheduledMessage> {
        val current = store.messages.value[id] ?: return notFound(id)
        val error = validator.validateQuote(current.message.replyMessageId)
            ?: validator.validateScheduledAt(current.cid, scheduledAt)
        error?.let { return Result.Failure(it) }
        return change(id) { scheduled, _ -> scheduled.reschedule(scheduledAt) }
    }

    suspend fun sendNow(id: String): Result<ScheduledMessage> {
        val current = store.messages.value[id] ?: return notFound(id)
        validator.validateQuote(current.message.replyMessageId)?.let { return Result.Failure(it) }
        // A failure of the latest changes is cleared, so they are sent again before the message is sent now.
        return updateVisible(id) { it.copy(isPendingSendNow = true, localFailure = null) }
    }

    suspend fun resend(id: String): Result<ScheduledMessage> {
        val current = store.messages.value[id] ?: return notFound(id)
        if (current.localFailure == null) {
            return Result.Failure(Error.GenericError("The scheduled message $id has no failed changes to send."))
        }
        // A message whose deletion failed is deleted again.
        return store.update(id) { it?.copy(localFailure = null) }
            ?.let { Result.Success(it.toScheduledMessage()) }
            ?: notFound(id)
    }

    /**
     * Deletes a scheduled message locally first: it is hidden right away, and the [ScheduledMessageSender] deletes it
     * on the server, with no request when it is not on the server yet. A message this device does not know is deleted
     * on the server directly.
     */
    suspend fun delete(id: String): Result<Unit> {
        if (store.update(id) { it?.copy(isDeleted = true, localFailure = null) } != null) return Result.Success(Unit)
        return api.deleteScheduledMessage(id).await()
    }

    /**
     * Queries the scheduled messages and saves them. When the query has no status filter and returns a single page,
     * the ones the server no longer has are removed, for example the ones sent while the device was offline. Only the
     * ones that were on the server before the query are removed, so a message scheduled meanwhile is kept. A result
     * for a message removed while the query ran, or older than the local message, is not saved.
     */
    suspend fun query(
        cid: String?,
        statuses: Set<ScheduledMessageQueryStatus>,
        limit: Int,
        next: String?,
    ): Result<QueryScheduledMessagesResult> {
        val known = known()
        val resetsBefore = hasLoadedAllResets.get()
        val result = api.queryScheduledMessages(cid, statuses, limit.coerceIn(1, MAX_QUERY_LIMIT), next).await()
        if (result is Result.Success) {
            saveQueried(result.value.scheduledMessages, known)
            // Only a single page holds every scheduled message of the query, so only then are the others removed.
            val isComplete = statuses.isEmpty() && next == null && result.value.next == null
            if (isComplete) removeMissing(cid, result.value.scheduledMessages, known)
            if (isComplete && cid == null && hasLoadedAllResets.get() == resetsBefore) _hasLoadedAll.value = true
        }
        return result
    }

    /**
     * The scheduled messages known now, to pass to [saveChannel] once a channel query that starts now returns.
     */
    fun known(): KnownScheduledMessages {
        val messages = store.messages.value
        return KnownScheduledMessages(
            ids = messages.keys,
            onServerIds = messages.values
                .filterNot(LocalScheduledMessage::isLocalOnly)
                .mapTo(mutableSetOf(), LocalScheduledMessage::id),
        )
    }

    /**
     * Saves the scheduled messages of the channel with [cid] that a channel query returned, which are all the ones the
     * server has, and removes the ones of the channel the server no longer has, as a full [query] does.
     *
     * @param known The scheduled messages from [known] when the query started.
     */
    suspend fun saveChannel(cid: String, scheduledMessages: List<ScheduledMessage>, known: KnownScheduledMessages) {
        saveQueried(scheduledMessages, known)
        removeMissing(cid, scheduledMessages, known)
    }

    /**
     * Saves the scheduled messages a query returned. One removed while the query ran, for example sent or deleted, or
     * older than the local message, is not saved.
     */
    private suspend fun saveQueried(scheduledMessages: List<ScheduledMessage>, known: KnownScheduledMessages) {
        scheduledMessages.forEach { server ->
            store.update(server.id) { current ->
                when {
                    current == null && server.id in known.ids -> null
                    else -> current.mergeServerUnlessOlder(server)
                }
            }
        }
    }

    /**
     * Removes the scheduled messages of the channel with [cid], or of every channel when null, that were on the server
     * when the query started and are not in its complete result. The ones scheduled while it ran are kept.
     */
    private suspend fun removeMissing(cid: String?, result: List<ScheduledMessage>, known: KnownScheduledMessages) {
        val removed = known.onServerIds - result.map(ScheduledMessage::id).toSet()
        store.removeAll { (cid == null || it.cid == cid) && it.id in removed && !it.isLocalOnly }
    }

    suspend fun onEvent(event: ChatEvent) {
        when (event) {
            is ScheduledMessageCreatedEvent -> save(event.scheduledMessage)
            is ScheduledMessageUpdatedEvent -> save(event.scheduledMessage)
            is ScheduledMessageFailedEvent -> save(event.scheduledMessage)
            is ScheduledMessageDeletedEvent -> store.remove(event.scheduledMessage.id)
            is ScheduledMessageSentEvent -> store.remove(event.scheduledMessage.id)
            else -> Unit
        }
    }

    private suspend fun save(scheduledMessage: ScheduledMessage) {
        store.update(scheduledMessage.id) { it.mergeServerUnlessOlder(scheduledMessage) }
    }

    private suspend fun change(
        id: String,
        transform: (LocalScheduledMessage, now: Date) -> LocalScheduledMessage,
    ): Result<ScheduledMessage> {
        val now = now()
        return updateVisible(id) {
            transform(it, now).copy(
                pendingChange = if (it.isLocalOnly) PendingChange.Create else PendingChange.Update,
                localFailure = null,
                updatedAt = now,
            )
        }
    }

    private fun LocalScheduledMessage.reschedule(scheduledAt: Date?): LocalScheduledMessage =
        if (scheduledAt == null) {
            this
        } else {
            copy(
                message = message.copy(createdAt = scheduledAt),
                scheduledAt = scheduledAt,
                hasPendingScheduledAt = hasPendingScheduledAt || mayBeOnServer,
            )
        }

    /**
     * Applies [transform] to the scheduled message with [id], unless it is deleted.
     */
    private suspend fun updateVisible(
        id: String,
        transform: (LocalScheduledMessage) -> LocalScheduledMessage,
    ): Result<ScheduledMessage> =
        store.update(id) { current -> current?.let { if (it.isDeleted) it else transform(it) } }
            ?.takeUnless(LocalScheduledMessage::isDeleted)
            ?.let { Result.Success(it.toScheduledMessage()) }
            ?: notFound(id)

    private fun <T : Any> notFound(id: String): Result<T> =
        Result.Failure(Error.GenericError("There is no scheduled message with id: $id."))
}

/**
 * The scheduled messages known when a query started.
 *
 * @property ids The IDs of all of them.
 * @property onServerIds The IDs of the ones the server had.
 */
internal class KnownScheduledMessages(val ids: Set<String>, val onServerIds: Set<String>)

/** The largest page the server returns. */
private const val MAX_QUERY_LIMIT = 100

/**
 * Merges [server] into the local message, unless it is older than the local message with no pending changes, for
 * example an event of an edit that arrives after the answer to a later edit.
 */
private fun LocalScheduledMessage?.mergeServerUnlessOlder(server: ScheduledMessage): LocalScheduledMessage =
    if (this != null && pendingChange == null && server.updatedAt < updatedAt) this else mergeServer(server)
