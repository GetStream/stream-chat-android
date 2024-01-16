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

package io.getstream.chat.android.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.buffer.StartStopBuffer
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.event.handler.internal.utils.toChannelUserRead
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

private const val COUNT_BUFFER_LIMIT = 100

/**
 * Call responsible to handle counting of unread messages. Use this class to handle complex scenarios where simply
 * incrementing the count without any logic would create race conditions and inconsistent state. The counting is
 * delayed by StartStopBuffer<ChatEvent> and only when the SDK is not in a syncing the channels data, it realises the
 * count. The counting messages are buffered until the count can safely happen. This class doesn't postpone any other
 * logic, only counting logic.
 *
 * @param mutableState [ChannelMutableState]
 * @param globalState [GlobalState]
 * @param unreadTrigger StateFlow<Boolean> Trigger of the count with control and the SDK is able or not able to count
 * the unread messages to avoid race conditions.
 * @param countBuffer [StartStopBuffer] The buffer that holds the counting and keeps the events to be counted in a later
 * moment.
 */
internal class UnreadCountLogic(
    private val clientState: ClientState,
    private val mutableState: ChannelMutableState,
    private val globalState: GlobalState,
    private val unreadTrigger: StateFlow<Boolean>,
    private val countBuffer: StartStopBuffer<ChatEvent> = StartStopBuffer(
        suffix = "UC",
        bufferLimit = COUNT_BUFFER_LIMIT,
        customTrigger = unreadTrigger,
    ),
) {

    init {
        countBuffer.subscribe(this::handleCountEvent)
    }

    /**
     * Increments the unread count of the Channel if necessary.
     *
     * @param chatEvent [ChatEvent].
     */
    fun enqueueCount(chatEvent: ChatEvent) {
        countBuffer.enqueueData(chatEvent)
    }

    /**
     * Handles the count event accordingly with which event is passed. If the event can't be handled, it throws an
     * IllegalArgumentException.
     */
    private fun handleCountEvent(chatEvent: ChatEvent) {
        when (chatEvent) {
            is NewMessageEvent -> {
                performCount(chatEvent.message)
            }

            is NotificationMessageNewEvent -> {
                performCount(chatEvent.message)
            }

            is MessageReadEvent -> {
                mutableState.upsertReads(chatEvent.toChannelUserRead().let(::listOf))
            }

            is NotificationMarkReadEvent -> {
                mutableState.upsertReads(chatEvent.toChannelUserRead().let(::listOf))
            }

            is MarkAllReadEvent -> {
                mutableState.upsertReads(chatEvent.toChannelUserRead().let(::listOf))
            }

            else -> throw IllegalArgumentException(
                "The event ${chatEvent.javaClass.simpleName} is not handled by UnreadCountLogic",
            )
        }
    }

    /**
     * Perform count the a new message arrive.
     */
    private fun performCount(message: Message) {
        synchronized(this) {
            val messageCreateAt = message.getCreatedAtOrDefault(Date(0))
            mutableState
                .read
                .value
                ?.takeUnless { it.lastReceivedEventDate.after(messageCreateAt) }
                ?.let {
                    mutableState.upsertReads(
                        listOf(
                            it.copy(
                                lastReceivedEventDate = messageCreateAt,
                                unreadMessages = it.unreadMessages + 1,
                            ),
                        ),
                    )
                }
        }
    }

    private companion object {
        private const val TAG = "UnreadCountLogic"
    }
}
