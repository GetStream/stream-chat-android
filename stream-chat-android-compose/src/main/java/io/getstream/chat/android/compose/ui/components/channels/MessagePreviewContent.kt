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

package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.getSenderDisplayName
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Displays a message preview with the sender name and message content as separate composables
 * in a [Row].
 */
@Composable
internal fun MessagePreviewContent(
    message: Message,
    currentUser: User?,
    isDirectMessaging: Boolean,
    modifier: Modifier = Modifier,
    senderTextStyle: TextStyle = ChatTheme.typography.captionEmphasis,
    senderColor: Color = ChatTheme.colors.textTertiary,
    contentTextStyle: TextStyle = ChatTheme.typography.captionDefault,
    contentColor: Color = ChatTheme.colors.textSecondary,
) {
    val context = LocalContext.current
    val senderName = message.getSenderDisplayName(context, currentUser, isDirectMessaging)
    val messageText = ChatTheme.messagePreviewFormatter.formatMessagePreview(
        message,
        currentUser,
        isDirectMessaging,
    )
    val fullPreview = if (senderName != null) "$senderName: $messageText" else messageText.text
    Row(
        modifier = modifier
            .testTag("Stream_MessagePreview")
            .clearAndSetSemantics { text = AnnotatedString(fullPreview) },
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing3xs),
    ) {
        if (senderName != null) {
            Text(
                text = "$senderName:",
                style = senderTextStyle,
                color = senderColor,
                maxLines = 1,
            )
        }
        Text(
            text = messageText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = contentTextStyle,
            color = contentColor,
            inlineContent = ChatTheme.messagePreviewIconFactory.createPreviewIcons(),
        )
    }
}

/**
 * Displays a draft message preview with the "Draft:" label and draft text as separate composables
 * in a [Row].
 */
@Composable
internal fun DraftPreviewContent(
    draftMessage: DraftMessage,
    modifier: Modifier = Modifier,
) {
    val draftLabel = stringResource(R.string.stream_compose_channel_list_draft)
    val draftBody = draftMessage.previewBody()
    val fullPreview = "$draftLabel $draftBody"
    Row(
        modifier = modifier
            .testTag("Stream_MessagePreview")
            .clearAndSetSemantics { text = AnnotatedString(fullPreview) },
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing3xs),
    ) {
        Text(
            text = draftLabel,
            style = ChatTheme.typography.captionEmphasis,
            color = ChatTheme.colors.accentPrimary,
            maxLines = 1,
        )
        Text(
            text = draftBody,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ChatTheme.typography.captionDefault,
            color = ChatTheme.colors.textSecondary,
        )
    }
}

/**
 * Renders the body of the draft preview. Active commands are surfaced with their `/name` prefix
 * so the channel list reflects what the user actually composed (matches legacy mode, where the
 * prefix was carried inside `text`).
 */
private fun DraftMessage.previewBody(): String {
    val name = command ?: return text
    val typed = args ?: text
    return if (typed.isEmpty()) "/$name" else "/$name $typed"
}
