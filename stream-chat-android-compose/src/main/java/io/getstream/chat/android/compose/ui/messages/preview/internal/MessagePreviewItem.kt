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

package io.getstream.chat.android.compose.ui.messages.preview.internal

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.shouldShowOnlineIndicator

/**
 * Composable which represents a preview of a given message. It displays info about the channel in which it was typed,
 * and a short preview of the message text.
 *
 * @param message The [Message] to show a preview of.
 * @param currentUser The currently logged in [User], used for message formatting.
 * @param onMessagePreviewClick Action to be executed when the item is clicked on.
 * @param modifier The [Modifier] for external styling.
 * @param leadingContent Customizable composable function that represents the leading content of a message preview,
 * usually the avatar that holds an image of the user that sent the message.
 * @param centerContent Customizable composable function that represents the center content of a message preview,
 * usually holding information about the message and who and where it was sent.
 * @param trailingContent Customizable composable function that represents the trailing content of a message preview,
 * usually information about the date where the message was sent.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MessagePreviewItem(
    message: Message,
    currentUser: User?,
    onMessagePreviewClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.(Message) -> Unit = {
        DefaultMessagePreviewItemLeadingContent(it, currentUser)
    },
    centerContent: @Composable RowScope.(Message) -> Unit = {
        DefaultMessagePreviewItemCenterContent(it, currentUser)
    },
    trailingContent: @Composable RowScope.(Message) -> Unit = {
        DefaultMessagePreviewItemTrailingContent(it)
    },
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = { onMessagePreviewClick(message) },
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent(message)
            centerContent(message)
            trailingContent(message)
        }
    }
}

/**
 * Default leading content for a message preview. Shows an avatar for the user which sent the message.
 *
 * @param message The [Message] for which the leading content is shown.
 * @param currentUser The currently logged in user.
 */
@Composable
internal fun DefaultMessagePreviewItemLeadingContent(message: Message, currentUser: User?) {
    ChatTheme.componentFactory.UserAvatar(
        user = message.user,
        modifier = Modifier
            .padding(
                start = ChatTheme.dimens.channelItemHorizontalPadding,
                end = 4.dp,
                top = ChatTheme.dimens.channelItemVerticalPadding,
                bottom = ChatTheme.dimens.channelItemVerticalPadding,
            )
            .size(ChatTheme.dimens.channelAvatarSize),
        showIndicator = message.user.shouldShowOnlineIndicator(
            userPresence = ChatTheme.userPresence,
            currentUser = currentUser,
        ),
        showBorder = false,
    )
}

/**
 * Default center content for a message preview. Shows info about the channel in which the message was sent, and a short
 * preview of the message text.
 *
 * @param message The [Message] for which the center content is shown.
 * @param currentUser The currently logged in [User], used for message formatting.
 */
@Composable
internal fun RowScope.DefaultMessagePreviewItemCenterContent(
    message: Message,
    currentUser: User?,
) {
    Column(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp)
            .weight(1f)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = ChatTheme.messagePreviewFormatter.formatMessageTitle(message),
            style = ChatTheme.typography.bodyBold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textPrimary,
        )

        Text(
            text = ChatTheme.messagePreviewFormatter.formatMessagePreview(message, currentUser),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ChatTheme.typography.body,
            color = ChatTheme.colors.textSecondary,
        )
    }
}

/**
 * Default leading trailing for a message preview. Shows the timestamp of the message.
 *
 * @param message The [Message] for which the trailing content is shown.
 */
@Composable
internal fun RowScope.DefaultMessagePreviewItemTrailingContent(message: Message) {
    Column(
        modifier = Modifier
            .padding(
                start = 4.dp,
                end = ChatTheme.dimens.channelItemHorizontalPadding,
                top = ChatTheme.dimens.channelItemVerticalPadding,
                bottom = ChatTheme.dimens.channelItemVerticalPadding,
            )
            .wrapContentHeight()
            .align(Alignment.Bottom),
        horizontalAlignment = Alignment.End,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Timestamp(date = message.createdAt)
        }
    }
}
