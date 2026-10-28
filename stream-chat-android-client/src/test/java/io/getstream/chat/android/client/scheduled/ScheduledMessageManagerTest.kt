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

import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.ScheduledMessageDeletedEvent
import io.getstream.chat.android.client.events.ScheduledMessageFailedEvent
import io.getstream.chat.android.client.events.ScheduledMessageSentEvent
import io.getstream.chat.android.client.events.ScheduledMessageUpdatedEvent
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.QueryScheduledMessagesResult
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerFailedPublishing
import io.getstream.chat.android.models.ScheduledMessageQueryStatus
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomScheduledMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCall
import io.getstream.result.Result
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContainSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.Date

internal class ScheduledMessageManagerTest {

    private val fixture = ScheduledMessageManagerFixture()
    private val manager = fixture.manager
    private val repositoryFacade: RepositoryFacade = fixture.repositoryFacade
    private val cid = randomCID()

    @Test
    fun `schedule saves the message locally first, as the current user's`() = runTest {
        startOffline()
        val message = Message(id = randomString(), text = randomString(), parentId = randomString())
        val scheduledAt = randomDate()

        val scheduled = (manager.schedule(cid, message, scheduledAt) as Result.Success).value

        scheduled.id shouldBeEqualTo message.id
        scheduled.cid shouldBeEqualTo cid
        scheduled.scheduledAt shouldBeEqualTo scheduledAt
        scheduled.status shouldBeEqualTo ScheduledMessageStatus.PreparingToSchedule
        scheduled.message.user shouldBeEqualTo fixture.user
        scheduled.message.parentId shouldBeEqualTo message.parentId
        manager.scheduledMessages.value shouldBeEqualTo listOf(scheduled)
        repositoryFacade.scheduledMessageRepository.selectScheduledMessages().single().id shouldBeEqualTo message.id
        verifyNoInteractions(fixture.api)
        verify(repositoryFacade, never()).insertMessage(any())
    }

    @Test
    fun `edit and reschedule are saved locally first`() = runTest {
        val onServer = givenScheduledOnServer()
        val edited = onServer.message.copy(text = randomString())
        val scheduledAt = randomDate()

        val updated = (manager.update(onServer.id, edited, scheduledAt = null) as Result.Success).value
        val rescheduled = (manager.reschedule(onServer.id, scheduledAt) as Result.Success).value

        updated.message.text shouldBeEqualTo edited.text
        updated.status shouldBeEqualTo ScheduledMessageStatus.PreparingToSchedule
        rescheduled.scheduledAt shouldBeEqualTo scheduledAt
        rescheduled.message.text shouldBeEqualTo edited.text
        manager.scheduledMessages.value shouldBeEqualTo listOf(rescheduled)
        verifyNoInteractions(fixture.api)
    }

    @Test
    fun `a slash command is refused in a channel with commands, on schedule and edit`() = runTest {
        whenever(repositoryFacade.selectChannelConfig(cid.substringBefore(":")))
            .thenReturn(ChannelConfig(cid.substringBefore(":"), Config(commands = listOf(Command("giphy", "", "", "")))))
        val onServer = givenScheduledOnServer(randomScheduledMessage(cid = cid))
        val command = Message(id = randomString(), text = "/giphy hello")

        manager.schedule(cid, command, randomDate()).shouldBeInstanceOf<Result.Failure>()
        manager.update(onServer.id, onServer.message.copy(text = "/giphy hello"), null)
            .shouldBeInstanceOf<Result.Failure>()

        manager.scheduledMessages.value shouldBeEqualTo listOf(onServer)
        verifyNoInteractions(fixture.api)
    }

    @Test
    fun `a slash is scheduled in a channel without commands`() = runTest {
        manager.schedule(cid, Message(id = randomString(), text = "/shrug"), randomDate())
            .shouldBeInstanceOf<Result.Success<ScheduledMessage>>()
    }

    @Test
    fun `a quote of a deleted message is refused on schedule, edit, reschedule and send now`() = runTest {
        val quoted = randomMessage(deletedAt = Date())
        whenever(repositoryFacade.selectMessage(quoted.id)).thenReturn(quoted)
        val onServer = givenScheduledOnServer(
            randomScheduledMessage().let { it.copy(message = it.message.copy(replyMessageId = quoted.id)) },
        )

        listOf(
            manager.schedule(cid, Message(id = randomString(), replyMessageId = quoted.id), randomDate()),
            manager.update(onServer.id, onServer.message.copy(text = randomString()), null),
            manager.reschedule(onServer.id, randomDate()),
            manager.sendNow(onServer.id),
        ).forEach { it.shouldBeInstanceOf<Result.Failure>() }

        manager.scheduledMessages.value shouldBeEqualTo listOf(onServer)
    }

