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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.compose.ui.components.messages

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.components.attachments.files.FileIconData
import io.getstream.chat.android.compose.ui.components.attachments.files.FileTypeIcon
import io.getstream.chat.android.compose.ui.components.common.PlayButton
import io.getstream.chat.android.compose.ui.components.common.PlayButtonSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.utils.extensions.isMine

/**
 * Wraps the quoted message into a component that shows only the sender avatar, text and single attachment preview.
 *
 * @param message The quoted message to show.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 * @param replyMessage The message that contains the reply.
 */
@Composable
public fun QuotedMessage(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    replyMessage: Message? = null,
) {
    val style = MessageStyling.quotedMessageStyle(message, replyMessage, currentUser)

    Row(
        modifier = modifier
            .background(style.backgroundColor, RoundedCornerShape(StreamTokens.radiusLg))
            .padding(StreamTokens.spacingXs)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 2.dp),
            thickness = 2.dp,
            color = style.indicatorColor,
        )

        val bodyBuilder = rememberBodyBuilder()
        val body = remember(bodyBuilder, message, currentUser) { bodyBuilder.build(message, currentUser) }

        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .defaultMinSize(minHeight = 40.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            QuotedMessageUserName(message, replyMessage, currentUser, style.textColor)
            QuotedMessageText(body, style.textColor)
        }

        QuotedMessageAttachmentPreview(body)
    }
}

@Composable
internal fun MessageComposerQuotedMessage(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit,
) {
    Box(modifier) {
        QuotedMessage(
            message = message,
            currentUser = currentUser,
            replyMessage = null,
            modifier = Modifier.semantics(mergeDescendants = true) {},
        )

        ComposerCancelIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(StreamTokens.spacing2xs, -StreamTokens.spacing2xs),
            contentDescription = stringResource(R.string.stream_compose_message_composer_cancel_reply),
            onClick = onCancelClick,
        )
    }
}

