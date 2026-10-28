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
import io.getstream.chat.android.client.events.ScheduledMessageCreatedEvent
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.persistance.repository.ScheduledMessageRepository
import io.getstream.chat.android.client.scheduled.LocalScheduledMessage.PendingChange
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.IOException
import java.util.Date

internal fun randomLocalScheduledMessage(
    id: String = randomString(),
    cid: String = randomCID(),
    message: Message = Message(id = id, cid = cid, text = randomString(), user = randomUser()),
    scheduledAt: Date = randomDate(),
    serverStatus: ScheduledMessageStatus = ScheduledMessageStatus.Scheduled,
    publishingFailure: ScheduledMessageFailure.ServerFailedPublishing? = null,
    pendingChange: PendingChange? = null,
    hasPendingScheduledAt: Boolean = false,
    isPendingSendNow: Boolean = false,
    isDeleted: Boolean = false,
    isCreateSent: Boolean = false,
    localFailure: ScheduledMessageFailure? = null,
    createdAt: Date = randomDate(),
    updatedAt: Date = randomDate(),
): LocalScheduledMessage = LocalScheduledMessage(
    id = id,
    cid = cid,
    channel = null,
    message = message,
    scheduledAt = scheduledAt,
    serverStatus = serverStatus,
    publishingFailure = publishingFailure,
    pendingChange = pendingChange,
    hasPendingScheduledAt = hasPendingScheduledAt,
    isPendingSendNow = isPendingSendNow,
    isDeleted = isDeleted,
    isCreateSent = isCreateSent,
    localFailure = localFailure,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/**
 * A [ScheduledMessageRepository] in memory, which survives a restart of the [ScheduledMessageManager].
 */
internal class InMemoryScheduledMessageRepository(
    vararg messages: LocalScheduledMessage,
) : ScheduledMessageRepository {

    val messages = messages.associateBy(LocalScheduledMessage::id).toMutableMap()

    override suspend fun selectScheduledMessages(): List<LocalScheduledMessage> = messages.values.toList()

    override suspend fun insertScheduledMessage(message: LocalScheduledMessage) {
        messages[message.id] = message
    }

    override suspend fun deleteScheduledMessage(id: String) {
        messages.remove(id)
    }

    override suspend fun clear() {
        messages.clear()
    }
}

/**
 * A [Call] that completes when [result] does, to hold a request in flight.
 */
internal class PendingCall<T : Any>(val result: CompletableDeferred<Result<T>> = CompletableDeferred()) : Call<T> {
    override fun execute(): Result<T> = error("Not supported")
    override fun enqueue(callback: Call.Callback<T>) = error("Not supported")
    override suspend fun await(): Result<T> = result.await()
    override fun cancel() = Unit
}

/**
 * A [Call] that runs [block] when awaited.
 */
internal class SuspendingCall<T : Any>(private val block: suspend () -> Result<T>) : Call<T> {
    override fun execute(): Result<T> = error("Not supported")
    override fun enqueue(callback: Call.Callback<T>) = error("Not supported")
    override suspend fun await(): Result<T> = block()
    override fun cancel() = Unit
}

/** A rejection of the server with a backend [code]. */
internal fun serverError(code: Int, statusCode: Int = 400): Error.NetworkError =
    Error.NetworkError(message = randomString(), serverErrorCode = code, statusCode = statusCode)

/** A failure with no answer from the server. */
internal fun connectionError(): Error.NetworkError =
    Error.NetworkError(message = randomString(), serverErrorCode = -1, statusCode = -1, cause = IOException())

/**
 * Builds a [ScheduledMessageManager] with mocked dependencies, an online connection and a repository in memory.
 */
internal class ScheduledMessageManagerFixture(
    repository: ScheduledMessageRepository = InMemoryScheduledMessageRepository(),
) {
    val api: ChatApi = mock()
    val attachmentUploader: AttachmentUploader = mock()
    val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Connected)
    val user: User = randomUser()
    val clientState: ClientState = mock {
        on { connectionState } doReturn connectionState
        on { user } doReturn MutableStateFlow(user)
        on { isOnline } doAnswer { connectionState.value == ConnectionState.Connected }
    }
    val repositoryFacade: RepositoryFacade = mock {
        on { scheduledMessageRepository } doReturn repository
    }
    val sentAsMessages = mutableListOf<Message>()
    val now = Date()

    val manager = ScheduledMessageManager(
        api = api,
        attachmentUploader = attachmentUploader,
        clientState = clientState,
        getRepositoryFacade = { repositoryFacade },
        now = { now },
        onSentAsMessage = { sentAsMessages += it },
    )
}

internal fun scheduledMessageCreatedEvent(scheduledMessage: ScheduledMessage) = ScheduledMessageCreatedEvent(
    type = EventType.SCHEDULED_MESSAGE_CREATED,
    createdAt = Date(),
    rawCreatedAt = null,
    cid = scheduledMessage.cid,
    channelType = scheduledMessage.cid.substringBefore(":"),
    channelId = scheduledMessage.cid.substringAfter(":"),
    scheduledMessage = scheduledMessage,
)
