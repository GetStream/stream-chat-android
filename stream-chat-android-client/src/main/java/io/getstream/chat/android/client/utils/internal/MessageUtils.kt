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
import java.util.regex.Pattern

private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")

// TODO: type should be a sealed/class or enum at the client level
@InternalStreamChatApi
public fun getMessageType(message: Message): String {
    val hasAttachments = message.attachments.isNotEmpty()
    val hasAttachmentsToUpload = message.hasPendingAttachments()

    return if (COMMAND_PATTERN.matcher(message.text).find() || (hasAttachments && hasAttachmentsToUpload)) {
        Message.TYPE_EPHEMERAL
    } else {
        Message.TYPE_REGULAR
    }
}
