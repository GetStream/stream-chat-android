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

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities.SCHEDULE_MESSAGE
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.QueryScheduledMessagesResult
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageFailure.LocalDeviceFailedScheduling
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerFailedPublishing
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelCapabilities
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomScheduledMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageAction.CopyText
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageAction.Delete
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageAction.Edit
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageAction.EditAndReschedule
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageAction.Reschedule
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageAction.SendNow
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageListEvent
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.CoroutineCall
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.Date

@Suppress("LargeClass")
@ExtendWith(TestCoroutineExtension::class)
internal class ScheduledMessageListControllerTest {

    private lateinit var scheduledMessages: MutableStateFlow<List<ScheduledMessage>>
    private lateinit var hasLoadedAll: MutableStateFlow<Boolean>
    private lateinit var deletedAlreadySent: MutableSharedFlow<ScheduledMessage>
    private lateinit var connectionState: MutableStateFlow<ConnectionState>
    private lateinit var currentUser: MutableStateFlow<User?>
    private lateinit var client: ChatClient
    private lateinit var quotedMessages: MutableMap<String, Message>
    private lateinit var loadedMessageIds: MutableList<String>
    private val savedUsers = mutableListOf<User>()
    private val repository: RepositoryFacade = mock {
        onBlocking { selectUsers(any()) } doSuspendableAnswer { invocation ->
            val ids = invocation.getArgument<List<String>>(0)
            savedUsers.filter { it.id in ids }
        }
    }

