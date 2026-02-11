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

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ReactionEmoji
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ReactionSortingByLastReactionAt
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState

/**
 * Represents the options user can take after selecting a message.
 *
 * The selected message is shown in a centered pop-out overlay with a dark background,
 * reactions above it and a flat options list below.
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
 * @param currentUser The currently logged-in user, used to build the message preview.
 * @param onDismiss Handler called when the menu is dismissed.
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
    reactionTypes: Map<String, String> = ReactionEmoji.defaultReactions,
    @DrawableRes showMoreReactionsIcon: Int = R.drawable.stream_compose_ic_more,
    currentUser: User? = null,
    onDismiss: () -> Unit = {},
) {
    val componentFactory = ChatTheme.componentFactory

    Column(
        modifier = modifier
            .background(overlayColor)
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = null,
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        val canLeaveReaction = ChannelCapabilities.SEND_REACTION in ownCapabilities
        if (canLeaveReaction && ChatTheme.reactionOptionsTheme.areReactionOptionsVisible) {
            componentFactory.MessageMenuHeaderContent(
                modifier = Modifier,
                message = message,
                messageOptions = messageOptions,
                onMessageAction = onMessageAction,
                ownCapabilities = ownCapabilities,
                onShowMore = onShowMoreReactionsSelected,
                reactionTypes = reactionTypes,
                showMoreReactionsIcon = showMoreReactionsIcon,
            )
        }

        val messageItemState = MessageItemState(
            message = message,
            isMine = message.user.id == currentUser?.id,
            currentUser = currentUser,
            ownCapabilities = ownCapabilities,
            showMessageFooter = true,
        )
        componentFactory.MessageContainer(
            modifier = Modifier.disablePointerEvents(),
            messageItem = messageItemState,
            reactionSorting = ReactionSortingByLastReactionAt,
            onPollUpdated = { _, _ -> },
            onCastVote = { _, _, _ -> },
            onRemoveVote = { _, _, _ -> },
            selectPoll = { _, _, _ -> },
            onClosePoll = {},
            onAddPollOption = { _, _ -> },
            onLongItemClick = {},
            onThreadClick = {},
            onReactionsClick = {},
            onGiphyActionClick = {},
            onMediaGalleryPreviewResult = {},
            onQuotedMessageClick = {},
            onUserAvatarClick = null,
            onMessageLinkClick = null,
            onUserMentionClick = {},
            onAddAnswer = { _, _, _ -> },
            onReply = {},
        )

        // Options
        componentFactory.MessageMenuOptions(
            modifier = Modifier,
            message = message,
            options = messageOptions,
            onMessageOptionSelected = { onMessageAction(it.action) })
    }

    BackHandler(enabled = true, onBack = onDismiss)
}

private fun Modifier.disablePointerEvents(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

/**
 * Preview of [SelectedMessageMenu].
 */
@Preview(showBackground = true, name = "SelectedMessageMenu Preview")
@Composable
private fun SelectedMessageMenuPreview() {
    ChatTheme {
        val messageOptionsStateList = defaultMessageOptionsState(
            selectedMessage = Message(),
            currentUser = User(),
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
