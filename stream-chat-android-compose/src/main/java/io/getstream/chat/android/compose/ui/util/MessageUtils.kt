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

package io.getstream.chat.android.compose.ui.util

import android.content.Context
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.uiutils.extension.isUploading
import io.getstream.chat.android.uiutils.util.EmojiUtil

/**
 * Takes the current message and returns the sender display name.
 *
 * @return Sender display name.
 */
internal fun Message.getSenderDisplayName(
    context: Context,
    currentUser: User?,
): String? =
    when (user.id) {
        currentUser?.id -> context.getString(R.string.stream_compose_channel_list_you)
        else -> null
    }

/**
 * @return If the message type is regular.
 */
internal fun Message.isRegular(): Boolean = type == ModelType.message_regular

/**
 * @return If the message type is ephemeral.
 */
internal fun Message.isEphemeral(): Boolean = type == ModelType.message_ephemeral

/**
 * @return If the message type is system.
 */
internal fun Message.isSystem(): Boolean = type == ModelType.message_system

/**
 * @return If the message type is error.
 */
internal fun Message.isError(): Boolean = type == ModelType.message_error

/**
 * @return If the message is deleted.
 */
internal fun Message.isDeleted(): Boolean = deletedAt != null

/**
 * @return If the message contains an attachment that is currently being uploaded.
 */
internal fun Message.isUploading(): Boolean = attachments.any { it.isUploading() }

/**
 * @return If the message is a start of a thread.
 */
internal fun Message.hasThread(): Boolean = threadParticipants.isNotEmpty()

/**
 * @return If the message is related to a Giphy slash command.
 */
internal fun Message.isGiphy(): Boolean = command == ModelType.attach_giphy

/**
 * @return If the message is a temporary message to select a gif.
 */
internal fun Message.isGiphyEphemeral(): Boolean = isGiphy() && isEphemeral()

/**
 * @return If the message is emoji only or not.
 */
internal fun Message.isEmojiOnly(): Boolean = EmojiUtil.isEmojiOnly(this)

/**
 * @return If the message is single emoji only or not.
 */
internal fun Message.isSingleEmoji(): Boolean = EmojiUtil.isSingleEmoji(this)

/**
 * @return The number of emoji inside the message.
 */
internal fun Message.getEmojiCount(): Int = EmojiUtil.getEmojiCount(this)

/**
 * @return If the message should has less or equal to [MaxFullSizeEmoji] emoji count.
 */
internal fun Message.isFewEmoji(): Boolean = isEmojiOnly() && getEmojiCount() <= MaxFullSizeEmoji

/**
 * @return If the message is emoji only and should be shown without a message bubble or not.
 */
internal fun Message.isEmojiOnlyWithoutBubble(): Boolean = isFewEmoji() &&
    replyTo == null

/**
 * Max number of emoji without showing it inside a bubble.
 */
internal const val MaxFullSizeEmoji: Int = 3
