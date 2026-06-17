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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState

/**
 * Prefixes [content] with the message sender so screen readers announce who sent the message.
 * Applied to a single content leaf per message, so the sender is announced once regardless of the
 * visual grouping that hides the author name on consecutive messages from the same user. When
 * [isReply] is set, the prefix uses "replied" instead of "said" so the message announces that it is
 * a reply.
 *
 * @param isMine Whether the message belongs to the current user.
 * @param senderName Display name of the sender, used for incoming messages.
 * @param content The content already announced for this leaf (message text, poll name, etc.).
 * @param isReply Whether the message is a reply to another message.
 */
@Composable
internal fun senderAwareContentDescription(
    isMine: Boolean,
    senderName: String,
    content: String,
    isReply: Boolean = false,
): String = when {
    isMine && isReply -> stringResource(R.string.stream_compose_message_sender_self_reply, content)
    isMine -> stringResource(R.string.stream_compose_message_sender_self, content)
    isReply -> stringResource(R.string.stream_compose_message_sender_other_reply, senderName, content)
    else -> stringResource(R.string.stream_compose_message_sender_other, senderName, content)
}

/**
 * Returns [label] prefixed with the sender when this attachment is the one designated to announce
 * the sender ([AttachmentState.announceSender]); otherwise returns [label] unchanged. Centralizes
 * the sender gate shared by the attachment content components.
 *
 * @param label The content already announced for this attachment (e.g. "Image attachment").
 */
@Composable
internal fun AttachmentState.senderAwareDescription(label: String): String =
    if (announceSender) {
        senderAwareContentDescription(isMine, message.user.name, label, isReply = message.replyTo != null)
    } else {
        label
    }