    @Test
    fun `deleting a message that is not on the server sends no request`() = runTest {
        fixture.connectionState.value = ConnectionState.Offline
        val scheduled = (manager.schedule(cid, Message(id = randomString()), randomDate()) as Result.Success).value

        manager.delete(scheduled.id) shouldBeEqualTo Result.Success(Unit)

        manager.scheduledMessages.value.shouldBeEmpty()
        verifyNoInteractions(fixture.api)
    }

    @Test
    fun `deleting a message this device does not know deletes it on the server`() = runTest {
        val id = randomString()
        whenever(fixture.api.deleteScheduledMessage(any())).thenReturn(TestCall(Result.Success(Unit)))

        manager.delete(id) shouldBeEqualTo Result.Success(Unit)

        verify(fixture.api).deleteScheduledMessage(id)
    }

    @Test
    fun `scheduling an id that is already scheduled is refused`() = runTest {
        val onServer = givenScheduledOnServer()

        manager.schedule(cid, Message(id = onServer.id, text = randomString()), randomDate())
            .shouldBeInstanceOf<Result.Failure>()

        manager.scheduledMessages.value shouldBeEqualTo listOf(onServer)
    }

    @Test
    fun `resend fails when nothing failed locally`() = runTest {
        val failedToPublish = givenScheduledOnServer(
            randomScheduledMessage(status = ScheduledMessageStatus.Failed, failure = ServerFailedPublishing(null)),
        )

        manager.resend(failedToPublish.id).shouldBeInstanceOf<Result.Failure>()
    }

    @Test
    fun `created, updated and failed events save the message, deleted and sent events remove it`() = runTest {
        startOffline()
        val created = randomScheduledMessage()
        val updated = created.copy(message = created.message.copy(text = randomString()))
        val failed = updated.copy(status = ScheduledMessageStatus.Failed, failure = ServerFailedPublishing(null))
        val other = randomScheduledMessage()

        manager.onEvent(scheduledMessageCreatedEvent(created))
        manager.scheduledMessages.value shouldBeEqualTo listOf(created)
        manager.onEvent(ScheduledMessageUpdatedEvent(EventType.SCHEDULED_MESSAGE_UPDATED, Date(), null, cid, "", "", updated))
        manager.scheduledMessages.value shouldBeEqualTo listOf(updated)
        manager.onEvent(ScheduledMessageFailedEvent(EventType.SCHEDULED_MESSAGE_FAILED, Date(), null, cid, "", "", failed))
        manager.scheduledMessages.value shouldBeEqualTo listOf(failed)
        manager.onEvent(ScheduledMessageDeletedEvent(EventType.SCHEDULED_MESSAGE_DELETED, Date(), null, cid, "", "", failed))
        manager.scheduledMessages.value.shouldBeEmpty()

        manager.onEvent(scheduledMessageCreatedEvent(other))
        manager.onEvent(sentEvent(other))
        manager.scheduledMessages.value.shouldBeEmpty()
        repositoryFacade.scheduledMessageRepository.selectScheduledMessages().shouldBeEmpty()
    }

    @Test
    fun `sent and message new end in the same state in either order`() = runTest {
        val scheduled = givenScheduledOnServer()
        val newMessage = newMessageEvent(scheduled.message.copy(id = randomString()))

        manager.onEvent(sentEvent(scheduled))
        manager.onEvent(newMessage)
        val sentFirst = manager.scheduledMessages.value

        givenScheduledOnServer(scheduled)
        manager.onEvent(newMessage)
        manager.onEvent(sentEvent(scheduled))

        manager.scheduledMessages.value shouldBeEqualTo sentFirst
        sentFirst.shouldBeEmpty()
    }

    @Test
    fun `a duplicate created event changes nothing`() = runTest {
        val scheduled = givenScheduledOnServer()

        manager.onEvent(scheduledMessageCreatedEvent(scheduled))

        manager.scheduledMessages.value shouldBeEqualTo listOf(scheduled)
    }

    @Test
    fun `an event keeps the local content while it has changes the server does not have`() = runTest {
        fixture.connectionState.value = ConnectionState.Offline
        val onServer = givenScheduledOnServer()
        val edited = onServer.message.copy(text = randomString())
        manager.update(onServer.id, edited, scheduledAt = null)

        manager.onEvent(scheduledMessageCreatedEvent(onServer.copy(status = ScheduledMessageStatus.Sending)))

        val scheduled = manager.scheduledMessages.value.single()
        scheduled.message.text shouldBeEqualTo edited.text
        scheduled.status shouldBeEqualTo ScheduledMessageStatus.PreparingToSchedule
    }

    @Test
    fun `a full query removes the messages the server no longer has, except the ones not on the server yet`() = runTest {
        startOffline()
        val sentWhileOffline = givenScheduledOnServer()
        val kept = givenScheduledOnServer()
        val notOnServer = (manager.schedule(cid, Message(id = randomString()), randomDate()) as Result.Success).value
        val new = randomScheduledMessage()

        query(null, emptySet(), null, QueryScheduledMessagesResult(listOf(kept, new), next = null))

        manager.scheduledMessages.value.map(ScheduledMessage::id) shouldContainSame
            listOf(kept.id, new.id, notOnServer.id)
        repositoryFacade.scheduledMessageRepository.selectScheduledMessages().map { it.id } shouldContainSame
            listOf(kept.id, new.id, notOnServer.id)
        (sentWhileOffline.id in manager.scheduledMessages.value.map(ScheduledMessage::id)) shouldBeEqualTo false
    }

