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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.MessageReactionItemState
import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewReactionData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.React

/**
 * Represents the list of user reactions as a draggable bottom sheet.
 *
 * @param message The selected message.
 * @param currentUser The currently logged in user.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 * @param onMessageAction Handler that propagates click events on each item.
 * @param onShowMoreReactionsSelected Handler that propagates clicks on the show more reactions button.
 * @param modifier Modifier for styling.
 * @param onDismiss Handler called when the menu is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun SelectedReactionsMenu(
    message: Message,
    currentUser: User?,
    ownCapabilities: Set<String>,
    onMessageAction: (MessageAction) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    ModalBottomSheet(
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(),
        shape = ChatTheme.shapes.bottomSheet,
        containerColor = ChatTheme.colors.backgroundElevationElevation1,
        scrimColor = ChatTheme.colors.overlayBackground,
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        ChatTheme.componentFactory.ReactionsMenuContent(
            modifier = Modifier.fillMaxHeight(),
            currentUser = currentUser,
            message = message,
            onMessageAction = onMessageAction,
            onShowMoreReactionsSelected = onShowMoreReactionsSelected,
            ownCapabilities = ownCapabilities,
        )
    }
}

/**
 * Default content for the reactions menu bottom sheet.
 *
 * Composes the reaction count title, the reaction count row (chips), and the user reactions list
 * inside a scrollable column.
 *
 * @param message The selected message.
 * @param currentUser The currently logged in user.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 * @param onMessageAction Handler that propagates click events on each item.
 * @param onShowMoreReactionsSelected Handler that propagates clicks on the show more reactions button.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun ReactionsMenuContent(
    message: Message,
    currentUser: User?,
    ownCapabilities: Set<String>,
    onMessageAction: (MessageAction) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reactionGroups = buildReactionGroups(message)
    val userReactions = buildUserReactionItems(
        message = message,
        currentUser = currentUser,
    )
    val resolver = ChatTheme.reactionResolver
    val onAddReactionClick = onShowMoreReactionsSelected
        .takeIf { ChannelCapabilities.SEND_REACTION in ownCapabilities }
    val onReactionOptionSelected: (String) -> Unit = { type ->
        onMessageAction(
            React(
                reaction = Reaction(
                    messageId = message.id,
                    type = type,
                    emojiCode = resolver.emojiCode(type),
                ),
                message = message,
            ),
        )
    }

    val reactionCountText = LocalResources.current.getQuantityString(
        R.plurals.stream_compose_message_reactions,
        userReactions.size,
        userReactions.size,
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.backgroundElevationElevation1)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = reactionCountText,
            style = ChatTheme.typography.title3Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textPrimary,
        )

        ReactionCountRow(
            reactionGroups = reactionGroups,
            ownReactions = message.ownReactions,
            onReactionOptionSelected = onReactionOptionSelected,
            onAddReactionClick = onAddReactionClick,
        )

        userReactions.forEach { item ->
            UserReactionRow(
                item = item,
                onClick = if (item.isMine) {
                    { onReactionOptionSelected(item.type) }
                } else {
                    null
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Builds a list of [MessageReactionItemState] from the message's reaction groups.
 *
 * @param message The message to build reaction groups from.
 */
@Composable
private fun buildReactionGroups(message: Message): List<MessageReactionItemState> {
    val resolver = ChatTheme.reactionResolver
    return remember(resolver, message.reactionGroups) {
        val supported = resolver.supportedReactions
        message.reactionGroups
            .entries
            .filter { it.key in supported }
            .map { (type, group) ->
                MessageReactionItemState(
                    type = type,
                    emoji = resolver.emojiCode(type),
                    count = group.count,
                )
            }
    }
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

@Composable
private fun ReactionsMenuContentPreview(selectedMessage: Message) {
    ReactionsMenuContent(
        message = selectedMessage,
        currentUser = PreviewUserData.user1,
        onMessageAction = {},
        onShowMoreReactionsSelected = {},
        ownCapabilities = ChannelCapabilities.toSet(),
    )
}

@Composable
internal fun ReactionsMenuContentOneReaction() {
    ReactionsMenuContentPreview(
        selectedMessage = PreviewMessageData.message1.copy(
            latestReactions = PreviewReactionData.oneReaction,
            reactionGroups = PreviewReactionData.oneReactionGroup,
        ),
    )
}

@Composable
internal fun ReactionsMenuContentManyReactions() {
    ReactionsMenuContentPreview(
        PreviewMessageData.message1.copy(
            latestReactions = PreviewReactionData.manyReaction,
            reactionGroups = PreviewReactionData.manyReactionGroups,
        ),
    )
}

@Preview
@Composable
private fun OneSelectedReactionMenuPreview() {
    ChatTheme {
        ReactionsMenuContentOneReaction()
    }
}

@Preview
@Composable
private fun ManySelectedReactionsMenuPreview() {
    ChatTheme {
        ReactionsMenuContentManyReactions()
    }
}
