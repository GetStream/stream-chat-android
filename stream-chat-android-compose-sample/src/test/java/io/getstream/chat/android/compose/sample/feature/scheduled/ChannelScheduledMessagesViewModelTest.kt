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

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomScheduledMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExtendWith(TestCoroutineExtension::class)
internal class ChannelScheduledMessagesViewModelTest {

    private val channelType = randomString()
    private val channelId = randomString()
    private val cid = "$channelType:$channelId"
    private val scheduledMessages = MutableStateFlow<List<ScheduledMessage>>(emptyList())
    private val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Connected)
    private val clientState: ClientState = mock {
        on { connectionState } doReturn connectionState
    }
    private val client: ChatClient = mock {
        on { scheduledMessages } doAnswer { scheduledMessages }
        on { clientState } doReturn clientState
        on { queryUsers(any()) } doAnswer { emptyList<User>().asCall() }
        on { scheduledMessages(any(), anyOrNull()) } doAnswer { invocation ->
            val cid = invocation.getArgument<String>(0)
            val parentId = invocation.getArgument<String?>(1)
            scheduledMessages.map { list -> list.filter { it.cid == cid && it.message.parentId == parentId } }
        }
    }
    private val resolvedAttachments = listOf(randomAttachment())

    @Test
    fun `the banner counts the pending messages of the channel or of the thread`() = runTest {
        val parentId = randomString()
        scheduledMessages.value = listOf(
            scheduled(),
            scheduled(),
            scheduled(parentId = parentId),
            randomScheduledMessage(status = ScheduledMessageStatus.Scheduled),
        )
        val viewModel = viewModel()
        viewModel.banner.value shouldBeEqualTo ScheduledMessagesBanner.Pending(2)

        viewModel.onThreadChanged(parentId)

        viewModel.banner.value shouldBeEqualTo ScheduledMessagesBanner.Pending(1)
    }

    @Test
    fun `the banner tells first about the failed messages, then the ones waiting for the connection`() {
        val pending = scheduled()
        val preparing = scheduled(status = ScheduledMessageStatus.PreparingToSchedule)
        val failed = scheduled(status = ScheduledMessageStatus.Failed)

        scheduledMessagesBanner(listOf(pending, preparing, failed), ConnectionState.Offline) shouldBeEqualTo
            ScheduledMessagesBanner.Failed(1)
        scheduledMessagesBanner(listOf(pending, preparing), ConnectionState.Offline) shouldBeEqualTo
            ScheduledMessagesBanner.Waiting(1)
        scheduledMessagesBanner(listOf(pending, preparing), ConnectionState.Connected) shouldBeEqualTo
            ScheduledMessagesBanner.Pending(2)
        scheduledMessagesBanner(emptyList(), ConnectionState.Connected).shouldBeNull()
    }

    @Test
    fun `schedule sends the composed message with its attachments resolved, then empties the composer`() = runTest {
        val composed = randomMessage(cid = cid, attachments = listOf(randomAttachment()))
        val scheduledAt = randomDate()
        val local = scheduled(id = composed.id, status = ScheduledMessageStatus.PreparingToSchedule)
        whenever(client.scheduleMessage(eq(channelType), eq(channelId), any(), eq(scheduledAt))).thenAnswer {
            scheduledMessages.value = listOf(local)
            local.asCall()
        }
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onScheduleClick(composed, scheduledAt)
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.ClearComposer

            val serverDate = Date(scheduledAt.time + 30_000)
            scheduledMessages.value = listOf(local.copy(scheduledAt = serverDate, status = ScheduledMessageStatus.Scheduled))
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Scheduled(serverDate)
        }
        verify(client).scheduleMessage(channelType, channelId, composed.copy(attachments = resolvedAttachments), scheduledAt)
    }

    @Test
    fun `a refused schedule keeps the content of the composer`() = runTest {
        val error = Error.GenericError(randomString())
        whenever(client.scheduleMessage(any(), any(), any(), any())) doAnswer { error.asCall<ScheduledMessage>() }
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onScheduleClick(randomMessage(), randomDate())

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Error(error.message)
            expectNoEvents()
        }
    }

    @Test
    fun `editing fills the composer with the scheduled message and its quote, and hides the banner`() = runTest {
        val quoted = randomMessage()
        val edited = scheduled(message = randomMessage(cid = cid, parentId = null, replyMessageId = quoted.id, replyTo = null))
        scheduledMessages.value = listOf(edited, scheduled())
        whenever(client.getMessage(quoted.id)) doAnswer { quoted.asCall() }

        val viewModel = viewModel(editedMessageId = edited.id)

        viewModel.editedMessage.value shouldBeEqualTo edited
        viewModel.banner.value.shouldBeNull()
        viewModel.events.test {
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.EditInComposer(edited, quoted)
        }
    }

    @Test
    fun `saving an edit keeps the date, the custom data and the mentions still in the text, then ends it`() = runTest {
        val kept = User(id = randomString(), name = "Ann")
        val removed = User(id = randomString(), name = "Bob")
        val edited = scheduled(
            message = randomMessage(
                cid = cid,
                parentId = null,
                replyMessageId = null,
                extraData = mapOf("custom" to "value", "mml" to "<mml/>"),
            ).copy(
                mentionedUsersIds = listOf(kept.id, removed.id),
                mentionedHere = true,
                mentionedRoles = listOf("admin", "moderator"),
            ),
        )
        scheduledMessages.value = listOf(edited)
        whenever(client.queryUsers(any())) doAnswer { listOf(kept, removed).asCall() }
        val composed = Message(text = "Hi @Ann and @admin, @Bobby", attachments = listOf(randomAttachment()))
        whenever(client.updateScheduledMessage(eq(edited.id), any(), isNull())) doAnswer { edited.asCall() }
        val viewModel = viewModel(editedMessageId = edited.id)

        viewModel.events.test {
            skipItems(1) // The content to edit.
            viewModel.onSaveEditClick(composed)

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.ClearComposer
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Scheduled(edited.scheduledAt)
        }
        val content = argumentCaptor<Message>()
        verify(client).updateScheduledMessage(eq(edited.id), content.capture(), isNull())
        content.firstValue.text shouldBeEqualTo composed.text
        content.firstValue.attachments shouldBeEqualTo resolvedAttachments
        content.firstValue.mentionedUsersIds shouldBeEqualTo listOf(kept.id)
        content.firstValue.mentionedHere shouldBeEqualTo false
        content.firstValue.mentionedRoles shouldBeEqualTo listOf("admin")
        content.firstValue.extraData shouldBeEqualTo mapOf("custom" to "value")
        viewModel.editedMessage.value.shouldBeNull()
    }

    @Test
    fun `scheduling a failed message again schedules a new message with its content, then deletes it`() = runTest {
        val failed = scheduled(status = ScheduledMessageStatus.Failed).let {
            it.copy(message = it.message.copy(extraData = mapOf("custom" to "value")))
        }
        scheduledMessages.value = listOf(failed)
        val composed = randomMessage(cid = cid, replyMessageId = null)
        val scheduledAt = randomDate()
        val new = scheduled(id = composed.id)
        whenever(client.scheduleMessage(eq(channelType), eq(channelId), any(), eq(scheduledAt))) doAnswer {
            scheduledMessages.value = listOf(failed, new)
            new.asCall()
        }
        whenever(client.deleteScheduledMessage(failed.id)) doAnswer {
            scheduledMessages.value = listOf(new)
            Unit.asCall()
        }
        val viewModel = viewModel(editedMessageId = failed.id, isScheduledAgain = true)

        viewModel.events.test {
            skipItems(1) // The content to edit.
            viewModel.onScheduleClick(composed, scheduledAt)

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.ClearComposer
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Scheduled(new.scheduledAt)
        }
        val content = argumentCaptor<Message>()
        inOrder(client) {
            verify(client).scheduleMessage(eq(channelType), eq(channelId), content.capture(), eq(scheduledAt))
            verify(client).deleteScheduledMessage(failed.id)
        }
        content.firstValue.id shouldBeEqualTo ""
        content.firstValue.text shouldBeEqualTo composed.text
        content.firstValue.extraData shouldBeEqualTo mapOf("custom" to "value")
        viewModel.editedMessage.value.shouldBeNull()
    }

    @Test
    fun `a failed message scheduled again is deleted before the server answers`() = runTest {
        val failed = scheduled(status = ScheduledMessageStatus.Failed)
        val new = scheduled(status = ScheduledMessageStatus.PreparingToSchedule)
        scheduledMessages.value = listOf(failed)
        whenever(client.scheduleMessage(any(), any(), any(), any())) doAnswer {
            scheduledMessages.value = listOf(failed, new)
            new.asCall()
        }
        whenever(client.deleteScheduledMessage(failed.id)) doAnswer { Unit.asCall() }
        val viewModel = viewModel(editedMessageId = failed.id, isScheduledAgain = true)

        viewModel.onScheduleClick(randomMessage(cid = cid), randomDate())

        // The new message is still waiting for the server.
        verify(client).deleteScheduledMessage(failed.id)
    }

    @Test
    fun `a failed message scheduled again that can't be deleted tells so last`() = runTest {
        val failed = scheduled(status = ScheduledMessageStatus.Failed)
        val new = scheduled()
        scheduledMessages.value = listOf(failed)
        whenever(client.scheduleMessage(any(), any(), any(), any())) doAnswer {
            scheduledMessages.value = listOf(failed, new)
            new.asCall()
        }
        val error = Error.GenericError(randomString())
        whenever(client.deleteScheduledMessage(failed.id)) doAnswer { error.asCall<Unit>() }
        val viewModel = viewModel(editedMessageId = failed.id, isScheduledAgain = true)

        viewModel.events.test {
            skipItems(1) // The content to edit.
            viewModel.onScheduleClick(randomMessage(cid = cid), randomDate())

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.ClearComposer
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Scheduled(new.scheduledAt)
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Error(error.message)
        }
    }

    @Test
    fun `editing waits for the scheduled messages to load after a restart`() = runTest {
        val edited = scheduled()
        val viewModel = viewModel(editedMessageId = edited.id)
        viewModel.editedMessage.value.shouldBeNull()

        scheduledMessages.value = listOf(edited)

        viewModel.editedMessage.value shouldBeEqualTo edited
        viewModel.events.test {
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.EditInComposer(edited, null)
        }
    }

    @Test
    fun `a schedule made offline tells it is queued`() = runTest {
        connectionState.value = ConnectionState.Offline
        val composed = randomMessage(cid = cid)
        whenever(client.scheduleMessage(any(), any(), any(), any())) doAnswer {
            scheduled(id = composed.id, status = ScheduledMessageStatus.PreparingToSchedule).asCall()
        }
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onScheduleClick(composed, randomDate())

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.ClearComposer
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Queued
        }
    }

    @Test
    fun `a message sent right away, as its date had passed, tells so`() = runTest {
        val composed = randomMessage(cid = cid)
        whenever(client.scheduleMessage(any(), any(), any(), any())) doAnswer {
            scheduled(id = composed.id, status = ScheduledMessageStatus.PreparingToSchedule).asCall()
        }
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onScheduleClick(composed, randomDate())

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.ClearComposer
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.SentRightAway
        }
    }

    @Test
    fun `the edit ends when the server starts sending the message`() = runTest {
        val edited = scheduled()
        scheduledMessages.value = listOf(edited)
        val viewModel = viewModel(editedMessageId = edited.id)

        viewModel.events.test {
            skipItems(1) // The content to edit.
            scheduledMessages.value = listOf(edited.copy(status = ScheduledMessageStatus.Sending))

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.ClearComposer
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.EditedMessageSent
        }
        viewModel.editedMessage.value.shouldBeNull()
    }

    @Test
    fun `the edit ends when the message is deleted on another device`() = runTest {
        val edited = scheduled()
        scheduledMessages.value = listOf(edited)
        val viewModel = viewModel(editedMessageId = edited.id)

        viewModel.events.test {
            skipItems(1) // The content to edit.
            scheduledMessages.value = emptyList()

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.ClearComposer
            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.EditedMessageDeleted
        }
    }

    private fun viewModel(
        editedMessageId: String? = null,
        isScheduledAgain: Boolean = false,
    ) = ChannelScheduledMessagesViewModel(
        cid = cid,
        editedMessageId = editedMessageId,
        isScheduledAgain = isScheduledAgain,
        resolveAttachments = { resolvedAttachments },
        client = client,
    )

    private fun scheduled(
        id: String = randomString(),
        parentId: String? = null,
        status: ScheduledMessageStatus = ScheduledMessageStatus.Scheduled,
        message: Message = randomMessage(id = id, cid = cid, parentId = parentId, replyMessageId = null),
    ) = randomScheduledMessage(id = message.id, cid = cid, message = message, status = status)
}
