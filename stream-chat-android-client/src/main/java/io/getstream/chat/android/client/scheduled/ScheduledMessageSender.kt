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
import io.getstream.chat.android.client.errors.isDuplicateMessageError
import io.getstream.chat.android.client.errors.isStatusNotFound
import io.getstream.chat.android.client.errors.scheduledMessageRejectionReason
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.needsUpload
import io.getstream.chat.android.client.scheduled.LocalScheduledMessage.PendingChange
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageFailure.LocalDeviceFailedScheduling
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

/**
 * Sends the changes of the scheduled messages in [store] to the server, one request at a time: it uploads the
 * attachments with [attachmentUploader], then schedules the message, updates it, or sends it now once its changes are
 * on the server. It runs whenever the scheduled messages change and whenever the connection is back. Without a
 * connection, the changes stay pending until the next one.
 *
 * @param onSentAsMessage Called with the message the server sent right away, because its date had passed.
 */
internal class ScheduledMessageSender(
    private val store: ScheduledMessageStore,
    private val api: ChatApi,
    private val attachmentUploader: AttachmentUploader,
    private val clientState: ClientState,
    private val onSentAsMessage: suspend (Message) -> Unit,
) {

    private val logger by taggedLogger("Chat:ScheduledMessageSender")

    /**
     * Starts sending the pending changes, including the ones left in progress before a restart, until [scope] is
     * cancelled.
     */
    fun start(scope: CoroutineScope): Job =
        merge(store.messages, clientState.connectionState.filter { it == ConnectionState.Connected })
            .conflate()
            .onEach { sendPending() }
            .launchIn(scope)

    private suspend fun sendPending() {
        while (true) {
            val next = store.messages.value.values
                .filter(LocalScheduledMessage::hasPendingRequest)
                .minWithOrNull(
                    compareBy<LocalScheduledMessage> { !it.isDeleted }.thenBy(LocalScheduledMessage::createdAt),
                )
                ?: return
            if (!send(next)) return
        }
    }

    /**
     * Sends the next request [message] needs.
     *
     * @return False when it waits for the connection.
     */
    private suspend fun send(message: LocalScheduledMessage): Boolean {
        if (message.isDeleted) return delete(message)
        if (!clientState.isOnline) return false
        val attachment = message.message.attachments.firstOrNull(Attachment::needsUpload)
        return when {
            attachment != null -> upload(message, attachment)
            message.pendingChange == PendingChange.Create -> create(message)
            message.pendingChange == PendingChange.Update -> update(message)
            else -> sendNow(message)
        }
    }

    /**
     * Deletes [message] on the server, unless it is not there yet, then removes it. When the server can't delete it,
     * the message shows again with the failure.
     */
    private suspend fun delete(message: LocalScheduledMessage): Boolean {
        if (message.mayBeOnServer) {
            if (!clientState.isOnline) return false
            val result = retryOnConflict { api.deleteScheduledMessage(message.id) }
            if (result is Result.Failure && !result.value.isNotFound()) {
                if (result.value.isWaitingForConnection()) return false
                logger.e { "[delete] failed to delete ${message.id} on the server: ${result.value}" }
                store.update(message.id) {
                    it?.copy(isDeleted = false, localFailure = result.value.toScheduledMessageFailure())
                }
                return true
            }
        }
        store.update(message.id) { null }
        return true
    }

    private suspend fun upload(message: LocalScheduledMessage, attachment: Attachment): Boolean {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        return when (val result = attachmentUploader.uploadAttachment(channelType, channelId, attachment, message.id)) {
            is Result.Success -> {
                store.update(message.id) { it?.replaceAttachment(attachment, result.value) }
                true
            }
            is Result.Failure -> {
                if (result.value.isWaitingForConnection()) return false
                val failed = attachment.copy(uploadState = Attachment.UploadState.Failed(result.value))
                store.update(message.id) {
                    it?.replaceAttachment(attachment, failed)
                        ?.copy(
                            localFailure = LocalDeviceFailedScheduling(
                                reason = LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed,
                                message = result.value.message,
                            ),
                        )
                }
                true
            }
        }
    }

    private suspend fun create(message: LocalScheduledMessage): Boolean {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        // From now on the server may have the message, until it answers.
        val sent = store.update(message.id) { it?.copy(isCreateSent = true) } ?: return true
        val result = retryOnConflict {
            api.scheduleMessage(channelType, channelId, sent.message, sent.scheduledAt)
        }
        return when (result) {
            is Result.Success -> {
                val scheduledMessage = result.value.scheduledMessage
                when {
                    scheduledMessage == null -> {
                        onSentAsMessage(result.value.message)
                        store.update(message.id) { null }
                    }
                    // The server answers a repeated create with the message it stored first, with older content.
                    message.isCreateSent -> saveRepeatedCreate(scheduledMessage)
                    else -> saveSent(sent, scheduledMessage)
                }
                true
            }
            // The first create was sent right away, as its date had passed, and its answer was lost.
            is Result.Failure -> if (message.isCreateSent && result.value.isDuplicateMessageError()) {
                store.update(message.id) { null }
                true
            } else {
                onFailure(sent, result.value)
            }
        }
    }

    private suspend fun update(message: LocalScheduledMessage): Boolean {
        val scheduledAt = message.scheduledAt.takeIf { message.hasPendingScheduledAt }
        val result = retryOnConflict { api.updateScheduledMessage(message.id, message.message, scheduledAt) }
        return when (result) {
            is Result.Success -> {
                saveSent(message, result.value)
                true
            }
            is Result.Failure -> onFailure(message, result.value)
        }
    }

    private suspend fun sendNow(message: LocalScheduledMessage): Boolean =
        when (val result = retryOnConflict { api.sendScheduledMessageNow(message.id) }) {
            is Result.Success -> {
                store.update(message.id) { it?.mergeServer(result.value)?.copy(isPendingSendNow = false) }
                true
            }
            is Result.Failure -> onFailure(message, result.value)
        }

    /**
     * Sends the request again, once, when the server rejects it with a conflict: another request changed the message
     * a moment earlier, and the same request succeeds when sent again. Other rejections are never sent again.
     */
    private suspend fun <T : Any> retryOnConflict(request: () -> Call<T>): Result<T> {
        val result = request().await()
        val reason = (result as? Result.Failure)?.value?.scheduledMessageRejectionReason()
        return if (reason == ServerRejectedScheduling.Reason.ScheduledMessageConflict) request().await() else result
    }

    /**
     * Saves the answer of the server to the changes in [sent]. When the message changed while they were sent, the
     * changes stay pending, now as an update.
     */
    private suspend fun saveSent(sent: LocalScheduledMessage, server: ScheduledMessage) {
        store.update(sent.id) { current ->
            when {
                current == null -> null
                current.message == sent.message && current.scheduledAt == sent.scheduledAt ->
                    current.copy(pendingChange = null, hasPendingScheduledAt = false).mergeServer(server)
                else -> current.copy(
                    pendingChange = PendingChange.Update,
                    hasPendingScheduledAt = current.hasPendingScheduledAt || current.scheduledAt != sent.scheduledAt,
                    isCreateSent = false,
                ).mergeServer(server)
            }
        }
    }

    /**
     * Saves the answer to a create that was sent before. While the server can still edit the message, the local
     * content stays pending as an update, with the date when it was rescheduled since. Otherwise the server version is
     * saved.
     */
    private suspend fun saveRepeatedCreate(server: ScheduledMessage) {
        store.update(server.id) { current ->
            current?.copy(
                pendingChange = PendingChange.Update.takeIf { server.status == ScheduledMessageStatus.Scheduled },
                isCreateSent = false,
            )?.mergeServer(server)
        }
    }

    /**
     * Saves why the request for [sent] failed, or removes the message when the server no longer has it.
     *
     * @return False when it waits for the connection.
     */
    private suspend fun onFailure(sent: LocalScheduledMessage, error: Error): Boolean {
        val reason = error.scheduledMessageRejectionReason()
        val isGone = reason == ServerRejectedScheduling.Reason.ScheduledMessageAlreadySent ||
            reason == ServerRejectedScheduling.Reason.ScheduledMessageCanceled ||
            (!sent.isLocalOnly && error.isNotFound())
        when {
            isGone -> store.update(sent.id) { null }
            error.isWaitingForConnection() -> return false
            else -> store.update(sent.id) {
                // A rejection means the server did not store a create.
                it?.copy(
                    localFailure = error.toScheduledMessageFailure(),
                    isCreateSent = it.isCreateSent && !error.isRejection(),
                )
            }
        }
        return true
    }

    private fun Error.isNotFound(): Boolean = (this as? Error.NetworkError)?.isStatusNotFound() == true

    /**
     * Whether the server answered that it rejects the request. A 5xx, a rate limit, a timeout or no answer leave the
     * outcome unknown.
     */
    private fun Error.isRejection(): Boolean =
        this is Error.NetworkError && statusCode in HTTP_CLIENT_ERRORS && statusCode !in UNKNOWN_OUTCOME_STATUSES

    /** Whether the request failed with no answer from the server because the device is offline. */
    private fun Error.isWaitingForConnection(): Boolean = !isRejection() && !clientState.isOnline

    private fun Error.toScheduledMessageFailure(): ScheduledMessageFailure = if (isRejection()) {
        ServerRejectedScheduling(reason = scheduledMessageRejectionReason(), message = message)
    } else {
        LocalDeviceFailedScheduling(reason = LocalDeviceFailedScheduling.Reason.RequestFailed, message = message)
    }

    private fun LocalScheduledMessage.replaceAttachment(old: Attachment, new: Attachment): LocalScheduledMessage =
        copy(message = message.copy(attachments = message.attachments.map { if (it == old) new else it }))
}

/** The HTTP client error statuses. */
@Suppress("MagicNumber") // The HTTP statuses, listed once here.
private val HTTP_CLIENT_ERRORS = 400..499

/** Request timeout and too many requests, whose outcome is unknown. */
@Suppress("MagicNumber") // The HTTP statuses, listed once here.
private val UNKNOWN_OUTCOME_STATUSES = setOf(408, 429)
