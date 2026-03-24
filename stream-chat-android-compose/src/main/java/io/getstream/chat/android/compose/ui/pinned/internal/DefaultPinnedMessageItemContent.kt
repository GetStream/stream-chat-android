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

package io.getstream.chat.android.compose.ui.pinned.internal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.theme.UserAvatarParams
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Composable which represents a pinned message item. It displays the sender, a short preview of the message text,
 * and the timestamp.
 *
 * @param message The [Message] to show.
 * @param currentUser The currently logged in [User], used for message formatting.
 * @param onPinnedMessageClick Action to be executed when the item is clicked on.
 * @param modifier The [Modifier] for external styling.
 * @param leadingContent Customizable composable function that represents the leading content of a pinned message item,
 * usually the avatar that holds an image of the user that sent the message.
 * @param centerContent Customizable composable function that represents the center content of a pinned message item,
 * usually holding information about the message and who and where it was sent.
 * @param trailingContent Customizable composable function that represents the trailing content of a pinned message
 * item, usually information about the date where the message was sent.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PinnedMessageItemContent(
    message: Message,
    currentUser: User?,
    onPinnedMessageClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.(Message) -> Unit = {
        DefaultPinnedMessageItemLeadingContent(it)
    },
    centerContent: @Composable RowScope.(Message) -> Unit = {
        DefaultPinnedMessageItemCenterContent(it, currentUser)
    },
    trailingContent: @Composable RowScope.(Message) -> Unit = {
        DefaultPinnedMessageItemTrailingContent(it)
    },
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = StreamTokens.spacing2xs)
            .combinedClickable(
                onClick = { onPinnedMessageClick(message) },
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(StreamTokens.spacingSm),
            horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
            verticalAlignment = Alignment.Top,
        ) {
            leadingContent(message)
            centerContent(message)
            trailingContent(message)
        }
    }
}

/**
 * Default leading content for a pinned message item. Shows an avatar for the user which sent the message.
 *
 * @param message The [Message] for which the leading content is shown.
 */
@Composable
internal fun DefaultPinnedMessageItemLeadingContent(message: Message) {
    ChatTheme.componentFactory.UserAvatar(
        params = UserAvatarParams(
            user = message.user,
            modifier = Modifier.size(AvatarSize.Large),
            showIndicator = false,
            showBorder = false,
        ),
    )
}

/**
 * Default center content for a pinned message item. Shows the sender name as a title and a short
 * preview of the message text.
 *
 * @param message The [Message] for which the center content is shown.
 * @param currentUser The currently logged in [User], used for message formatting.
 */
@Composable
internal fun RowScope.DefaultPinnedMessageItemCenterContent(
    message: Message,
    currentUser: User?,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
    ) {
        Text(
            text = ChatTheme.messagePreviewFormatter.formatMessageTitle(message, currentUser),
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = ChatTheme.messagePreviewFormatter.formatMessagePreview(
                message = message,
                currentUser = currentUser,
                isDirectMessaging = false,
                includeSenderName = false,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ChatTheme.typography.captionDefault,
            color = ChatTheme.colors.textTertiary,
            inlineContent = ChatTheme.messagePreviewIconFactory.createPreviewIcons(),
        )
    }
}

/**
 * Default trailing content for a pinned message item. Shows the timestamp of the message.
 *
 * @param message The [Message] for which the trailing content is shown.
 */
@Composable
internal fun DefaultPinnedMessageItemTrailingContent(message: Message) {
    Timestamp(date = message.createdAt)
}
