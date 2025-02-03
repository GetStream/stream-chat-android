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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.components.SimpleMenu
import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptions
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.components.reactionoptions.ReactionOptions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ReactionIcon
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.React

/**
 * Represents the options user can take after selecting a message.
 *
 * @param message The selected message.
 * @param messageOptions The available message options within the menu.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 * @param onMessageAction Handler that propagates click events on each item.
 * @param onShowMoreReactionsSelected Handler that propagates clicks on the show more reactions button.
 * @param modifier Modifier for styling.
 * @param shape Changes the shape of [SelectedMessageMenu].
 * @param overlayColor The color applied to the overlay.
 * @param reactionTypes The available reactions within the menu.
 * @param showMoreReactionsIcon Drawable resource used for the show more button.
 * @param onDismiss Handler called when the menu is dismissed.
 * @param headerContent The content shown at the top of the [SelectedMessageMenu] dialog. By default [ReactionOptions].
 * @param centerContent The content shown at the center of the [SelectedMessageMenu] dialog.
 * By Default [MessageOptions].
 */
@Composable
public fun SelectedMessageMenu(
    message: Message,
    messageOptions: List<MessageOptionItemState>,
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
            SelectedMessageMenuHeaderContent(
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
            SelectedMessageMenuCenterContent(
                messageOptions = messageOptions,
                onMessageAction = onMessageAction,
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
 * Default header content of the selected message menu.
 */
@Composable
internal fun DefaultSelectedMessageMenuHeaderContent(
    message: Message,
    ownCapabilities: Set<String>,
    reactionTypes: Map<String, ReactionIcon>,
    @DrawableRes showMoreReactionsDrawableRes: Int = R.drawable.stream_compose_ic_more,
    onMessageAction: (MessageAction) -> Unit,
    showMoreReactionsIcon: () -> Unit,
) {
    val canLeaveReaction = ownCapabilities.contains(ChannelCapabilities.SEND_REACTION)
    if (ChatTheme.reactionOptionsTheme.areReactionOptionsVisible && canLeaveReaction) {
        ReactionOptions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 20.dp),
            reactionTypes = reactionTypes,
            showMoreReactionsIcon = showMoreReactionsDrawableRes,
            onReactionOptionSelected = {
                onMessageAction(
                    React(
                        reaction = Reaction(messageId = message.id, type = it.type),
                        message = message,
                    ),
                )
            },
            onShowMoreReactionsSelected = showMoreReactionsIcon,
            ownReactions = message.ownReactions,
        )
    }
}

/**
 * Default selected message options.
 *
 * @param messageOptions The available options.
 * @param onMessageAction Handler when the user selects an option.
 */
@Composable
internal fun DefaultSelectedMessageOptions(
    messageOptions: List<MessageOptionItemState>,
    onMessageAction: (MessageAction) -> Unit,
) {
    MessageOptions(
        options = messageOptions,
        onMessageOptionSelected = {
            onMessageAction(it.action)
        },
    )
}

/**
 * Preview of [SelectedMessageMenu].
 */
@Preview(showBackground = true, name = "SelectedMessageMenu Preview")
@Composable
private fun SelectedMessageMenuPreview() {
    ChatTheme {
        val messageOptionsStateList = defaultMessageOptionsState(
            selectedMessage = PreviewMessageData.message1,
            currentUser = PreviewUserData.user1,
            isInThread = false,
            ownCapabilities = ChannelCapabilities.toSet(),
        )

        SelectedMessageMenu(
            message = Message(),
            messageOptions = messageOptionsStateList,
            onMessageAction = {},
            onShowMoreReactionsSelected = {},
            ownCapabilities = ChannelCapabilities.toSet(),
        )
    }
}
