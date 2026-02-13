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

package io.getstream.chat.android.compose.ui.components.selectedmessage

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState
import io.getstream.chat.android.compose.ui.components.SimpleMenu
import io.getstream.chat.android.compose.ui.components.reactionoptions.ReactionOptions
import io.getstream.chat.android.compose.ui.components.userreactions.UserReactions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewReactionData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.MessageAction

/**
 * Represents the list of user reactions.
 *
 * @param message The selected message.
 * @param currentUser The currently logged in user.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 * @param onMessageAction Handler that propagates click events on each item.
 * @param onShowMoreReactionsSelected Handler that propagates clicks on the show more reactions button.
 * @param modifier Modifier for styling.
 * @param shape Changes the shape of [SelectedReactionsMenu].
 * @param overlayColor The color applied to the overlay.
 * @param showMoreReactionsIcon Drawable resource used for the show more button.
 * @param onDismiss Handler called when the menu is dismissed.
 * @param headerContent The content shown at the top of the [SelectedReactionsMenu] dialog.
 * By default [ReactionOptions].
 * @param centerContent The content shown in the [SelectedReactionsMenu] dialog. By Default [UserReactions].
 */
@Composable
public fun SelectedReactionsMenu(
    message: Message,
    currentUser: User?,
    ownCapabilities: Set<String>,
    onMessageAction: (MessageAction) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
    overlayColor: Color = ChatTheme.colors.overlay,
    @DrawableRes showMoreReactionsIcon: Int = R.drawable.stream_compose_ic_more,
    onDismiss: () -> Unit = {},
    headerContent: @Composable ColumnScope.() -> Unit = {
        val canLeaveReaction = ownCapabilities.contains(ChannelCapabilities.SEND_REACTION)

        if (canLeaveReaction) {
            with(ChatTheme.componentFactory) {
                ReactionsMenuHeaderContent(
                    modifier = Modifier,
                    message = message,
                    onMessageAction = onMessageAction,
                    onShowMoreReactionsSelected = onShowMoreReactionsSelected,
                    showMoreReactionsIcon = showMoreReactionsIcon,
                )
            }
        }
    },
    centerContent: @Composable ColumnScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ReactionsMenuCenterContent(
                modifier = Modifier,
                userReactions = buildUserReactionItems(
                    message = message,
                    currentUser = currentUser,
                ),
            )
        }
    },
) {
    SimpleMenu(
        modifier = modifier,
        shape = shape,
        overlayColor = overlayColor,
        onDismiss = onDismiss,
        headerContent = headerContent,
        centerContent = centerContent,
    )
}

/**
 * Builds a list of user reactions, based on the current user and the selected message.
 *
 * @param message The message the reactions were left for.
 * @param currentUser The currently logged in user.
 */
@Composable
private fun buildUserReactionItems(
    message: Message,
    currentUser: User?,
): List<UserReactionItemState> {
    val resolver = ChatTheme.reactionResolver
    return message.latestReactions
        .mapNotNull {
            val user = it.user ?: return@mapNotNull null
            val type = it.type

            UserReactionItemState(
                user = user,
                type = type,
                isMine = currentUser?.id == user.id,
                emojiCode = resolver.emojiCode(type),
            )
        }
}

/**
 * Preview of the [SelectedReactionsMenu] component with 1 reaction.
 */
@Preview
@Composable
private fun OneSelectedReactionMenuPreview() {
    ChatTheme {
        val message = Message(latestReactions = PreviewReactionData.oneReaction.toMutableList())

        SelectedReactionsMenu(
            message = message,
            currentUser = PreviewUserData.user1,
            onMessageAction = {},
            onShowMoreReactionsSelected = {},
            ownCapabilities = ChannelCapabilities.toSet(),
        )
    }
}

/**
 * Preview of the [SelectedReactionsMenu] component with many reactions.
 */
@Preview
@Composable
private fun ManySelectedReactionsMenuPreview() {
    ChatTheme {
        val message = Message(latestReactions = PreviewReactionData.manyReaction.toMutableList())

        SelectedReactionsMenu(
            message = message,
            currentUser = PreviewUserData.user1,
            onMessageAction = {},
            onShowMoreReactionsSelected = {},
            ownCapabilities = ChannelCapabilities.toSet(),
        )
    }
}
