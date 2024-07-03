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

@file:Suppress("TooManyFunctions")
@file:JvmName("MessageUtils")

package io.getstream.chat.android.client.utils.message

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.utils.date.after
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageModerationAction
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import java.util.UUID

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
 * @return If the current message failed to send.
 */
@InternalStreamChatApi
public fun Message.isFailed(): Boolean = this.syncStatus == SyncStatus.FAILED_PERMANENTLY

/**
 * @return If the message type is error or failed to send.
 */
@InternalStreamChatApi
public fun Message.isErrorOrFailed(): Boolean = isError() || isFailed()

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
 * @return If the message is related to the poll.
 */
public fun Message.isPoll(): Boolean = poll != null

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

/**
 * @return If the message belongs to the current user.
 */
@InternalStreamChatApi
public fun Message.isMine(currentUserId: String?): Boolean = currentUserId == user.id

/**
 * @return If the message has moderation bounce action.
 */
@InternalStreamChatApi
public fun Message.isModerationBounce(): Boolean = moderationDetails?.action == MessageModerationAction.bounce

/**
 * @return If the message has moderation block action.
 */
@InternalStreamChatApi
public fun Message.isModerationBlock(): Boolean = moderationDetails?.action == MessageModerationAction.block

/**
 * @return If the message has moderation flag action.
 */
@InternalStreamChatApi
public fun Message.isModerationFlag(): Boolean = moderationDetails?.action == MessageModerationAction.flag

/**
 * @return if the message failed at moderation or not.
 */
public fun Message.isModerationError(currentUserId: String?): Boolean = isMine(currentUserId) &&
    (isError() && isModerationBounce())

/**
 * Ensures the message has an id.
 * If the message doesn't have an id, a unique message id is generated.
 * @return the message with an id.
 */
internal fun Message.ensureId(currentUser: User?): Message =
    copy(id = id.takeIf { it.isNotBlank() } ?: generateMessageId(currentUser))

/**
 * Returns a unique message id prefixed with user id.
 */
private fun generateMessageId(user: User?): String {
    return "${user?.id}-${UUID.randomUUID()}"
}
