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

import io.getstream.chat.android.client.events.AnswerCastedEvent
import io.getstream.chat.android.client.events.HasMessage
import io.getstream.chat.android.client.events.HasPoll
import io.getstream.chat.android.client.events.HasReminder
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.PollClosedEvent
import io.getstream.chat.android.client.events.PollDeletedEvent
import io.getstream.chat.android.client.events.PollUpdatedEvent
import io.getstream.chat.android.client.events.ReminderCreatedEvent
import io.getstream.chat.android.client.events.ReminderDeletedEvent
import io.getstream.chat.android.client.events.ReminderUpdatedEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.client.extensions.internal.processPoll
import io.getstream.chat.android.client.extensions.internal.toMessageReminderInfo
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.state.plugin.state.channel.thread.internal.ThreadMutableState

/** Logic class for thread state management. Implements [ThreadQueryListener] as listener for LLC requests. */
internal class ThreadLogic(
    private val threadStateLogic: ThreadStateLogic,
) {

    private val mutableState: ThreadMutableState = threadStateLogic.writeThreadState()

    fun isLoadingMessages(): Boolean = mutableState.loading.value

    internal fun setLoading(isLoading: Boolean) {
        mutableState.setLoading(isLoading)
    }

    /**
     * Returns message stored in [ThreadMutableState] if exists
     *
     * @param messageId The id of the message.
     *
     * @return [Message] if exists, null otherwise.
     */
    internal fun getMessage(messageId: String): Message? {
        return mutableState.rawMessage.value[messageId]?.copy()
    }

    internal fun stateLogic(): ThreadStateLogic {
        return threadStateLogic
    }

    internal fun deleteMessage(message: Message) {
        threadStateLogic.deleteMessage(message)
    }

    internal fun upsertMessage(message: Message) = upsertMessages(listOf(message))

    internal fun upsertMessages(messages: List<Message>) = threadStateLogic.upsertMessages(messages)

    internal fun removeLocalMessage(message: Message) {
        threadStateLogic.deleteMessage(message)
    }

    internal fun setEndOfOlderMessages(isEnd: Boolean) {
        mutableState.setEndOfOlderMessages(isEnd)
    }

    internal fun updateOldestMessageInThread(messages: List<Message>) {
        mutableState.setOldestInThread(
            messages.sortedBy { it.createdAt }
                .firstOrNull()
                ?: mutableState.oldestInThread.value,
        )
    }

    internal fun setEndOfNewerMessages(isEnd: Boolean) {
        mutableState.setEndOfNewerMessages(isEnd)
    }

    internal fun updateNewestMessageInThread(messages: List<Message>) {
        mutableState.setNewestInThread(
            messages.sortedBy { it.createdAt }
                .lastOrNull()
                ?: mutableState.newestInThread.value,
        )
    }

    internal fun handleMessageEvents(events: List<HasMessage>) {
        val messages = events
            .map { event ->
                val originalMessage = getMessage(event.message.id)
                val ownReactions = originalMessage?.ownReactions ?: event.message.ownReactions
                // Enrich the poll as might not be present in the event
                val poll = event.message.poll ?: originalMessage?.poll
                if (event is MessageUpdatedEvent) {
                    event.message.copy(
                        replyTo = mutableState.messages.value.firstOrNull { it.id == event.message.replyMessageId },
                        ownReactions = ownReactions,
                        poll = poll,
                    )
                } else {
                    event.message.copy(
                        ownReactions = ownReactions,
                    )
                }
            }
        upsertMessages(messages)
    }

    internal fun handleReminderEvents(events: List<HasReminder>) {
        val messages = events.mapNotNull { event ->
            val message = getMessage(event.reminder.messageId)
            when (event) {
                is ReminderCreatedEvent,
                is ReminderUpdatedEvent,
                -> {
                    message?.copy(reminder = event.reminder.toMessageReminderInfo())
                }
                is ReminderDeletedEvent -> {
                    message?.copy(reminder = null)
                }
                else -> return@mapNotNull null
            }
        }
        if (messages.isNotEmpty()) {
            upsertMessages(messages)
        }
    }

    internal fun handlePollEvents(currentUserId: String?, events: List<HasPoll>) {
        // Don't handle poll events if there is no poll in the parent message (should never happen)
        val parentMessage = mutableState.parentMessage ?: return
        val poll = parentMessage.poll ?: return
        // The processed poll after applying each event sequentially
        var processedPoll: Poll? = poll
        // Don't handle poll events if the poll in the parent message is different (should never happen)
        events
            .filter { it.poll.id == poll.id }
            .forEach { event ->
                processedPoll = when (event) {
                    is AnswerCastedEvent -> event.processPoll { processedPoll }
                    is PollClosedEvent -> event.processPoll { processedPoll }
                    is PollUpdatedEvent -> event.processPoll { processedPoll }
                    is VoteRemovedEvent -> event.processPoll { processedPoll }
                    is VoteCastedEvent -> event.processPoll(currentUserId) { processedPoll }
                    is VoteChangedEvent -> event.processPoll(currentUserId) { processedPoll }
                    is PollDeletedEvent -> null // poll is deleted, remove from state
                }
            }
        if (processedPoll != poll) {
            mutableState.updateParentMessagePoll(processedPoll)
        }
    }
}
