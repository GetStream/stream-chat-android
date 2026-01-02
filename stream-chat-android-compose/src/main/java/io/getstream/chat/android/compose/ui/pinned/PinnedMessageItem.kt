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

package io.getstream.chat.android.compose.ui.pinned

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.messages.preview.internal.MessagePreviewItem
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * The basic pinned message item that displays the channel and the message in a list, and exposes click action.
 *
 * @param message The [Message] to be displayed.
 * @param currentUser The currently logged in [User].
 * @param onPinnedMessageClick The action to be executed when the item is clicked.
 * @param leadingContent Customizable composable function that represents the leading content of a pinned message,
 * usually the avatar that holds an image of the user that sent the message.
 * @param centerContent Customizable composable function that represents the center content of a pinned message,
 * usually holding information about the message and who and where it was sent.
 * @param trailingContent Customizable composable function that represents the trailing content of a pinned message,
 * usually information about the date where the message was sent.
 */
@Composable
public fun PinnedMessageItem(
    message: Message,
    currentUser: User?,
    onPinnedMessageClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.(Message) -> Unit = {
        with(ChatTheme.componentFactory) {
            PinnedMessageListItemLeadingContent(it, currentUser)
        }
    },
    centerContent: @Composable RowScope.(Message) -> Unit = {
        with(ChatTheme.componentFactory) {
            PinnedMessageListItemCenterContent(it, currentUser)
        }
    },
    trailingContent: @Composable RowScope.(Message) -> Unit = {
        with(ChatTheme.componentFactory) {
            PinnedMessageListItemTrailingContent(it)
        }
    },
) {
    MessagePreviewItem(
        message = message,
        currentUser = currentUser,
        onMessagePreviewClick = onPinnedMessageClick,
        modifier = modifier,
        leadingContent = leadingContent,
        centerContent = centerContent,
        trailingContent = trailingContent,
    )
}
