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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.state.messages.MessageReactionItemState
import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.ShimmerProgressIndicator
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.ViewModelStore
import io.getstream.chat.android.compose.viewmodel.messages.ReactionsMenuViewModel
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
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = ChatTheme.colors.backgroundElevationElevation1,
        scrimColor = ChatTheme.colors.backgroundCoreScrim,
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
 * inside a scrollable column with pagination support.
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
    ViewModelStore(message.id) {
        val viewModel = viewModel { ReactionsMenuViewModel(messageId = message.id) }

        val state by viewModel.state.collectAsState()

        val reactionGroups = buildReactionGroups(message)
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

        val userReactions = buildUserReactionItems(
            reactions = state.reactions,
            currentUser = currentUser,
        )

        ReactionsMenuList(
            reactionGroups = reactionGroups,
            items = userReactions,
            selectedReactionType = state.selectedReactionType,
            isLoading = state.isLoading,
            isLoadingMore = state.isLoadingMore,
            onReactionSelected = viewModel::selectReaction,
            onReactionOptionSelected = onReactionOptionSelected,
            onAddReactionClick = onAddReactionClick,
            onLoadMore = viewModel::loadMore,
            modifier = modifier,
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun ReactionsMenuList(
    reactionGroups: List<MessageReactionItemState>,
    items: List<UserReactionItemState>,
    selectedReactionType: String?,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    onReactionSelected: (String) -> Unit,
    onReactionOptionSelected: (String) -> Unit,
    onAddReactionClick: (() -> Unit)?,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LoadMoreHandler(
        lazyListState = listState,
        loadMore = onLoadMore,
    )

    LazyColumn(
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.backgroundElevationElevation1),
    ) {
        item(key = "Stream_header") {
            val totalCount = reactionGroups.sumOf(MessageReactionItemState::count)
            Text(
                text = pluralStringResource(R.plurals.stream_compose_message_reactions, totalCount, totalCount),
                style = ChatTheme.typography.headingMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textPrimary,
            )
            ReactionCountRow(
                reactionGroups = reactionGroups,
                selectedReactionType = selectedReactionType,
                onReactionSelected = onReactionSelected,
                onAddReactionClick = onAddReactionClick,
            )
        }

        if (isLoading) {
            items(ShimmerItemCount, key = { "Stream_shimmer_$it" }) {
                UserReactionShimmerItem()
            }
        } else {
            items(
                items = items,
                key = { item -> "${item.user.id}_${item.type}" },
            ) { item ->
                UserReactionRow(
                    modifier = Modifier.padding(bottom = StreamTokens.spacingXs),
                    item = item,
                    onClick = if (item.isMine) {
                        { onReactionOptionSelected(item.type) }
                    } else {
                        null
                    },
                )
            }

            if (isLoadingMore) {
                item(key = "Stream_loading_more") {
                    LoadingIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun UserReactionShimmerItem(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = StreamTokens.spacingSm, vertical = StreamTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        ShimmerProgressIndicator(
            modifier = Modifier
                .size(AvatarSize.Medium)
                .clip(CircleShape),
        )
        ShimmerProgressIndicator(
            modifier = Modifier
                .width(200.dp)
                .height(16.dp)
                .clip(CircleShape),
        )
    }
}

private const val ShimmerItemCount = 8

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
 * Builds a list of user reactions from the loaded reactions list.
 *
 * @param reactions The list of reactions loaded from the API.
 * @param currentUser The currently logged in user.
 */
@Composable
private fun buildUserReactionItems(
    reactions: List<Reaction>,
    currentUser: User?,
): List<UserReactionItemState> {
    val resolver = ChatTheme.reactionResolver
    return remember(reactions, currentUser, resolver) {
        reactions.mapNotNull {
            val user = it.user ?: return@mapNotNull null
            UserReactionItemState(
                user = user,
                type = it.type,
                isMine = currentUser?.id == user.id,
                emojiCode = resolver.emojiCode(it.type),
            )
        }
    }
}

@Composable
private fun ReactionsMenuListPreview(message: Message) {
    ReactionsMenuList(
        reactionGroups = buildReactionGroups(message),
        items = buildUserReactionItems(
            reactions = message.latestReactions,
            currentUser = PreviewUserData.user1,
        ),
        selectedReactionType = null,
        isLoading = false,
        isLoadingMore = false,
        onReactionSelected = {},
        onReactionOptionSelected = {},
        onAddReactionClick = {},
        onLoadMore = {},
    )
}

@Composable
internal fun ReactionsMenuContentOneReaction() {
    ReactionsMenuListPreview(
        message = PreviewMessageData.message1.copy(
            latestReactions = PreviewReactionData.oneReaction,
            reactionGroups = PreviewReactionData.oneReactionGroup,
        ),
    )
}

@Composable
internal fun ReactionsMenuContentManyReactions() {
    ReactionsMenuListPreview(
        message = PreviewMessageData.message1.copy(
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
