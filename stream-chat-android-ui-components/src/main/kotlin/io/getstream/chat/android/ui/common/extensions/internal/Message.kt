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

package io.getstream.chat.android.ui.common.extensions.internal

import android.content.Context
import android.text.SpannableString
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.R

internal fun Message.getSenderDisplayName(context: Context, isDirectMessaging: Boolean = false): String? =
    when {
        user.isCurrentUser() -> context.getString(R.string.stream_ui_channel_list_you)
        isDirectMessaging -> null
        else -> user.asMention(context)
    }

internal fun Message.getPinnedText(context: Context): String? {
    val pinnedBy = pinnedBy ?: return null

    val user = if (pinnedBy.isCurrentUser()) {
        context.getString(R.string.stream_ui_message_list_pinned_message_you)
    } else {
        pinnedBy.name
    }
    return context.getString(R.string.stream_ui_message_list_pinned_message, user)
}

/**
 * Returns a string representation of message attachments or null if the attachment list is empty.
 */
internal fun Message.getAttachmentsText(): SpannableString? {
    return attachments.takeIf { it.isNotEmpty() }
        ?.mapNotNull { attachment ->
            attachment.title?.let { title ->
                val prefix = getAttachmentPrefix(attachment)
                if (prefix != null) {
                    "$prefix $title"
                } else {
                    title
                }
            } ?: attachment.name ?: attachment.fallback
        }
        ?.joinToString()
        ?.italicize()
}

private fun getAttachmentPrefix(attachment: Attachment): String? =
    when (attachment.type) {
        ModelType.attach_giphy -> "/giphy"
        else -> null
    }
