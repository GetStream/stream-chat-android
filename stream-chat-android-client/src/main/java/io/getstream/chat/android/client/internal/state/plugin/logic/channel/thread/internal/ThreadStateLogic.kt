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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.thread.internal

import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.extensions.internal.hasOutdatedMemberInfo
import io.getstream.chat.android.client.extensions.internal.withRefreshedMemberInfo
import io.getstream.chat.android.client.internal.state.plugin.state.channel.thread.internal.ThreadMutableState
import io.getstream.chat.android.models.MemberInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus

/**
 * The logic of the state of a thread. This class contains the logic of how to
 * update the state of the thread in the SDK.
 *
 * @property mutableState [ThreadMutableState]
 */
internal class ThreadStateLogic(private val mutableState: ThreadMutableState) {

    /**
     * Return [ThreadMutableState] representing the state of the thread. Use this when you would like to
     * keep track of the state and would like to write a new state too.
     */
    fun writeThreadState(): ThreadMutableState = mutableState

    /**
     * Deletes a message for the thread
     *
     * @param message [Message]
     */
    fun deleteMessage(message: Message) {
        mutableState.deleteMessage(message)
    }

    /**
     * Upsert message in the thread.
     *
     * @param message The message to be added or updated.
     */
    fun upsertMessage(message: Message) = upsertMessages(listOf(message))

    /**
     * Refreshes the [Message.member] snapshot carried by the thread replies the given member authored.
     *
     * Thread replies live in this state rather than the channel one, so the channel refresh does not reach them.
     *
     * @param cid The channel the membership belongs to.
     * @param userId The author whose replies should be refreshed.
     * @param memberInfo The member snapshot to store.
     */
    fun updateMessagesMemberInfo(cid: String, userId: String, memberInfo: MemberInfo) {
        val outdated = mutableState.rawMessage.value.values
            .filter { message -> message.cid == cid && message.hasOutdatedMemberInfo(userId, memberInfo) }
        if (outdated.isEmpty()) return
        mutableState.upsertMessages(outdated.map { message -> message.withRefreshedMemberInfo(userId, memberInfo) })
    }

    /**
     * Upsert messages in the channel.
     *
     * @param messages the list of [Message] to be upserted
     * new messages should be kept.
     */
    fun upsertMessages(messages: List<Message>) {
        val oldMessages = mutableState.rawMessage.value
        mutableState.upsertMessages(
            messages.filter { newMessage -> isMessageNewerThanCurrent(oldMessages[newMessage.id], newMessage) },
        )
    }

    /**
     * Updates each thread message that quotes the given message with its new content.
     *
     * @param quotedMessage The message whose quoting messages should be updated.
     */
    fun updateQuotedMessageReferences(quotedMessage: Message) {
        updateQuotingMessages(quotedMessage.id) { it.copy(replyTo = quotedMessage) }
    }

    /**
     * Clears the quoted message reference from each thread message quoting the given message.
     *
     * @param quotedMessageId The ID of the quoted message to remove references for.
     */
    fun deleteQuotedMessageReferences(quotedMessageId: String) {
        updateQuotingMessages(quotedMessageId) { it.copy(replyTo = null) }
    }

    private fun updateQuotingMessages(quotedMessageId: String, update: (Message) -> Message) {
        val quotingMessages = mutableState.rawMessage.value.values
            .filter { it.replyTo?.id == quotedMessageId || it.replyMessageId == quotedMessageId }
        if (quotingMessages.isNotEmpty()) {
            mutableState.upsertMessages(quotingMessages.map(update))
        }
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
