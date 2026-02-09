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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.components.attachments.files.FileIconData
import io.getstream.chat.android.compose.ui.components.attachments.files.FileTypeIcon
import io.getstream.chat.android.compose.ui.components.common.PlayButton
import io.getstream.chat.android.compose.ui.components.common.PlayButtonSize
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.isMine

/**
 * Wraps the quoted message into a component that shows only the sender avatar, text and single attachment preview.
 *
 * @param message The quoted message to show.
 * @param currentUser The currently logged in user.
 * @param onLongItemClick Handler when the item is long clicked.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param modifier Modifier for styling.
 * @param replyMessage The message that contains the reply.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun QuotedMessage(
    message: Message,
    currentUser: User?,
    onLongItemClick: (Message) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    replyMessage: Message? = null,
) {
    val style = MessageStyling.quotedMessageStyle(message, replyMessage, currentUser)

    Row(
        modifier = modifier
            .combinedClickable(
                interactionSource = remember(::MutableInteractionSource),
                indication = null,
                onLongClick = { onLongItemClick(message) },
                onClick = { onQuotedMessageClick(message) },
            )
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
            onLongItemClick = {},
            onQuotedMessageClick = {},
            replyMessage = null,
        )

        ComposerCancelIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(StreamTokens.spacing2xs, -StreamTokens.spacing2xs),
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
    val userName = if (message.isMine(currentUser)) {
        stringResource(R.string.stream_compose_quoted_message_you)
    } else if (replyMessage == null) {
        stringResource(R.string.stream_compose_quoted_message_reply_to, message.user.name)
    } else {
        message.user.name
    }

    Text(
        text = userName,
        fontWeight = FontWeight.SemiBold,
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
            )
        }

        Text(
            modifier = Modifier.testTag("Stream_QuotedMessage"),
            text = body.text,
            color = color,
            fontWeight = FontWeight.Normal,
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
    @param:DrawableRes
    val iconId: Int? = null,
    val imagePreviewData: Any? = null,
    val videoPreviewData: Any? = null,
    val previewIcon: FileIconData? = null,
)