@Composable
private fun QuotedMessageUserName(
    message: Message,
    replyMessage: Message?,
    currentUser: User?,
    color: Color,
) {
    val isMine = message.isMine(currentUser)
    val isComposerBanner = replyMessage == null
    val userName = when {
        isMine -> stringResource(R.string.stream_compose_quoted_message_you)
        isComposerBanner -> stringResource(R.string.stream_compose_quoted_message_reply_to, message.user.name)
        else -> message.user.name
    }
    val accessibilityName = when {
        replyMessage == null && isMine ->
            stringResource(R.string.stream_compose_quoted_message_reply_to_you)
        replyMessage != null -> {
            val replierName = if (replyMessage.isMine(currentUser)) {
                stringResource(R.string.stream_compose_quoted_message_you)
            } else {
                replyMessage.user.name
            }
            if (isMine) {
                stringResource(R.string.stream_compose_quoted_message_replied_to_your_message, replierName)
            } else {
                stringResource(
                    R.string.stream_compose_quoted_message_replied_to_their_message,
                    replierName,
                    message.user.name,
                )
            }
        }
        else -> null
    }

    Text(
        modifier = if (accessibilityName != null) {
            Modifier.semantics { contentDescription = accessibilityName }
        } else {
            Modifier
        },
        text = userName,
        style = ChatTheme.typography.metadataEmphasis,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun QuotedMessageText(body: QuotedMessageBody, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        body.iconId?.let { iconId ->
            Icon(
                painter = painterResource(iconId),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp),
            )
        }

        val spokenText = body.spokenText
        Text(
            modifier = Modifier
                .weight(1f)
                .testTag("Stream_QuotedMessage")
                .semantics { if (spokenText != null) contentDescription = spokenText },
            text = body.text,
            style = ChatTheme.typography.metadataDefault,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val mediaPreviewModifier = Modifier
    .size(40.dp)
    .clip(RoundedCornerShape(StreamTokens.radiusMd))

@Composable
private fun QuotedMessageAttachmentPreview(body: QuotedMessageBody) {
    when {
        body.imagePreviewData != null -> {
            StreamAsyncImage(
                modifier = mediaPreviewModifier,
                data = body.imagePreviewData,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }

        body.videoPreviewData != null -> {
            Box(contentAlignment = Alignment.Center) {
                StreamAsyncImage(
                    modifier = mediaPreviewModifier,
                    data = body.videoPreviewData,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                PlayButton(PlayButtonSize.Small)
            }
        }

        body.previewIcon != null -> {
            FileTypeIcon(
                modifier = Modifier.height(40.dp),
                data = body.previewIcon,
            )
        }
    }
}

internal data class QuotedMessageBody(
    val text: String,
    val spokenText: String? = null,
    @param:DrawableRes
    val iconId: Int? = null,
    val imagePreviewData: Any? = null,
    val videoPreviewData: Any? = null,
    val previewIcon: FileIconData? = null,
)

@Preview
@Composable
private fun QuotedMessageFromOtherUserPreview() {
    ChatTheme { QuotedMessageFromOtherUser() }
}

@Preview
@Composable
private fun QuotedMessageFromSelfPreview() {
    ChatTheme { QuotedMessageFromSelf() }
}

@Preview
@Composable
private fun QuotedMessageWithLongTextPreview() {
    ChatTheme { QuotedMessageWithLongText() }
}

@Preview
@Composable
private fun QuotedMessageWithImageAttachmentPreview() {
    ChatTheme { QuotedMessageWithImageAttachment() }
}

@Preview
@Composable
private fun QuotedMessageWithFileAttachmentPreview() {
    ChatTheme { QuotedMessageWithFileAttachment() }
}

@Preview
@Composable
private fun QuotedMessageInComposerPreview() {
    ChatTheme { QuotedMessageInComposer() }
}

@Preview
@Composable
private fun QuotedMessageReplyByMeToOtherPreview() {
    ChatTheme { QuotedMessageReplyByMeToOther() }
}

@Preview
@Composable
private fun QuotedMessageReplyByOtherToMePreview() {
    ChatTheme { QuotedMessageReplyByOtherToMe() }
}

@Composable
internal fun QuotedMessageFromOtherUser() {
    QuotedMessage(
        message = Message(
            id = "msg-1",
            text = "Hey, did you see the new design?",
            user = PreviewUserData.user2,
        ),
        currentUser = PreviewUserData.user1,
    )
}

@Composable
internal fun QuotedMessageFromSelf() {
    QuotedMessage(
        message = Message(
            id = "msg-2",
            text = "Yes, looks great!",
            user = PreviewUserData.user1,
        ),
        currentUser = PreviewUserData.user1,
    )
}

@Composable
internal fun QuotedMessageWithLongText() {
    QuotedMessage(
        message = Message(
            id = "msg-3",
            text = "This is a very long quoted message that should overflow with ellipsis " +
                "because it does not fit on a single line in the quoted preview",
            user = PreviewUserData.user2,
        ),
        currentUser = PreviewUserData.user1,
    )
}

@Composable
internal fun QuotedMessageWithImageAttachment() {
    QuotedMessage(
        message = Message(
            id = "msg-4",
            text = "Check this out",
            user = PreviewUserData.user2,
            attachments = mutableListOf(
                Attachment(
                    type = AttachmentType.IMAGE,
                    imageUrl = "https://example.com/image.jpg",
                ),
            ),
        ),
        currentUser = PreviewUserData.user1,
    )
}

@Composable
internal fun QuotedMessageWithFileAttachment() {
    QuotedMessage(
        message = Message(
            id = "msg-5",
            text = "",
            user = PreviewUserData.user2,
            attachments = mutableListOf(
                Attachment(
                    type = AttachmentType.FILE,
                    title = "Q1-report.pdf",
                    mimeType = "application/pdf",
                    fileSize = 1024 * 256,
                ),
            ),
        ),
        currentUser = PreviewUserData.user1,
    )
}

@Composable
internal fun QuotedMessageInComposer() {
    MessageComposerQuotedMessage(
        message = Message(
            id = "msg-6",
            text = "Reply target",
            user = PreviewUserData.user2,
        ),
        currentUser = PreviewUserData.user1,
        onCancelClick = {},
    )
}

@Composable
internal fun QuotedMessageReplyByMeToOther() {
    QuotedMessage(
        message = Message(
            id = "msg-7",
            text = "Original message from the other user",
            user = PreviewUserData.user2,
        ),
        currentUser = PreviewUserData.user1,
        replyMessage = Message(
            id = "reply-7",
            text = "On it.",
            user = PreviewUserData.user1,
        ),
    )
}

@Composable
internal fun QuotedMessageReplyByOtherToMe() {
    QuotedMessage(
        message = Message(
            id = "msg-8",
            text = "Original message from me",
            user = PreviewUserData.user1,
        ),
        currentUser = PreviewUserData.user1,
        replyMessage = Message(
            id = "reply-8",
            text = "Got it, thanks!",
            user = PreviewUserData.user2,
        ),
    )
}
