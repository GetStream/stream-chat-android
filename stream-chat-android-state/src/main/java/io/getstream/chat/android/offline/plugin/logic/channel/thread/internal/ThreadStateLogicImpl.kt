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

import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.state.channel.thread.internal.ThreadMutableState
import io.getstream.chat.android.offline.plugin.state.channel.thread.internal.toMutableState

/**
 * The logic of the state of a thread. This class contains the logic of how to
 * update the state of the thread in the SDK.
 *
 * @property mutableState [ThreadMutableState]
 */
internal class ThreadStateLogicImpl(
    private val mutableState: ThreadMutableState
) : ThreadStateLogic {

    /**
     * Return [ThreadMutableState] representing the state of the thread. Use this when you would like to
     * keep track of the state and would like to write a new state too.
     */
    override fun writeThreadState(): ThreadMutableState {
        return mutableState.toMutableState()
    }

    /**
     * Deletes a message for the thread
     *
     * @param message [Message]
     */
    override fun deleteMessage(message: Message) {
        mutableState.rawMessages -= message.id
    }

    /**
     * Upsert message in the thread.
     *
     * @param message The message to be added or updated.
     */
    override fun upsertMessage(message: Message) = upsertMessages(listOf(message))

    /**
     * Upsert messages in the channel.
     *
     * @param messages the list of [Message] to be upserted
     * new messages should be kept.
     */
    override fun upsertMessages(messages: List<Message>) {
        val newMessages = parseMessages(messages)
        mutableState.rawMessages = newMessages
    }

    /**
     * Removes local message. Doesn't remove message in database.
     *
     * @param message The [Message] to be deleted.
     */
    override fun removeLocalMessage(message: Message) {
        mutableState.rawMessages -= message.id
    }

    /**
     * Updates [ThreadMutableState.rawMessages] with new messages.
     * The message will by only updated if its creation/update date is newer than the one stored in the StateFlow.
     *
     * @param messages The list of messages to update.
     */
    private fun parseMessages(messages: List<Message>): Map<String, Message> {
        val currentMessages = mutableState.rawMessages
        return currentMessages + messages
            .filter { newMessage -> isMessageNewerThanCurrent(currentMessages[newMessage.id], newMessage) }
            .associateBy(Message::id)
    }

    private fun isMessageNewerThanCurrent(currentMessage: Message?, newMessage: Message): Boolean {
        return if (newMessage.syncStatus == SyncStatus.COMPLETED) {
            (currentMessage?.lastUpdateTime() ?: NEVER.time) <= newMessage.lastUpdateTime()
        } else {
            (currentMessage?.lastLocalUpdateTime() ?: NEVER.time) <= newMessage.lastLocalUpdateTime()
        }
    }

    private fun Message.lastUpdateTime(): Long = listOfNotNull(
        createdAt,
        updatedAt,
        deletedAt,
    ).maxOfOrNull { it.time }
        ?: NEVER.time

    private fun Message.lastLocalUpdateTime(): Long = listOfNotNull(
        createdLocallyAt,
        updatedLocallyAt,
        deletedAt,
    ).maxOfOrNull { it.time }
        ?: NEVER.time
}
