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

package io.getstream.chat.android.compose.ui.attachments.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.Delete
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewOption
import io.getstream.chat.android.compose.state.mediagallerypreview.Reply
import io.getstream.chat.android.compose.state.mediagallerypreview.SaveMedia
import io.getstream.chat.android.compose.state.mediagallerypreview.ShowInChat
import io.getstream.chat.android.compose.ui.components.StreamHorizontalDivider
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Composable rendering the options menu overlay for media gallery preview.
 *
 * Displays a dropdown menu in the top-right corner with available actions for the
 * currently displayed attachment. The menu appears as a floating surface with a
 * semi-transparent overlay covering the entire screen behind it. Clicking anywhere
 * outside the menu dismisses it.
 *
 * Each option is rendered as a [MediaGalleryOptionItem] with dividers between items.
 *
 * @param attachment The currently displayed attachment for which options are shown.
 * @param options List of available options to display in the menu.
 * @param onOptionClick Callback invoked when an option is clicked, providing both the attachment and option.
 * @param onDismiss Callback invoked when the menu should be dismissed.
 * @param modifier Optional modifier applied to the Surface containing the options.
 */
@Composable
internal fun MediaGalleryOptionsMenu(
    attachment: Attachment,
    options: List<MediaGalleryPreviewOption>,
    onOptionClick: (Attachment, MediaGalleryPreviewOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.overlayBackground)
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = onDismiss,
            ),
    ) {
        Surface(
            modifier = modifier
                .padding(16.dp)
                .width(150.dp)
                .wrapContentHeight()
                .align(Alignment.TopEnd),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp,
            color = ChatTheme.colors.backgroundElevationElevation1,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, option ->
                    MediaGalleryOptionItem(
                        option = option,
                        onClick = {
                            onDismiss()
                            onOptionClick(attachment, option)
                        },
                    )

                    if (index != options.lastIndex) {
                        StreamHorizontalDivider()
                    }
                }
            }
        }
    }
}

/**
 * Composable rendering a single option item in the media gallery options menu.
 *
 * Displays a horizontal row containing an icon and text label representing
 * an action that can be performed on the current media item. The appearance
 * of the item adapts based on whether the option is enabled or disabled.
 *
 * The entire row is clickable and shows a ripple effect when pressed.
 *
 * @param option The configuration for this option item, including title, icons, and enabled state.
 * @param onClick Callback invoked when the option is clicked.
 */
@Composable
internal fun MediaGalleryOptionItem(
    option: MediaGalleryPreviewOption,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.backgroundElevationElevation1)
            .clickable(
                interactionSource = null,
                indication = ripple(),
                enabled = option.isEnabled,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = option.iconPainter,
            tint = option.iconColor,
            contentDescription = option.title,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = option.title,
            color = option.titleColor,
            style = ChatTheme.typography.bodyBold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Creates the default list of media options for the media gallery preview screen.
 *
 * Constructs a list of available actions based on the provided configuration and current state.
 * Options may include:
 * - Reply to message
 * - Show in chat
 * - Save media
 * - Delete (only visible for the message owner)
 *
 * Each option has its appearance and enabled state determined by:
 * - The configuration settings
 * - The current connection state (options may be disabled when offline)
 * - The current user's permissions (delete is only available for own messages)
 *
 * @param currentUser The currently logged in user.
 * @param message The message containing the attachments.
 * @param connectionState The current network connection state.
 * @param config Configuration controlling which options are visible.
 * @return List of [MediaGalleryPreviewOption] items to display in the options menu.
 */
@Composable
internal fun defaultMediaOptions(
    currentUser: User?,
    message: Message,
    connectionState: ConnectionState,
    config: MediaGalleryOptionsConfig,
): List<MediaGalleryPreviewOption> {
    val isConnected = connectionState is ConnectionState.Connected
    val options = mutableListOf<MediaGalleryPreviewOption>()
    if (config.isReplyVisible) {
        val option = MediaGalleryPreviewOption(
            title = stringResource(id = R.string.stream_compose_media_gallery_preview_reply),
            titleColor = ChatTheme.colors.textPrimary,
            iconPainter = painterResource(id = R.drawable.stream_compose_ic_reply),
            iconColor = ChatTheme.colors.textPrimary,
            action = Reply(message),
            isEnabled = true,
        )
        options.add(option)
    }
    if (config.isShowInChatVisible) {
        val option = MediaGalleryPreviewOption(
            title = stringResource(id = R.string.stream_compose_media_gallery_preview_show_in_chat),
            titleColor = ChatTheme.colors.textPrimary,
            iconPainter = painterResource(id = R.drawable.stream_compose_ic_show_in_chat),
            iconColor = ChatTheme.colors.textPrimary,
            action = ShowInChat(message),
            isEnabled = true,
        )
        options.add(option)
    }
    if (config.isSaveMediaVisible) {
        val color = if (isConnected) ChatTheme.colors.textPrimary else ChatTheme.colors.textDisabled
        val option = MediaGalleryPreviewOption(
            title = stringResource(id = R.string.stream_compose_media_gallery_preview_save_image),
            titleColor = color,
            iconPainter = painterResource(id = R.drawable.stream_compose_ic_download),
            iconColor = color,
            action = SaveMedia(message),
            isEnabled = isConnected,
        )
        options.add(option)
    }
    if (config.isDeleteVisible && message.user.id == currentUser?.id) {
        val color = if (isConnected) ChatTheme.colors.accentError else ChatTheme.colors.textDisabled
        val option = MediaGalleryPreviewOption(
            title = stringResource(id = R.string.stream_compose_media_gallery_preview_delete),
            titleColor = color,
            iconPainter = painterResource(id = R.drawable.stream_compose_ic_delete),
            iconColor = color,
            action = Delete(message),
            isEnabled = isConnected,
        )
        options.add(option)
    }
    return options
}
