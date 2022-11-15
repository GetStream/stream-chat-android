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

@file:JvmName("MessageUtils")

package io.getstream.chat.android.client.utils.message

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.MessageType
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.utils.date.after
import io.getstream.chat.android.models.AttachmentType

private const val ITEM_COUNT_OF_TWO: Int = 2

/**
 * Peeks the latest message from the sorted [List] of messages.
 */
@InternalStreamChatApi
public fun List<Message>.latestOrNull(): Message? = when (size >= ITEM_COUNT_OF_TWO) {
    true -> {
        val first = first()
        val last = last()
        when (last.createdAfter(first)) {
            true -> last
            else -> first
        }
    }
    else -> lastOrNull()
}

/**
 * Tests if [this] message was created after [that] message.
 */
@InternalStreamChatApi
public fun Message.createdAfter(that: Message): Boolean {
    val thisDate = this.createdAt ?: this.createdLocallyAt
    val thatDate = that.createdAt ?: that.createdLocallyAt
    return thisDate after thatDate
}

/**
 * @return If the message is deleted.
 */
public fun Message.isDeleted(): Boolean = deletedAt != null

/**
 * @return If the message type is regular.
 */
public fun Message.isRegular(): Boolean = type == MessageType.REGULAR

/**
 * @return If the message type is ephemeral.
 */
public fun Message.isEphemeral(): Boolean = type == MessageType.EPHEMERAL

/**
 * @return If the message type is system.
 */
public fun Message.isSystem(): Boolean = type == MessageType.SYSTEM

/**
 * @return If the message type is error.
 */
public fun Message.isError(): Boolean = type == MessageType.ERROR

/**
 * @return If the message is related to a Giphy slash command.
 */
public fun Message.isGiphy(): Boolean = command == AttachmentType.GIPHY

/**
 * @return If the message is a temporary message to select a gif.
 */
public fun Message.isGiphyEphemeral(): Boolean = isGiphy() && isEphemeral()

/**
 * @return If the message is a start of a thread.
 */
public fun Message.isThreadStart(): Boolean = threadParticipants.isNotEmpty()

/**
 * @return If the message is a thread reply.
 */
public fun Message.isThreadReply(): Boolean = !parentId.isNullOrEmpty()

/**
 * @return If the message contains quoted message.
 */
public fun Message.isReply(): Boolean = replyTo != null
