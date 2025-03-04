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

package io.getstream.chat.android.client.utils.internal

import io.getstream.chat.android.client.extensions.internal.hasPendingAttachments
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import java.util.regex.Pattern

private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")

/**
 * Updates the type of the [Message] based on its content.
 *
 * If the message contains a command or has attachments to upload, the type will be [MessageType.EPHEMERAL].
 * If the message is a system message, the type will be [MessageType.SYSTEM].
 * Otherwise, the type will be [MessageType.REGULAR], as we cannot send messages which are not regular, ephemeral, or
 * system.
 *
 * @param message The message to update.
 */
@InternalStreamChatApi
public fun getMessageType(message: Message): String {
    val hasAttachments = message.attachments.isNotEmpty()
    val hasAttachmentsToUpload = message.hasPendingAttachments()

    return if (COMMAND_PATTERN.matcher(message.text).find() || (hasAttachments && hasAttachmentsToUpload)) {
        MessageType.EPHEMERAL
    } else if (message.type == MessageType.SYSTEM) {
        MessageType.SYSTEM
    } else {
        MessageType.REGULAR
    }
}
