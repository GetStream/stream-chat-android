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

package io.getstream.chat.android.compose.ui.components.selectedmessage

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState
import io.getstream.chat.android.compose.ui.components.SimpleMenu
import io.getstream.chat.android.compose.ui.components.reactionoptions.ReactionOptions
import io.getstream.chat.android.compose.ui.components.userreactions.UserReactions
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ReactionIcon
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewReactionData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.React

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
 * @param reactionTypes The available reactions within the menu.
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
    reactionTypes: Map<String, ReactionIcon> = ChatTheme.reactionIconFactory.createReactionIcons(),
    @DrawableRes showMoreReactionsIcon: Int = R.drawable.stream_compose_ic_more,
    onDismiss: () -> Unit = {},
    headerContent: @Composable ColumnScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            SelectedReactionsMenuHeaderContent(
                message = message,
                ownCapabilities = ownCapabilities,
                reactionTypes = reactionTypes,
                showMoreReactionsDrawable = showMoreReactionsIcon,
                onMessageAction = onMessageAction,
                onShowMoreReactionsClick = onShowMoreReactionsSelected,
            )
        }
    },
    centerContent: @Composable ColumnScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            SelectedReactionsMenuCenterContent(
                message = message,
                currentUser = currentUser,
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
 * Default header content for the selected reactions menu.
 */
@Composable
internal fun DefaultSelectedReactionsHeaderContent(
    message: Message,
    ownCapabilities: Set<String>,
    reactionTypes: Map<String, ReactionIcon>,
    @DrawableRes showMoreReactionsIcon: Int = R.drawable.stream_compose_ic_more,
    onMessageAction: (MessageAction) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
) {
    val canLeaveReaction = ownCapabilities.contains(ChannelCapabilities.SEND_REACTION)
    if (canLeaveReaction) {
        ReactionOptions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 20.dp),
            reactionTypes = reactionTypes,
            showMoreReactionsIcon = showMoreReactionsIcon,
            onReactionOptionSelected = {
                onMessageAction(
                    React(
                        reaction = Reaction(messageId = message.id, type = it.type),
                        message = message,
                    ),
                )
            },
            onShowMoreReactionsSelected = onShowMoreReactionsSelected,
            ownReactions = message.ownReactions,
        )
    }
}

/**
 * Default center content for the selected reactions menu.
 *
 * @param message The selected message.
 * @param currentUser The currently logged in user.
 */
@Composable
internal fun DefaultSelectedReactionsCenterContent(
    message: Message,
    currentUser: User?,
) {
    UserReactions(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = ChatTheme.dimens.userReactionsMaxHeight)
            .padding(vertical = 16.dp),
        items = buildUserReactionItems(
            message = message,
            currentUser = currentUser,
        ),
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
    val iconFactory = ChatTheme.reactionIconFactory
    return message.latestReactions
        .filter { it.user != null && iconFactory.isReactionSupported(it.type) }
        .map {
            val user = requireNotNull(it.user)
            val type = it.type
            val isMine = currentUser?.id == user.id
            val painter = iconFactory.createReactionIcon(type).getPainter(isMine)

            UserReactionItemState(
                user = user,
                painter = painter,
                type = type,
            )
        }
}

/**
 * Preview of the [SelectedReactionsMenu] component with 1 reaction.
 */
@Preview
@Composable
private fun OneSelectedReactionMenuPreview() {
    ChatPreviewTheme {
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
    ChatPreviewTheme {
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
