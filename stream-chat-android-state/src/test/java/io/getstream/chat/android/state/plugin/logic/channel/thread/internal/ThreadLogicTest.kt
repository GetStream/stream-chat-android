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

package io.getstream.chat.android.state.plugin.logic.channel.thread.internal

import io.getstream.chat.android.client.events.HasReminder
import io.getstream.chat.android.client.extensions.internal.toMessageReminderInfo
import io.getstream.chat.android.client.test.randomAnswerCastedEvent
import io.getstream.chat.android.client.test.randomMessageUpdateEvent
import io.getstream.chat.android.client.test.randomNotificationReminderDueEvent
import io.getstream.chat.android.client.test.randomPollClosedEvent
import io.getstream.chat.android.client.test.randomPollDeletedEvent
import io.getstream.chat.android.client.test.randomPollUpdatedEvent
import io.getstream.chat.android.client.test.randomReminderCreatedEvent
import io.getstream.chat.android.client.test.randomReminderDeletedEvent
import io.getstream.chat.android.client.test.randomReminderUpdatedEvent
import io.getstream.chat.android.client.test.randomVoteCastedEvent
import io.getstream.chat.android.client.test.randomVoteChangedEvent
import io.getstream.chat.android.client.test.randomVoteRemovedEvent
import io.getstream.chat.android.models.MessageReminderInfo
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageReminder
import io.getstream.chat.android.randomOption
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.state.plugin.state.channel.thread.internal.ThreadMutableState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class ThreadLogicTest {

    private lateinit var threadMutableState: ThreadMutableState
    private lateinit var threadStateLogic: ThreadStateLogic
    private lateinit var threadLogic: ThreadLogic

    @BeforeEach
    fun setUp() {
        threadMutableState = mock()
        threadStateLogic = mock()
        whenever(threadStateLogic.writeThreadState()).doReturn(threadMutableState)
        threadLogic = ThreadLogic(threadStateLogic)
    }

    @Test
    fun `Given ReminderCreatedEvent When handleReminderEvents is called Should upsert message with reminder info`() {
        // given
        val messageId = randomString()
        val existingMessage = randomMessage(id = messageId)
        val reminder = randomMessageReminder(messageId = messageId, message = existingMessage)
        val event = randomReminderCreatedEvent(messageId = messageId, reminder = reminder)

        whenever(threadMutableState.rawMessage)
            .doReturn(MutableStateFlow(mapOf(messageId to existingMessage)))

        // when
        threadLogic.handleReminderEvents(listOf(event))

        // then
        val expectedMessage = existingMessage.copy(reminder = reminder.toMessageReminderInfo())
        verify(threadStateLogic, times(1)).upsertMessages(listOf(expectedMessage))
    }

    @Test
    fun `Given ReminderUpdatedEvent When handleReminderEvents is called Should upsert message with updated reminder info`() {
        // given
        val messageId = randomString()
        val existingMessage = randomMessage(id = messageId)
        val reminder = randomMessageReminder(messageId = messageId, message = existingMessage)
        val event = randomReminderUpdatedEvent(messageId = messageId, reminder = reminder)

        whenever(threadMutableState.rawMessage)
            .doReturn(MutableStateFlow(mapOf(messageId to existingMessage)))

        // when
        threadLogic.handleReminderEvents(listOf(event))

        // then
        val expectedMessage = existingMessage.copy(reminder = reminder.toMessageReminderInfo())
        verify(threadStateLogic, times(1)).upsertMessages(listOf(expectedMessage))
    }

    @Test
    fun `Given ReminderDeletedEvent When handleReminderEvents is called Should upsert message with null reminder`() {
        // given
        val messageId = randomString()
        val existingMessage =
            randomMessage(id = messageId, reminder = MessageReminderInfo(Date(), randomDate(), randomDate()))
        val reminder = randomMessageReminder(messageId = messageId, message = existingMessage)
        val event = randomReminderDeletedEvent(messageId = messageId, reminder = reminder)

        whenever(threadMutableState.rawMessage)
            .doReturn(MutableStateFlow(mapOf(messageId to existingMessage)))

        // when
        threadLogic.handleReminderEvents(listOf(event))

        // then
        val expectedMessage = existingMessage.copy(reminder = null)
        verify(threadStateLogic, times(1)).upsertMessages(listOf(expectedMessage))
    }

    @Test
    fun `Given NotificationReminderDueEvent When handleReminderEvents is called Should not upsert message`() {
        // given
        val messageId = randomString()
        val existingMessage = randomMessage(id = messageId)
        val reminder = randomMessageReminder(messageId = messageId, message = existingMessage)
        val event = randomNotificationReminderDueEvent(messageId = messageId, reminder = reminder)

        whenever(threadMutableState.rawMessage)
            .doReturn(MutableStateFlow(mapOf(messageId to existingMessage)))

        // when
        threadLogic.handleReminderEvents(listOf(event))

        // then
        verify(threadStateLogic, never()).upsertMessages(any())
    }

    @Test
    fun `Given event with non-existing message When handleReminderEvents is called Should not upsert message`() {
        // given
        val messageId = randomString()
        val reminder = randomMessageReminder(messageId = messageId)
        val event = randomReminderCreatedEvent(messageId = messageId, reminder = reminder)

        whenever(threadMutableState.rawMessage).doReturn(MutableStateFlow(emptyMap()))

        // when
        threadLogic.handleReminderEvents(listOf(event))

        // then
        verify(threadStateLogic, never()).upsertMessages(any())
    }

    @Test
    fun `Given multiple reminder events When handleReminderEvents is called Should upsert all valid messages`() {
        // given
        val messageId1 = randomString()
        val messageId2 = randomString()
        val messageId3 = randomString()
        val existingMessage1 = randomMessage(id = messageId1)
        val existingMessage2 = randomMessage(id = messageId2)
        val existingMessage3 = randomMessage(id = messageId3)
        val reminder1 = randomMessageReminder(messageId = messageId1, message = existingMessage1)
        val reminder2 = randomMessageReminder(messageId = messageId2, message = existingMessage2)
        val reminder3 = randomMessageReminder(messageId = messageId3, message = existingMessage3)

        val createdEvent = randomReminderCreatedEvent(messageId = messageId1, reminder = reminder1)

        val updatedEvent = randomReminderUpdatedEvent(messageId = messageId2, reminder = reminder2)

        val deletedEvent = randomReminderDeletedEvent(messageId = messageId3, reminder = reminder3)

        whenever(threadMutableState.rawMessage)
            .doReturn(
                MutableStateFlow(
                    mapOf(
                        messageId1 to existingMessage1,
                        messageId2 to existingMessage2,
                        messageId3 to existingMessage3,
                    ),
                ),
            )

        // when
        threadLogic.handleReminderEvents(listOf(createdEvent, updatedEvent, deletedEvent))

        // then
        val expectedMessage1 = existingMessage1.copy(reminder = reminder1.toMessageReminderInfo())
        val expectedMessage2 = existingMessage2.copy(reminder = reminder2.toMessageReminderInfo())
        val expectedMessage3 = existingMessage3.copy(reminder = null)
        val expectedMessages = listOf(expectedMessage1, expectedMessage2, expectedMessage3)

        verify(threadStateLogic, times(1)).upsertMessages(expectedMessages)
    }

    @Test
    fun `Given mixed events with some non-existing messages When handleReminderEvents is called Should only upsert valid messages`() {
        // given
        val messageId1 = randomString()
        val messageId2 = randomString()
        val messageId3 = randomString()
        val existingMessage1 = randomMessage(id = messageId1)
        val existingMessage3 = randomMessage(id = messageId3)
        val reminder1 = randomMessageReminder(messageId = messageId1, message = existingMessage1)
        val reminder2 = randomMessageReminder(messageId = messageId2, message = randomMessage(id = messageId2))
        val reminder3 = randomMessageReminder(messageId = messageId3, message = existingMessage3)

        val createdEvent = randomReminderCreatedEvent(messageId = messageId1, reminder = reminder1)
        val updatedEvent = randomReminderUpdatedEvent(messageId = messageId2, reminder = reminder2)
        val deletedEvent = randomReminderDeletedEvent(messageId = messageId3, reminder = reminder3)

        whenever(threadMutableState.rawMessage)
            .doReturn(
                MutableStateFlow(
                    mapOf(
                        messageId1 to existingMessage1,
                        messageId3 to existingMessage3,
                    ),
                ),
            )

        // when
        threadLogic.handleReminderEvents(listOf(createdEvent, updatedEvent, deletedEvent))

        // then
        val expectedMessage1 = existingMessage1.copy(reminder = reminder1.toMessageReminderInfo())
        val expectedMessage3 = existingMessage3.copy(reminder = null)
        val expectedMessages = listOf(expectedMessage1, expectedMessage3)

        verify(threadStateLogic, times(1)).upsertMessages(expectedMessages)
    }

    @Test
    fun `Given empty events list When handleReminderEvents is called Should not call upsertMessages`() {
        // given
        val emptyEvents = emptyList<HasReminder>()

        // when
        threadLogic.handleReminderEvents(emptyEvents)

        // then
        verify(threadStateLogic, never()).upsertMessages(any())
    }

    @Test
    fun `Given only NotificationReminderDueEvent When handleReminderEvents is called Should not call upsertMessages`() {
        // given
        val messageId = randomString()
        val existingMessage = randomMessage(id = messageId)
        val reminder = randomMessageReminder(messageId = messageId, message = existingMessage)
        val event = randomNotificationReminderDueEvent(messageId = messageId, reminder = reminder)

        whenever(threadMutableState.rawMessage)
            .doReturn(MutableStateFlow(mapOf(messageId to existingMessage)))

        // when
        threadLogic.handleReminderEvents(listOf(event))

        // then
        verify(threadStateLogic, never()).upsertMessages(any())
    }

    @Test
    fun `Given event with existing message with reminder When ReminderCreatedEvent is handled Should replace reminder`() {
        // given
        val messageId = randomString()
        val oldReminderInfo = MessageReminderInfo(Date(), randomDate(), randomDate())
        val existingMessage = randomMessage(id = messageId, reminder = oldReminderInfo)
        val newReminder = randomMessageReminder(messageId = messageId, message = existingMessage)
        val event = randomReminderCreatedEvent(messageId = messageId, reminder = newReminder)

        whenever(threadMutableState.rawMessage)
            .doReturn(MutableStateFlow(mapOf(messageId to existingMessage)))

        // when
        threadLogic.handleReminderEvents(listOf(event))

        // then
        val expectedMessage = existingMessage.copy(reminder = newReminder.toMessageReminderInfo())
        verify(threadStateLogic, times(1)).upsertMessages(listOf(expectedMessage))
    }

    @Test
    fun `Given MessageUpdatedEvent with existing message with ownReactions When handleMessageEvents is called Should preserve original ownReactions`() {
        // given
        val messageId = randomString()
        val originalReactions = listOf(randomReaction(), randomReaction())
        val existingMessage = randomMessage(id = messageId, ownReactions = originalReactions)
        val updatedMessage = randomMessage(id = messageId, text = "Updated text", ownReactions = emptyList())
        val event = randomMessageUpdateEvent(message = updatedMessage)

        whenever(threadMutableState.rawMessage)
            .doReturn(MutableStateFlow(mapOf(messageId to existingMessage)))
        whenever(threadMutableState.messages)
            .doReturn(MutableStateFlow(emptyList()))

        // when
        threadLogic.handleMessageEvents(listOf(event))

        // then
        val expectedMessage = updatedMessage.copy(
            replyTo = null,
            ownReactions = originalReactions,
            poll = null,
        )
        verify(threadStateLogic, times(1)).upsertMessages(listOf(expectedMessage))
    }

    @Test
    fun `Given MessageUpdatedEvent with no existing message When handleMessageEvents is called Should use event ownReactions`() {
        // given
        val messageId = randomString()
        val eventReactions = listOf(randomReaction())
        val updatedMessage = randomMessage(id = messageId, ownReactions = eventReactions)
        val event = randomMessageUpdateEvent(message = updatedMessage)

        whenever(threadMutableState.rawMessage)
            .doReturn(MutableStateFlow(emptyMap()))
        whenever(threadMutableState.messages)
            .doReturn(MutableStateFlow(emptyList()))

        // when
        threadLogic.handleMessageEvents(listOf(event))

        // then
        val expectedMessage = updatedMessage.copy(
            replyTo = null,
            ownReactions = eventReactions,
            poll = null,
        )
        verify(threadStateLogic, times(1)).upsertMessages(listOf(expectedMessage))
    }

    @Test
    fun `Given MessageUpdatedEvent with existing message with poll When handleMessageEvents is called Should preserve original poll`() {
        // given
        val messageId = randomString()
        val originalPoll = randomPoll()
        val existingMessage = randomMessage(id = messageId, poll = originalPoll)
        val updatedMessage = randomMessage(id = messageId, text = "Updated text", poll = null)
        val event = randomMessageUpdateEvent(message = updatedMessage)

        whenever(threadMutableState.rawMessage)
            .doReturn(MutableStateFlow(mapOf(messageId to existingMessage)))
        whenever(threadMutableState.messages)
            .doReturn(MutableStateFlow(emptyList()))

        // when
        threadLogic.handleMessageEvents(listOf(event))

        // then
        val expectedMessage = updatedMessage.copy(
            replyTo = null,
            ownReactions = updatedMessage.ownReactions,
            poll = originalPoll,
        )
        verify(threadStateLogic, times(1)).upsertMessages(listOf(expectedMessage))
    }

    @Test
    fun `Given MessageUpdatedEvent with event poll When handleMessageEvents is called Should use event poll`() {
        // given
        val messageId = randomString()
        val existingMessage = randomMessage(id = messageId, poll = null)
        val eventPoll = randomPoll()
        val updatedMessage = randomMessage(id = messageId, text = "Updated text", poll = eventPoll)
        val event = randomMessageUpdateEvent(message = updatedMessage)

        whenever(threadMutableState.rawMessage)
            .doReturn(MutableStateFlow(mapOf(messageId to existingMessage)))
        whenever(threadMutableState.messages)
            .doReturn(MutableStateFlow(emptyList()))

        // when
        threadLogic.handleMessageEvents(listOf(event))

        // then
        val expectedMessage = updatedMessage.copy(
            replyTo = null,
            ownReactions = updatedMessage.ownReactions,
            poll = eventPoll,
        )
        verify(threadStateLogic, times(1)).upsertMessages(listOf(expectedMessage))
    }

    @Test
    fun `Given no parent message When handlePollEvents is called Should not update poll`() {
        // given
        val poll = randomPoll()
        val event = randomPollUpdatedEvent(poll = poll)

        whenever(threadMutableState.parentMessage).doReturn(null)

        // when
        threadLogic.handlePollEvents(currentUserId = randomString(), events = listOf(event))

        // then
        verify(threadMutableState, never()).updateParentMessagePoll(any())
    }

    @Test
    fun `Given parent message without poll When handlePollEvents is called Should not update poll`() {
        // given
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = null)
        val poll = randomPoll()
        val event = randomPollUpdatedEvent(poll = poll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = randomString(), events = listOf(event))

        // then
        verify(threadMutableState, never()).updateParentMessagePoll(any())
    }

    @Test
    fun `Given PollUpdatedEvent with matching poll ID When handlePollEvents is called Should update poll`() {
        // given
        val pollId = randomString()
        val currentUserId = randomString()
        val poll = randomPoll(id = pollId)
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = poll)
        val updatedPoll = randomPoll(id = pollId)
        val event = randomPollUpdatedEvent(poll = updatedPoll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = currentUserId, events = listOf(event))

        // then
        verify(threadMutableState, times(1)).updateParentMessagePoll(any())
    }

    @Test
    fun `Given PollClosedEvent with matching poll ID When handlePollEvents is called Should update poll`() {
        // given
        val pollId = randomString()
        val currentUserId = randomString()
        val poll = randomPoll(id = pollId, closed = false)
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = poll)
        val closedPoll = poll.copy(closed = true)
        val event = randomPollClosedEvent(poll = closedPoll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = currentUserId, events = listOf(event))

        // then
        verify(threadMutableState, times(1)).updateParentMessagePoll(any())
    }

    @Test
    fun `Given VoteCastedEvent with matching poll ID When handlePollEvents is called Should update poll`() {
        // given
        val pollId = randomString()
        val currentUserId = randomString()
        val poll = randomPoll(id = pollId)
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = poll)
        val updatedPoll = randomPoll(id = pollId)
        val event = randomVoteCastedEvent(poll = updatedPoll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = currentUserId, events = listOf(event))

        // then
        verify(threadMutableState, times(1)).updateParentMessagePoll(any())
    }

    @Test
    fun `Given VoteChangedEvent with matching poll ID When handlePollEvents is called Should update poll`() {
        // given
        val pollId = randomString()
        val currentUserId = randomString()
        val poll = randomPoll(id = pollId)
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = poll)
        val updatedPoll = randomPoll(id = pollId)
        val event = randomVoteChangedEvent(poll = updatedPoll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = currentUserId, events = listOf(event))

        // then
        verify(threadMutableState, times(1)).updateParentMessagePoll(any())
    }

    @Test
    fun `Given VoteRemovedEvent with matching poll ID When handlePollEvents is called Should update poll`() {
        // given
        val pollId = randomString()
        val currentUserId = randomString()
        val poll = randomPoll(id = pollId)
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = poll)
        val updatedPoll = randomPoll(id = pollId)
        val event = randomVoteRemovedEvent(poll = updatedPoll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = currentUserId, events = listOf(event))

        // then
        verify(threadMutableState, times(1)).updateParentMessagePoll(any())
    }

    @Test
    fun `Given AnswerCastedEvent with matching poll ID When handlePollEvents is called Should update poll`() {
        // given
        val pollId = randomString()
        val currentUserId = randomString()
        val poll = randomPoll(id = pollId)
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = poll)
        val updatedPoll = randomPoll(id = pollId)
        val event = randomAnswerCastedEvent(poll = updatedPoll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = currentUserId, events = listOf(event))

        // then
        verify(threadMutableState, times(1)).updateParentMessagePoll(any())
    }

    @Test
    fun `Given PollDeletedEvent with matching poll ID When handlePollEvents is called Should update poll to null`() {
        // given
        val pollId = randomString()
        val currentUserId = randomString()
        val poll = randomPoll(id = pollId)
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = poll)
        val deletedPoll = randomPoll(id = pollId)
        val event = randomPollDeletedEvent(poll = deletedPoll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = currentUserId, events = listOf(event))

        // then
        verify(threadMutableState, times(1)).updateParentMessagePoll(null)
    }

    @Test
    fun `Given event with non-matching poll ID When handlePollEvents is called Should not update poll`() {
        // given
        val pollId = randomString()
        val differentPollId = randomString()
        val currentUserId = randomString()
        val poll = randomPoll(id = pollId)
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = poll)
        val differentPoll = randomPoll(id = differentPollId)
        val event = randomPollUpdatedEvent(poll = differentPoll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = currentUserId, events = listOf(event))

        // then
        verify(threadMutableState, never()).updateParentMessagePoll(any())
    }

    @Test
    fun `Given multiple poll events with mixed poll IDs When handlePollEvents is called Should only update poll for matching events`() {
        // given
        val pollId = randomString()
        val differentPollId = randomString()
        val currentUserId = randomString()
        val poll = randomPoll(id = pollId)
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = poll)
        val matchingPoll = randomPoll(id = pollId)
        val nonMatchingPoll = randomPoll(id = differentPollId)

        val matchingEvent1 = randomPollUpdatedEvent(poll = matchingPoll)
        val nonMatchingEvent = randomVoteCastedEvent(poll = nonMatchingPoll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = currentUserId, events = listOf(matchingEvent1, nonMatchingEvent))

        // then
        verify(threadMutableState, times(1)).updateParentMessagePoll(matchingEvent1.poll)
    }

    @Test
    fun `Given batch of poll events When handlePollEvents is called Should process all events sequentially and update poll state`() {
        // given
        val pollId = randomString()
        val currentUserId = randomString()
        val optionId1 = randomOption()
        val optionId2 = randomOption()
        val initialPoll = randomPoll(
            id = pollId,
            options = listOf(optionId1, optionId2),
            voteCountsByOption = mapOf(optionId1.id to 0, optionId2.id to 0),
            closed = false,
        )
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = initialPoll)

        val updatedPoll = initialPoll.copy(
            voteCountsByOption = mapOf(optionId1.id to 2, optionId2.id to 0),
            closed = false,
        )
        val pollUpdatedEvent = randomPollUpdatedEvent(poll = updatedPoll)
        val closedPoll = updatedPoll.copy(closed = true)
        val pollClosedEvent = randomPollClosedEvent(poll = closedPoll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(
            currentUserId = currentUserId,
            events = listOf(pollUpdatedEvent, pollClosedEvent),
        )

        // then
        val expectedPoll = initialPoll.copy(
            voteCountsByOption = mapOf(optionId1.id to 2, optionId2.id to 0),
            closed = true,
        )
        verify(threadMutableState, times(1)).updateParentMessagePoll(expectedPoll)
    }

    @Test
    fun `Given batch with poll deleted at end When handlePollEvents is called Should end with null poll`() {
        // given
        val pollId = randomString()
        val currentUserId = randomString()
        val optionId = randomString()
        val initialPoll = randomPoll(
            id = pollId,
            voteCountsByOption = mapOf(optionId to 0),
            closed = false,
        )
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = initialPoll)

        val voteCastedEvent = randomVoteCastedEvent(
            poll = randomPoll(id = pollId, voteCountsByOption = mapOf(optionId to 1)),
        )
        val pollClosedEvent = randomPollClosedEvent(
            poll = randomPoll(id = pollId, voteCountsByOption = mapOf(optionId to 1), closed = true),
        )
        val pollDeletedEvent = randomPollDeletedEvent(poll = randomPoll(id = pollId))

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(
            currentUserId = currentUserId,
            events = listOf(voteCastedEvent, pollClosedEvent, pollDeletedEvent),
        )

        // then
        verify(threadMutableState, times(1)).updateParentMessagePoll(null)
    }

    @Test
    fun `Given batch with all non-matching poll IDs When handlePollEvents is called Should not update poll`() {
        // given
        val pollId = randomString()
        val differentPollId1 = randomString()
        val differentPollId2 = randomString()
        val currentUserId = randomString()
        val poll = randomPoll(id = pollId)
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = poll)

        val event1 = randomVoteCastedEvent(poll = randomPoll(id = differentPollId1))
        val event2 = randomPollUpdatedEvent(poll = randomPoll(id = differentPollId2))
        val event3 = randomPollClosedEvent(poll = randomPoll(id = differentPollId1))

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = currentUserId, events = listOf(event1, event2, event3))

        // then
        verify(threadMutableState, never()).updateParentMessagePoll(any())
    }
}