    @BeforeEach
    fun setUp() {
        scheduledMessages = MutableStateFlow(emptyList())
        hasLoadedAll = MutableStateFlow(false)
        deletedAlreadySent = MutableSharedFlow()
        connectionState = MutableStateFlow(ConnectionState.Connected)
        currentUser = MutableStateFlow(null)
        quotedMessages = mutableMapOf()
        loadedMessageIds = mutableListOf()
        val state: ClientState = mock {
            on { connectionState } doReturn connectionState
            on { user } doReturn currentUser
        }
        client = mock {
            on { scheduledMessages } doReturn scheduledMessages
            on { hasLoadedAllScheduledMessages } doReturn hasLoadedAll
            on { deletedScheduledMessagesAlreadySent } doReturn deletedAlreadySent
            on { clientState } doReturn state
            on { queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull()) } doReturn
                QueryScheduledMessagesResult(emptyList(), next = null).asCall()
            on { repositoryFacade } doReturn repository
        }
    }

    @Test
    fun `sorts the messages by their date, then by their text, whatever their status`() = runTest {
        val late = scheduled(scheduledAt = Date(3_000))
        val failed = scheduled(scheduledAt = Date(2_000), status = ScheduledMessageStatus.Failed)
        val sameTimeB = scheduled(scheduledAt = Date(1_000), text = "b")
        val sameTimeA = scheduled(scheduledAt = Date(1_000), text = "a")
        scheduledMessages.value = listOf(late, sameTimeB, sameTimeA, failed)

        val items = controller().state.value.items

        items.map { it.scheduledMessage } shouldBeEqualTo listOf(sameTimeA, sameTimeB, failed, late)
    }

    @Test
    fun `the scope of a channel lists its messages only, thread replies included`() = runTest {
        val cid = randomString()
        val inChannel = scheduled(cid = cid)
        val inThread = scheduled(cid = cid, parentId = randomString())
        scheduledMessages.value = listOf(inChannel, inThread, scheduled())

        val items = controller(cid = cid).state.value.items

        items.map { it.scheduledMessage }.toSet() shouldBeEqualTo setOf(inChannel, inThread)
        verify(client).queryScheduledMessages(eq(cid), any(), any(), anyOrNull())
    }

    @Test
    fun `the scope of a thread lists its replies only`() = runTest {
        val cid = randomString()
        val parentId = randomString()
        val inThread = scheduled(cid = cid, parentId = parentId)
        val inOtherThread = scheduled(cid = cid, parentId = randomString())
        scheduledMessages.value = listOf(scheduled(cid = cid), inThread, inOtherThread, scheduled(parentId = parentId))

        val items = controller(cid = cid, parentMessageId = parentId).state.value.items

        items.map { it.scheduledMessage } shouldBeEqualTo listOf(inThread)
        verify(client).queryScheduledMessages(eq(cid), any(), any(), anyOrNull())
    }

    @Test
    fun `the actions follow the status of the message`() = runTest {
        val pending = scheduled(status = ScheduledMessageStatus.PreparingToSchedule)
        val sending = scheduled(status = ScheduledMessageStatus.Sending)
        val uploading = scheduled(
            status = ScheduledMessageStatus.PreparingToSchedule,
            attachments = listOf(Attachment(upload = File(randomString()))),
        )

        pending.actions() shouldBeEqualTo listOf(Edit, Reschedule, SendNow, Delete)
        sending.actions().shouldBeEmpty()
        uploading.actions().shouldBeEmpty()
    }

    @Test
    fun `a message the server failed to send can be edited, rescheduled or deleted`() = runTest {
        val failed = failed(ServerFailedPublishing(ServerFailedPublishing.Reason.Banned))
        val channelDeleted = failed(ServerFailedPublishing(ServerFailedPublishing.Reason.ChannelDeleted))

        failed.actions() shouldBeEqualTo listOf(EditAndReschedule, Reschedule, Delete)
        channelDeleted.actions() shouldBeEqualTo listOf(CopyText, Delete)
    }

    @Test
    fun `a message whose changes did not reach the server can be edited, rescheduled or deleted`() = runTest {
        val rejected = failed(ServerRejectedScheduling(ServerRejectedScheduling.Reason.ScheduledLimitChannel, ""))
        val uploadFailed = failed(LocalDeviceFailedScheduling(LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed, null))
        val sendingOnServer = failed(ServerRejectedScheduling(ServerRejectedScheduling.Reason.ScheduledMessageSending, ""))

        rejected.actions() shouldBeEqualTo listOf(Edit, Reschedule, Delete)
        uploadFailed.actions() shouldBeEqualTo listOf(Edit, Reschedule, Delete)
        sendingOnServer.actions() shouldBeEqualTo listOf(Delete)
    }

    @Test
    fun `a message shows the quote it comes with, without loading it`() = runTest {
        val quoted = randomMessage(deletedAt = null)
        quotedMessages[quoted.id] = quoted.copy(text = randomString())
        scheduledMessages.value = listOf(scheduled(replyTo = quoted))

        val item = controller().state.value.items.single()

        item.quotedMessage shouldBeEqualTo quoted
        item.isQuoteDeleted shouldBeEqualTo false
        loadedMessageIds shouldBeEqualTo emptyList()
    }

    @Test
    fun `a message that quotes a deleted message can only be edited or deleted`() = runTest {
        val quoted = randomMessage(deletedAt = Date())
        scheduledMessages.value = listOf(scheduled(replyTo = quoted))

        val item = controller().state.value.items.single()

        item.quotedMessage shouldBeEqualTo quoted
        item.isQuoteDeleted shouldBeEqualTo true
        item.actions shouldBeEqualTo listOf(Edit, Delete)
    }

    @Test
    fun `a message whose quote did not come with it shows no quote`() = runTest {
        val quoted = randomMessage(deletedAt = Date())
        quotedMessages[quoted.id] = quoted
        scheduledMessages.value = listOf(scheduled(replyMessageId = quoted.id))

        val item = controller().state.value.items.single()

        item.quotedMessage shouldBeEqualTo null
        item.actions shouldBeEqualTo listOf(Edit, Reschedule, SendNow, Delete)
        loadedMessageIds shouldBeEqualTo emptyList()
    }

    @Test
    fun `a failed message that quotes a deleted message can't be rescheduled`() = runTest {
        val rejected = failed(ServerRejectedScheduling(ServerRejectedScheduling.Reason.ScheduledLimitChannel, ""))

        rejected.scheduledMessageActions(isQuoteDeleted = true, canSchedule = true) shouldBeEqualTo listOf(Edit, Delete)
    }

    @Test
    fun `a message the server failed to send that quotes a deleted message can't be rescheduled`() = runTest {
        val failed = failed(ServerFailedPublishing(ServerFailedPublishing.Reason.InvalidMessage))

        failed.scheduledMessageActions(isQuoteDeleted = true, canSchedule = true) shouldBeEqualTo
            listOf(EditAndReschedule, Delete)
    }

    @Test
    fun `without the capability to schedule, a message can only have its text copied or be deleted`() = runTest {
        val pending = scheduled()
        val rejected = failed(ServerRejectedScheduling(ServerRejectedScheduling.Reason.ScheduledLimitChannel, ""))
        val failed = failed(ServerFailedPublishing(ServerFailedPublishing.Reason.ChannelFrozen))
        val channelDeleted = failed(ServerFailedPublishing(ServerFailedPublishing.Reason.ChannelDeleted))
        val sending = scheduled(status = ScheduledMessageStatus.Sending)

        pending.actionsWithoutScheduling() shouldBeEqualTo listOf(CopyText, Delete)
        rejected.actionsWithoutScheduling() shouldBeEqualTo listOf(CopyText, Delete)
        failed.actionsWithoutScheduling() shouldBeEqualTo listOf(CopyText, Delete)
        channelDeleted.actionsWithoutScheduling() shouldBeEqualTo listOf(CopyText, Delete)
        sending.actionsWithoutScheduling().shouldBeEmpty()
    }

    @Test
    fun `a message in a saved channel where the user can no longer schedule can only be copied or deleted`() = runTest {
        val frozen = randomChannel(ownCapabilities = capabilities(canSchedule = false))
        val open = randomChannel(ownCapabilities = capabilities(canSchedule = true))
        givenSavedChannels(frozen, open)
        scheduledMessages.value = listOf(
            scheduled(cid = frozen.cid, scheduledAt = Date(1_000)),
            scheduled(cid = open.cid, scheduledAt = Date(2_000)),
        )

        val items = controller().state.value.items

        items.map { it.actions } shouldBeEqualTo listOf(
            listOf(CopyText, Delete),
            listOf(Edit, Reschedule, SendNow, Delete),
        )
    }

    @Test
    fun `a message whose channel or capabilities are not known keeps its actions`() = runTest {
        val withoutCapabilities = randomChannel(ownCapabilities = emptySet())
        givenSavedChannels(withoutCapabilities)
        scheduledMessages.value = listOf(scheduled(cid = withoutCapabilities.cid), scheduled())

        val items = controller().state.value.items

        items.map { it.actions } shouldBeEqualTo List(2) { listOf(Edit, Reschedule, SendNow, Delete) }
    }

    @Test
    fun `the capability is read again when the messages change`() = runTest {
        val channel = randomChannel(ownCapabilities = capabilities(canSchedule = true))
        givenSavedChannels(channel)
        val message = scheduled(cid = channel.cid)
        scheduledMessages.value = listOf(message)
        val controller = controller()
        controller.state.value.items.single().actions shouldBeEqualTo listOf(Edit, Reschedule, SendNow, Delete)

        givenSavedChannels(channel.copy(ownCapabilities = channel.ownCapabilities - SCHEDULE_MESSAGE))
        scheduledMessages.value = listOf(message.copy(scheduledAt = randomDate()))

        controller.state.value.items.single().actions shouldBeEqualTo listOf(CopyText, Delete)
    }

    @Test
    fun `each message gets its channel from its summary, without loading the channel`() = runTest {
        val summary = ChannelInfo(
            cid = "messaging:general",
            id = "general",
            type = "messaging",
            memberCount = 6,
            name = "General",
            image = "https://example.com/general.png",
            members = listOf(randomMember(), randomMember()),
        )
        scheduledMessages.value = listOf(scheduled(cid = "messaging:general").copy(channel = summary))

        val channel = controller().state.value.items.single().channel

        channel shouldBeEqualTo Channel(
            id = "general",
            type = "messaging",
            name = "General",
            image = "https://example.com/general.png",
            memberCount = 6,
            members = summary.members,
        )
        verify(client, never()).queryChannels(any())
    }

    @Test
    fun `the channel config is the one saved when it is asked for, also when saved after the list loaded`() = runTest {
        val config = Config(scheduledMessagesMaxLeadTimeSeconds = 3600)
        scheduledMessages.value = listOf(scheduled(cid = "messaging:general"))
        val controller = controller()
        controller.channelConfig("messaging:general") shouldBeEqualTo null

        whenever(repository.selectChannelConfig("messaging")) doReturn ChannelConfig("messaging", config)

        controller.channelConfig("messaging:general") shouldBeEqualTo config
    }

    @Test
    fun `a message with no summary has no channel`() = runTest {
        scheduledMessages.value = listOf(scheduled())

        controller().state.value.items.single().channel shouldBeEqualTo null
        verify(client, never()).queryChannels(any())
    }

    @Test
    fun `without a search, the search results are all the messages`() = runTest {
        scheduledMessages.value = listOf(scheduled(), scheduled())

        val state = controller().state.value

        state.searchQuery shouldBeEqualTo ""
        state.items.size shouldBeEqualTo 2
        state.searchResults shouldBeEqualTo state.items
    }

    @Test
    fun `the search keeps the messages whose text or attachment title or name contains it`() = runTest {
        val byText = scheduled(scheduledAt = Date(1_000), text = "Review the Venue shortlist")
        val byTitle = scheduled(scheduledAt = Date(2_000), attachments = listOf(randomAttachment(title = "venue.pdf")))
        val byName = scheduled(scheduledAt = Date(3_000), attachments = listOf(randomAttachment(name = "VENUE.png")))
        val other = scheduled(scheduledAt = Date(4_000), text = "Heads up")
        scheduledMessages.value = listOf(other, byName, byTitle, byText)
        val controller = controller()

        controller.setSearchQuery("  venue ")

        val state = controller.state.value
        state.searchQuery shouldBeEqualTo "  venue "
        state.searchResults.map { it.scheduledMessage } shouldBeEqualTo listOf(byText, byTitle, byName)
        state.items.map { it.scheduledMessage } shouldBeEqualTo listOf(byText, byTitle, byName, other)
    }

    @Test
    fun `the search matches the channel name and the members other than the current user`() = runTest {
        val me = randomUser(name = "Maya Ross")
        currentUser.value = me
        val named = scheduled(scheduledAt = Date(1_000), text = "Heads up").withSummary(name = "Amsterdam Office")
        val withMember = scheduled(scheduledAt = Date(2_000), text = "Heads up")
            .withSummary(name = "", members = listOf(randomMember(user = me), randomMember(randomUser(name = "Ana"))))
        val onlyMe = scheduled(scheduledAt = Date(3_000), text = "Heads up")
            .withSummary(name = "", members = listOf(randomMember(me)))
        scheduledMessages.value = listOf(named, withMember, onlyMe)
        val controller = controller()

        controller.setSearchQuery("amsterdam")
        controller.state.value.searchResults.map { it.scheduledMessage } shouldBeEqualTo listOf(named)

        controller.setSearchQuery("ana")
        controller.state.value.searchResults.map { it.scheduledMessage } shouldBeEqualTo listOf(withMember)

        controller.setSearchQuery("maya")
        controller.state.value.searchResults shouldBeEqualTo emptyList()
    }

    @Test
    fun `the search stops matching the current user once the user is known`() = runTest {
        val me = randomUser(name = "Maya Ross")
        val direct = scheduled(text = "Heads up")
            .withSummary(name = "", members = listOf(randomMember(user = me), randomMember(randomUser(name = "Ana"))))
        scheduledMessages.value = listOf(direct)
        val controller = controller()
        controller.setSearchQuery("maya")
        controller.state.value.searchResults.map { it.scheduledMessage } shouldBeEqualTo listOf(direct)

        currentUser.value = me

        controller.state.value.searchResults shouldBeEqualTo emptyList()
    }

    @Test
    fun `the search matches the channel ID of a message with no summary`() = runTest {
        val message = scheduled(cid = "messaging:launch-team")
        scheduledMessages.value = listOf(message, scheduled())
        val controller = controller()

        controller.setSearchQuery("LAUNCH")

        controller.state.value.searchResults.map { it.scheduledMessage } shouldBeEqualTo listOf(message)
    }

    @Test
    fun `the search follows the changes of the messages, and a blank search shows them all again`() = runTest {
        val first = scheduled(scheduledAt = Date(1_000), text = "launch notes")
        scheduledMessages.value = listOf(first)
        val controller = controller()
        controller.setSearchQuery("launch")

        val second = scheduled(scheduledAt = Date(2_000), text = "Launch day")
        scheduledMessages.value = listOf(first, second, scheduled(scheduledAt = Date(3_000)))
        controller.state.value.searchResults.map { it.scheduledMessage } shouldBeEqualTo listOf(first, second)

        controller.setSearchQuery("   ")
        controller.state.value.searchResults shouldBeEqualTo controller.state.value.items
    }

    @Test
    fun `loads every page of the scheduled messages`() = runTest {
        val next = randomString()
        whenever(client.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())).doReturn(
            QueryScheduledMessagesResult(emptyList(), next = next).asCall(),
            QueryScheduledMessagesResult(emptyList(), next = null).asCall(),
        )

        controller()

        verify(client).queryScheduledMessages(isNull(), any(), any(), isNull())
        verify(client).queryScheduledMessages(isNull(), any(), any(), eq(next))
    }

    @Test
    fun `does not load the scheduled messages again once the client has them all`() = runTest {
        hasLoadedAll.value = true

        controller()

        verify(client, never()).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
    }

    @Test
    fun `loads the scheduled messages again once connected after the connection was lost`() = runTest {
        hasLoadedAll.value = true
        controller()

        connectionState.value = ConnectionState.Offline
        hasLoadedAll.value = false
        verify(client, never()).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
        connectionState.value = ConnectionState.Connected

        verify(client).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
    }

    @Test
    fun `is loading until the messages are loaded, then empty`() = runTest {
        whenever(client.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())) doReturn
            CoroutineCall<QueryScheduledMessagesResult>(backgroundScope) { awaitCancellation() }
        val controller = controller()
        controller.state.value.isLoading shouldBeEqualTo true

        hasLoadedAll.value = true

        controller.state.value.isLoading shouldBeEqualTo false
        controller.state.value.isLoadFailed shouldBeEqualTo false
        controller.state.value.items.shouldBeEmpty()
    }

    @Test
    fun `once loaded, losing the connection keeps showing that there are none`() = runTest {
        val controller = controller()

        connectionState.value = ConnectionState.Offline

        controller.state.value.isLoading shouldBeEqualTo false
        controller.state.value.isLoadFailed shouldBeEqualTo false
    }

    @Test
    fun `when the client already has them all, losing the connection keeps showing that there are none`() = runTest {
        hasLoadedAll.value = true
        val controller = controller()

        connectionState.value = ConnectionState.Offline
        hasLoadedAll.value = false

        controller.state.value.isLoading shouldBeEqualTo false
        controller.state.value.isLoadFailed shouldBeEqualTo false
    }

    @Test
    fun `offline with nothing known, the messages can't load`() = runTest {
        connectionState.value = ConnectionState.Offline

        val state = controller().state.value

        state.isLoading shouldBeEqualTo false
        state.isLoadFailed shouldBeEqualTo true
        verify(client, never()).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
    }

    @Test
    fun `a failed load can be tried again`() = runTest {
        whenever(client.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())).doReturn(
            Error.GenericError(randomString()).asCall(),
            QueryScheduledMessagesResult(emptyList(), next = null).asCall(),
        )
        val controller = controller()
        controller.state.value.isLoadFailed shouldBeEqualTo true

        controller.load()

        controller.state.value.isLoadFailed shouldBeEqualTo false
        verify(client, times(2)).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
    }

    @Test
    fun `a message deleted from the list that the server had already sent tells it is in the channel`() = runTest {
        val deleted = scheduled()
        whenever(client.deleteScheduledMessage(deleted.id)) doReturn Unit.asCall()
        val controller = controller()

        controller.events.test {
            deletedAlreadySent.emit(deleted)
            expectNoEvents()

            controller.delete(deleted.id)
            deletedAlreadySent.emit(scheduled())
            expectNoEvents()

            deletedAlreadySent.emit(deleted)

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.DeletedMessageAlreadySent
        }
    }

    @Test
    fun `a reschedule the server answers as already sent or deleted tells the message is gone`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = scheduled()
        val local = scheduledMessage.copy(status = ScheduledMessageStatus.PreparingToSchedule)
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.rescheduleMessage(eq(scheduledMessage.id), any())).thenAnswer {
            scheduledMessages.value = listOf(local)
            local.asCall()
        }
        val controller = controller()

        controller.events.test {
            controller.reschedule(scheduledMessage.id, randomDate())
            expectNoEvents()

            scheduledMessages.value = emptyList()

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.MessageGone
        }
    }

    @Test
    fun `a reschedule the server accepts tells nothing`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = scheduled()
        val local = scheduledMessage.copy(status = ScheduledMessageStatus.PreparingToSchedule)
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.rescheduleMessage(eq(scheduledMessage.id), any())).thenAnswer {
            scheduledMessages.value = listOf(local)
            local.asCall()
        }
        val controller = controller()

        controller.events.test {
            controller.reschedule(scheduledMessage.id, randomDate())
            scheduledMessages.value = listOf(scheduledMessage)

            expectNoEvents()
        }
    }

    @Test
    fun `a message deleted while its reschedule is on the way does not tell it is gone`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = scheduled()
        val local = scheduledMessage.copy(status = ScheduledMessageStatus.PreparingToSchedule)
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.rescheduleMessage(eq(scheduledMessage.id), any())).thenAnswer {
            scheduledMessages.value = listOf(local)
            local.asCall()
        }
        whenever(client.deleteScheduledMessage(scheduledMessage.id)).thenAnswer {
            scheduledMessages.value = emptyList()
            Unit.asCall()
        }
        val controller = controller()

        controller.events.test {
            controller.reschedule(scheduledMessage.id, randomDate())
            controller.delete(scheduledMessage.id)

            expectNoEvents()
        }
    }

    @Test
    fun `a reschedule of a message the server failed to send schedules a new message, then deletes the failed one`() =
        runTest {
            hasLoadedAll.value = true
            val failedMessage = failed(ServerFailedPublishing(reason = null))
            scheduledMessages.value = listOf(failedMessage)
            val (channelType, channelId) = failedMessage.cid.split(":")
            val scheduledAt = randomDate()
            whenever(client.scheduleMessage(any(), any(), any(), any())) doReturn scheduled().asCall()
            whenever(client.deleteScheduledMessage(failedMessage.id)) doReturn Unit.asCall()
            val controller = controller()

            controller.events.test {
                controller.reschedule(failedMessage.id, scheduledAt)

                expectNoEvents()
            }
            val content = failedMessage.editedContent(failedMessage.message, isQuoteRemoved = false)
            val inOrder = inOrder(client)
            inOrder.verify(client).scheduleMessage(channelType, channelId, content.copy(id = ""), scheduledAt)
            inOrder.verify(client).deleteScheduledMessage(failedMessage.id)
            verify(client, never()).rescheduleMessage(any(), any())
        }

    @Test
    fun `a refused reschedule of a message the server failed to send keeps it and tells it failed`() = runTest {
        hasLoadedAll.value = true
        val failedMessage = failed(ServerFailedPublishing(reason = null))
        scheduledMessages.value = listOf(failedMessage)
        val error = Error.GenericError(randomString())
        whenever(client.scheduleMessage(any(), any(), any(), any())) doReturn error.asCall()
        val controller = controller()

        controller.events.test {
            controller.reschedule(failedMessage.id, randomDate())

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.ActionFailed(error)
        }
        verify(client, never()).deleteScheduledMessage(any())
    }

    @Test
    fun `send now while offline tells it is queued until online`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = scheduled()
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.sendScheduledMessageNow(scheduledMessage.id)) doReturn scheduledMessage.asCall()
        val controller = controller()
        connectionState.value = ConnectionState.Offline

        controller.events.test {
            controller.sendNow(scheduledMessage.id)

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.SendNowQueued
        }
    }

    @Test
    fun `send now while reconnecting tells it is queued until connected`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = scheduled()
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.sendScheduledMessageNow(scheduledMessage.id)) doReturn scheduledMessage.asCall()
        val controller = controller()
        connectionState.value = ConnectionState.Connecting

        controller.events.test {
            controller.sendNow(scheduledMessage.id)

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.SendNowQueued
        }
    }

    @Test
    fun `send now while online tells it is sent`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = scheduled()
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.sendScheduledMessageNow(scheduledMessage.id)) doReturn scheduledMessage.asCall()
        val controller = controller()

        controller.events.test {
            controller.sendNow(scheduledMessage.id)

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.SentNow
        }
    }

    @Test
    fun `an action on a message that is no longer there tells it is gone`() = runTest {
        hasLoadedAll.value = true
        val id = randomString()
        whenever(client.sendScheduledMessageNow(id)) doReturn Error.GenericError(randomString()).asCall()
        val controller = controller()

        controller.events.test {
            controller.sendNow(id)

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.MessageGone
        }
    }

    @Test
    fun `a send now refused for a deleted quote shows the deleted quote`() = runTest {
        hasLoadedAll.value = true
        val quoted = randomMessage(deletedAt = null)
        val scheduledMessage = scheduled(replyTo = quoted)
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.sendScheduledMessageNow(scheduledMessage.id)) doReturn
            Error.GenericError(randomString()).asCall()
        val controller = controller()
        controller.state.value.items.single().isQuoteDeleted shouldBeEqualTo false

        controller.events.test {
            quotedMessages[quoted.id] = quoted.copy(deletedAt = Date())
            controller.sendNow(scheduledMessage.id)

            expectNoEvents()
        }
        controller.state.value.items.single().isQuoteDeleted shouldBeEqualTo true
    }

    @Test
    fun `an action refused while the quote is not deleted keeps the quote of the message`() = runTest {
        hasLoadedAll.value = true
        val quoted = randomMessage(deletedAt = null)
        quotedMessages[quoted.id] = quoted.copy(text = randomString())
        val scheduledMessage = scheduled(replyTo = quoted)
        val error = Error.GenericError(randomString())
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.sendScheduledMessageNow(scheduledMessage.id)) doReturn error.asCall()
        val controller = controller()

        controller.events.test {
            controller.sendNow(scheduledMessage.id)

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.ActionFailed(error)
        }
        loadedMessageIds shouldBeEqualTo listOf(quoted.id)
        controller.state.value.items.single().quotedMessage shouldBeEqualTo quoted
    }

    @Test
    fun `any other refused action tells it failed`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = scheduled()
        val error = Error.GenericError(randomString())
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.rescheduleMessage(eq(scheduledMessage.id), any())) doReturn error.asCall()
        val controller = controller()

        controller.events.test {
            controller.reschedule(scheduledMessage.id, randomDate())

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.ActionFailed(error)
        }
    }

    @Test
    fun `the message to edit comes with the users it mentions`() = runTest {
        val user = randomUser()
        val scheduledMessage = scheduled(mentionedUsersIds = listOf(user.id))
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.queryUsers(any())) doReturn listOf(user).asCall()

        controller().messageToEdit(scheduledMessage.id) shouldBeEqualTo
            scheduledMessage.message.copy(mentionedUsers = listOf(user))
    }

    @Test
    fun `the message to edit takes the users saved on the device, without a request`() = runTest {
        val user = randomUser()
        savedUsers += user
        val scheduledMessage = scheduled(mentionedUsersIds = listOf(user.id))
        scheduledMessages.value = listOf(scheduledMessage)

        controller().messageToEdit(scheduledMessage.id) shouldBeEqualTo
            scheduledMessage.message.copy(mentionedUsers = listOf(user))
        verify(client, never()).queryUsers(any())
    }

    @Test
    fun `the message to edit loads only the users not saved on the device`() = runTest {
        val saved = randomUser()
        val loaded = randomUser()
        savedUsers += saved
        val scheduledMessage = scheduled(mentionedUsersIds = listOf(saved.id, loaded.id))
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.queryUsers(any())) doReturn listOf(loaded).asCall()

        controller().messageToEdit(scheduledMessage.id) shouldBeEqualTo
            scheduledMessage.message.copy(mentionedUsers = listOf(saved, loaded))
        verify(client).queryUsers(argThat { limit == 1 })
    }

    @Test
    fun `the message to edit has no users when they can't be loaded`() = runTest {
        val scheduledMessage = scheduled(mentionedUsersIds = listOf(randomString()))
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.queryUsers(any())) doReturn Error.GenericError(randomString()).asCall()

        controller().messageToEdit(scheduledMessage.id) shouldBeEqualTo
            scheduledMessage.message.copy(mentionedUsers = emptyList())
    }

    @Test
    fun `there is no message to edit once it is gone, which is told`() = runTest {
        val controller = controller()

        controller.events.test {
            controller.messageToEdit(randomString()) shouldBeEqualTo null

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.MessageGone
        }
    }

    @Test
    fun `an edit saves the content and keeps the date, then calls back`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = scheduled()
        scheduledMessages.value = listOf(scheduledMessage)
        val composed = scheduledMessage.message.copy(text = "Edited")
        whenever(client.updateScheduledMessage(eq(scheduledMessage.id), any(), isNull())) doReturn
            scheduledMessage.asCall()
        val results = mutableListOf<Result<ScheduledMessage>>()

        controller().edit(scheduledMessage.id, composed, isQuoteRemoved = false, onResult = { results += it })

        verify(client).updateScheduledMessage(scheduledMessage.id, composed, null)
        results shouldBeEqualTo listOf(Result.Success(scheduledMessage))
    }

    @Test
    fun `an edit resolves the attachments picked in the composer`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = scheduled()
        scheduledMessages.value = listOf(scheduledMessage)
        val resolved = listOf(Attachment(name = "resolved"))
        whenever(client.updateScheduledMessage(eq(scheduledMessage.id), any(), isNull())) doReturn
            scheduledMessage.asCall()

        controller().edit(
            id = scheduledMessage.id,
            message = scheduledMessage.message.copy(attachments = listOf(Attachment(name = "picked"))),
            isQuoteRemoved = false,
            resolveAttachments = { resolved },
        )

        verify(client).updateScheduledMessage(
            scheduledMessage.id,
            scheduledMessage.message.copy(attachments = resolved),
            null,
        )
    }

    @Test
    fun `an edit refused before it is saved tells it failed`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = scheduled()
        scheduledMessages.value = listOf(scheduledMessage)
        val error = Error.GenericError(randomString())
        whenever(client.updateScheduledMessage(eq(scheduledMessage.id), any(), isNull())) doReturn error.asCall()
        val controller = controller()
        val results = mutableListOf<Result<ScheduledMessage>>()

        controller.events.test {
            controller.edit(scheduledMessage.id, scheduledMessage.message, isQuoteRemoved = false) { results += it }

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.ActionFailed(error)
        }
        results shouldBeEqualTo listOf(Result.Failure(error))
    }

    @Test
    fun `an edit of a message that is gone tells it is gone, without a request`() = runTest {
        hasLoadedAll.value = true
        val controller = controller()

        controller.events.test {
            controller.edit(randomString(), randomMessage(), isQuoteRemoved = false)

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.MessageGone
        }
        verify(client, never()).updateScheduledMessage(any(), any(), anyOrNull())
    }

    @Test
    fun `an edit the server answers as already sent or deleted tells the message is gone`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = scheduled()
        val local = scheduledMessage.copy(status = ScheduledMessageStatus.PreparingToSchedule)
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.updateScheduledMessage(eq(scheduledMessage.id), any(), isNull())).thenAnswer {
            scheduledMessages.value = listOf(local)
            local.asCall()
        }
        val controller = controller()

        controller.events.test {
            controller.edit(scheduledMessage.id, scheduledMessage.message, isQuoteRemoved = false)
            expectNoEvents()

            scheduledMessages.value = emptyList()

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.MessageGone
        }
    }

    @Test
    fun `edit and reschedule schedules a new message, then deletes the failed one`() = runTest {
        hasLoadedAll.value = true
        val failedMessage = failed(ServerFailedPublishing(reason = null))
        scheduledMessages.value = listOf(failedMessage)
        val (channelType, channelId) = failedMessage.cid.split(":")
        val scheduledAt = randomDate()
        val composed = failedMessage.message.copy(text = "Edited")
        val newMessage = scheduled()
        whenever(client.scheduleMessage(any(), any(), any(), any())) doReturn newMessage.asCall()
        var deletes = 0
        whenever(client.deleteScheduledMessage(failedMessage.id)).thenAnswer {
            deletes++
            Unit.asCall()
        }
        // The failed message is deleted before the caller learns the result.
        val deletesAtResult = mutableListOf<Int>()
        val results = mutableListOf<Result<ScheduledMessage>>()
        val controller = controller()

        controller.events.test {
            controller.editAndReschedule(failedMessage.id, composed, false, scheduledAt) {
                deletesAtResult += deletes
                results += it
            }

            expectNoEvents()
        }
        verify(client).scheduleMessage(channelType, channelId, composed.copy(id = ""), scheduledAt)
        results shouldBeEqualTo listOf(Result.Success(newMessage))
        deletesAtResult shouldBeEqualTo listOf(1)
    }

    @Test
    fun `edit and reschedule calls back only once the failed message is deleted`() = runTest {
        hasLoadedAll.value = true
        val failedMessage = failed(ServerFailedPublishing(reason = null))
        scheduledMessages.value = listOf(failedMessage)
        val newMessage = scheduled()
        val deleted = CompletableDeferred<Result<Unit>>()
        whenever(client.scheduleMessage(any(), any(), any(), any())) doReturn newMessage.asCall()
        whenever(client.deleteScheduledMessage(failedMessage.id)) doReturn
            CoroutineCall(backgroundScope) { deleted.await() }
        val results = mutableListOf<Result<ScheduledMessage>>()

        controller().editAndReschedule(failedMessage.id, failedMessage.message, false, randomDate()) { results += it }
        runCurrent()
        results.shouldBeEmpty()

        deleted.complete(Result.Success(Unit))
        runCurrent()
        results shouldBeEqualTo listOf(Result.Success(newMessage))
    }

    @Test
    fun `edit and reschedule still deletes the failed message when the controller is closed while scheduling`() =
        runTest {
            hasLoadedAll.value = true
            val failedMessage = failed(ServerFailedPublishing(reason = null))
            scheduledMessages.value = listOf(failedMessage)
            val scheduled = CompletableDeferred<Result<ScheduledMessage>>()
            whenever(client.scheduleMessage(any(), any(), any(), any())) doReturn
                CoroutineCall(backgroundScope) { scheduled.await() }
            whenever(client.deleteScheduledMessage(failedMessage.id)) doReturn Unit.asCall()
            val controller = controller()
            controller.editAndReschedule(failedMessage.id, failedMessage.message, false, randomDate())
            runCurrent()

            controller.close()
            scheduled.complete(Result.Success(scheduled()))
            runCurrent()

            verify(client).deleteScheduledMessage(failedMessage.id)
        }

    @Test
    fun `edit and reschedule refused before it is saved keeps the failed message`() = runTest {
        hasLoadedAll.value = true
        val failedMessage = failed(ServerFailedPublishing(reason = null))
        scheduledMessages.value = listOf(failedMessage)
        val error = Error.GenericError(randomString())
        whenever(client.scheduleMessage(any(), any(), any(), any())) doReturn error.asCall()
        val controller = controller()

        controller.events.test {
            controller.editAndReschedule(failedMessage.id, failedMessage.message, false, randomDate())

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.ActionFailed(error)
        }
        verify(client, never()).deleteScheduledMessage(any())
    }

    @Test
    fun `edit and reschedule refused with the deleted quote removed tells it failed`() = runTest {
        hasLoadedAll.value = true
        val quoted = randomMessage(deletedAt = Date())
        quotedMessages[quoted.id] = quoted
        val failedMessage = failed(ServerFailedPublishing(reason = null)).let {
            it.copy(message = it.message.copy(replyMessageId = quoted.id))
        }
        scheduledMessages.value = listOf(failedMessage)
        val error = Error.GenericError(randomString())
        whenever(client.scheduleMessage(any(), any(), any(), any())) doReturn error.asCall()
        val controller = controller()

        controller.events.test {
            controller.editAndReschedule(failedMessage.id, failedMessage.message, true, randomDate())

            awaitItem() shouldBeEqualTo ScheduledMessageListEvent.ActionFailed(error)
        }
    }

    @Test
    fun `the edited content keeps what the composer does not edit`() {
        val scheduledMessage = scheduled(replyMessageId = randomString()).let {
            it.copy(
                message = it.message.copy(
                    extraData = mapOf("custom" to "kept"),
                    mentionedChannel = false,
                    mentionedHere = false,
                ),
            )
        }
        // A composer restored after a process restart knows the text only.
        val composed = Message(id = scheduledMessage.id, text = "Edited @channel", mentionedChannel = true)

        val content = scheduledMessage.editedContent(composed, isQuoteRemoved = false)

        content shouldBeEqualTo scheduledMessage.message.copy(text = "Edited @channel", mentionedChannel = true)
    }

    @Test
    fun `the edited content keeps the mentions of the users the composer did not know`() {
        val known = randomUser()
        val scheduledMessage = scheduled(mentionedUsersIds = listOf(known.id, "unknown"))
        // The composer kept no mention of the known user, whose name left the text.
        val composed = scheduledMessage.message.copy(mentionedUsers = listOf(known), mentionedUsersIds = emptyList())

        val content = scheduledMessage.editedContent(composed, isQuoteRemoved = false)

        content.mentionedUsersIds shouldBeEqualTo listOf("unknown")
        content.mentionedUsers.shouldBeEmpty()
    }

    @Test
    fun `the edited content drops the quote once it is removed`() {
        val scheduledMessage = scheduled(replyMessageId = randomString())

        val content = scheduledMessage.editedContent(scheduledMessage.message, isQuoteRemoved = true)

        content.replyMessageId shouldBeEqualTo null
        content.replyTo shouldBeEqualTo null
    }

    @Test
    fun `the edited content drops the mml a backend set`() {
        val scheduledMessage = scheduled().let {
            it.copy(message = it.message.copy(extraData = mapOf("mml" to "<mml/>", "custom" to "kept")))
        }

        val content = scheduledMessage.editedContent(scheduledMessage.message, isQuoteRemoved = false)

        content.extraData shouldBeEqualTo mapOf("custom" to "kept")
    }

    private fun controller(cid: String? = null, parentMessageId: String? = null) = ScheduledMessageListController(
        cid = cid,
        parentMessageId = parentMessageId,
        chatClient = client,
        getMessage = { id ->
            loadedMessageIds += id
            quotedMessages[id]?.asCall() ?: Error.GenericError(id).asCall()
        },
    )

    private fun ScheduledMessage.actions() = scheduledMessageActions(isQuoteDeleted = false, canSchedule = true)

    private fun ScheduledMessage.actionsWithoutScheduling() =
        scheduledMessageActions(isQuoteDeleted = false, canSchedule = false)

    private fun capabilities(canSchedule: Boolean): Set<String> {
        val others = randomChannelCapabilities(exclude = setOf(SCHEDULE_MESSAGE))
        return if (canSchedule) others + SCHEDULE_MESSAGE else others
    }

    private suspend fun givenSavedChannels(vararg channels: Channel) {
        channels.forEach { channel -> whenever(repository.selectChannel(channel.cid)) doReturn channel }
    }

    private fun ScheduledMessage.withSummary(name: String, members: List<Member> = emptyList()) = copy(
        channel = ChannelInfo(
            cid = cid,
            id = cid.substringAfter(':'),
            type = "messaging",
            name = name,
            members = members,
        ),
    )

    private fun failed(failure: ScheduledMessageFailure) =
        scheduled(status = ScheduledMessageStatus.Failed).copy(failure = failure)

    @Suppress("LongParameterList")
    private fun scheduled(
        cid: String = randomCID(),
        scheduledAt: Date = randomDate(),
        status: ScheduledMessageStatus = ScheduledMessageStatus.Scheduled,
        text: String = randomString(),
        parentId: String? = null,
        replyTo: Message? = null,
        replyMessageId: String? = replyTo?.id,
        attachments: List<Attachment> = emptyList(),
        mentionedUsersIds: List<String> = emptyList(),
    ): ScheduledMessage {
        val id = randomString()
        return randomScheduledMessage(
            id = id,
            cid = cid,
            message = randomMessage(
                id = id,
                cid = cid,
                text = text,
                parentId = parentId,
                replyTo = replyTo,
                replyMessageId = replyMessageId,
                attachments = attachments,
                mentionedUsers = emptyList(),
                extraData = emptyMap(),
            ).copy(mentionedUsersIds = mentionedUsersIds),
            scheduledAt = scheduledAt,
            status = status,
        )
    }
}
