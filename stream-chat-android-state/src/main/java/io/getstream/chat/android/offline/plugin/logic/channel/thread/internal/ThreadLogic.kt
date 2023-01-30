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

package io.getstream.chat.android.offline.plugin.logic.channel.thread.internal

import io.getstream.chat.android.client.events.HasMessage
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.offline.plugin.state.channel.thread.internal.ThreadMutableState

/** Logic class for thread state management. Implements [ThreadQueryListener] as listener for LLC requests. */
internal class ThreadLogic(
    private val threadStateLogic: ThreadStateLogic,
) {

    private val mutableState: ThreadMutableState = threadStateLogic.writeThreadState()

    fun isLoadingOlderMessages(): Boolean = mutableState.loadingOlderMessages.value

    fun isLoadingMessages(): Boolean = mutableState.loading.value

    internal fun setLoading(isLoading: Boolean) {
        mutableState.setLoading(isLoading)
    }

    internal fun setLoadingOlderMessages(isLoading: Boolean) {
        mutableState.setLoadingOlderMessages(isLoading)
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
                ?: mutableState.oldestInThread.value
        )
    }

    internal fun handleEvents(events: List<HasMessage>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    private fun handleEvent(event: HasMessage) {
        when (event) {
            is MessageUpdatedEvent -> {
                event.message.apply {
                    replyTo = mutableState.messages.value.firstOrNull { it.id == replyMessageId }
                }.let(::upsertMessage)
            }
            is NewMessageEvent,
            is MessageDeletedEvent,
            is NotificationMessageNewEvent,
            is ReactionNewEvent,
            is ReactionUpdateEvent,
            is ReactionDeletedEvent,
            -> {
                upsertMessage(event.message)
            }
            else -> Unit
        }
    }
}