    @Test
    fun `a full query of a channel removes only the missing messages of that channel`() = runTest {
        val inChannel = givenScheduledOnServer(randomScheduledMessage(cid = cid))
        val elsewhere = givenScheduledOnServer()

        query(cid, emptySet(), null, QueryScheduledMessagesResult(emptyList(), next = null))

        manager.scheduledMessages.value shouldBeEqualTo listOf(elsewhere)
        (inChannel.id in manager.scheduledMessages.value.map(ScheduledMessage::id)) shouldBeEqualTo false
    }

    @Test
    fun `a full query keeps a message scheduled on the server while it ran`() = runTest {
        val scheduledMeanwhile = randomScheduledMessage()
        whenever(fixture.api.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())).thenReturn(
            SuspendingCall {
                givenScheduledOnServer(scheduledMeanwhile)
                Result.Success(QueryScheduledMessagesResult(emptyList(), next = null))
            },
        )

        manager.query(null, emptySet(), limit = 100, next = null)

        manager.scheduledMessages.value shouldBeEqualTo listOf(scheduledMeanwhile)
    }

    @Test
    fun `a partial query removes nothing`() = runTest {
        val scheduled = givenScheduledOnServer()

        query(null, setOf(ScheduledMessageQueryStatus.Failed), null, QueryScheduledMessagesResult(emptyList(), null))
        query(null, emptySet(), null, QueryScheduledMessagesResult(emptyList(), next = randomString()))
        query(null, emptySet(), randomString(), QueryScheduledMessagesResult(emptyList(), next = null))

        manager.scheduledMessages.value shouldBeEqualTo listOf(scheduled)
    }

    @Test
    fun `the scheduled messages list the pending ones first, then the failed ones, each by date`() = runTest {
        val now = Date()
        val failedEarly = randomScheduledMessage(
            scheduledAt = Date(now.time + 1_000),
            status = ScheduledMessageStatus.Failed,
            failure = ServerFailedPublishing(null),
        )
        val late = randomScheduledMessage(scheduledAt = Date(now.time + 3_000))
        val early = randomScheduledMessage(scheduledAt = Date(now.time + 2_000))

        listOf(failedEarly, late, early).forEach { givenScheduledOnServer(it) }

        manager.scheduledMessages.value shouldBeEqualTo listOf(early, late, failedEarly)
    }

    @Test
    fun `start loads the saved messages, and stop clears them from memory only`() = runTest {
        val saved = randomLocalScheduledMessage(localFailure = null)
        val repository = InMemoryScheduledMessageRepository(saved)
        val restarted = ScheduledMessageManagerFixture(repository).manager

        restarted.start(backgroundScope)
        restarted.scheduledMessages.value shouldBeEqualTo listOf(saved.toScheduledMessage())
        restarted.stop()

        restarted.scheduledMessages.value.shouldBeEmpty()
        repository.messages.values.toList() shouldBeEqualTo listOf(saved)
    }

    private suspend fun query(
        cid: String?,
        statuses: Set<ScheduledMessageQueryStatus>,
        next: String?,
        result: QueryScheduledMessagesResult,
    ) {
        whenever(fixture.api.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull()))
            .thenReturn(TestCall(Result.Success(result)))
        manager.query(cid, statuses, limit = 100, next = next)
    }

    /** Starts saving to the repository, offline so nothing is sent. */
    private suspend fun TestScope.startOffline() {
        fixture.connectionState.value = ConnectionState.Offline
        manager.start(backgroundScope)
    }

    private suspend fun givenScheduledOnServer(
        scheduledMessage: ScheduledMessage = randomScheduledMessage(),
    ): ScheduledMessage {
        manager.onEvent(scheduledMessageCreatedEvent(scheduledMessage))
        return scheduledMessage
    }

    private fun sentEvent(scheduledMessage: ScheduledMessage) = ScheduledMessageSentEvent(
        type = EventType.SCHEDULED_MESSAGE_SENT,
        createdAt = Date(),
        rawCreatedAt = null,
        cid = scheduledMessage.cid,
        channelType = "",
        channelId = "",
        scheduledMessage = scheduledMessage.copy(status = ScheduledMessageStatus.Sent),
        messageId = randomString(),
    )

    private fun newMessageEvent(message: Message) = NewMessageEvent(
        type = EventType.MESSAGE_NEW,
        createdAt = Date(),
        rawCreatedAt = "",
        user = randomUser(),
        cid = message.cid,
        channelType = "",
        channelId = "",
        message = message,
        watcherCount = 0,
        totalUnreadCount = 0,
        unreadChannels = 0,
        channelMessageCount = null,
    )
}
