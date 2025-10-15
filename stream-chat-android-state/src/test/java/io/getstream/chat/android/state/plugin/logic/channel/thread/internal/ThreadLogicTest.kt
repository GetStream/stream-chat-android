/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.randomPoll
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
        val existingMessage = randomMessage(id = messageId, reminder = MessageReminderInfo(Date(), randomDate(), randomDate()))
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
        val poll = randomPoll(id = pollId)
        val parentMessageId = randomString()
        val parentMessage = randomMessage(id = parentMessageId, poll = poll)
        val updatedPoll = randomPoll(id = pollId)
        val event = randomPollClosedEvent(poll = updatedPoll)

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
        val matchingEvent2 = randomPollClosedEvent(poll = matchingPoll)

        whenever(threadMutableState.parentMessage).doReturn(parentMessage)

        // when
        threadLogic.handlePollEvents(currentUserId = currentUserId, events = listOf(matchingEvent1, nonMatchingEvent, matchingEvent2))

        // then
        verify(threadMutableState, times(2)).updateParentMessagePoll(any())
    }
}
