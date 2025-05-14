/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.components.StreamHorizontalDivider
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastSeenText
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.MessageListHeaderViewModel
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.common.utils.ExpandableList
import kotlinx.coroutines.flow.collectLatest
import java.util.Date

@ExperimentalStreamChatApi
@Composable
public fun GroupChannelInfoScreen(
    viewModelFactory: ChannelInfoViewModelFactory,
    modifier: Modifier = Modifier,
    viewModelKey: String? = null,
    onNavigationIconClick: () -> Unit = {},
    onPinnedMessagesClick: () -> Unit = {},
    topBar: @Composable (elevation: Dp) -> Unit = { elevation ->
        GroupChannelInfoTopBar(
            viewModelFactory = viewModelFactory,
            elevation = elevation,
            onNavigationIconClick = onNavigationIconClick,
        )
    },
) {
    val viewModel = viewModel<ChannelInfoViewModel>(key = viewModelKey, factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val headerElevation by animateDpAsState(
        targetValue = if (listState.canScrollBackward) {
            ChatTheme.dimens.headerElevation
        } else {
            1.dp
        },
    )

    Scaffold(
        modifier = modifier,
        topBar = { topBar(headerElevation) },
        containerColor = ChatTheme.colors.barsBackground,
    ) { padding ->
        GroupChannelInfoContent(
            modifier = Modifier.padding(padding),
            listState = listState,
            state = state,
            onViewAction = viewModel::onViewAction,
            onPinnedMessagesClick = onPinnedMessagesClick,
        )
    }

    var modal by remember { mutableStateOf<ChannelInfoViewEvent.Modal?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is ChannelInfoViewEvent.Modal) {
                modal = event
            }
        }
    }

    ChannelInfoScreenModal(
        modal = modal,
        isGroupChannel = true,
        onViewAction = viewModel::onViewAction,
        onDismiss = { modal = null },
    )
}

@Composable
private fun GroupChannelInfoTopBar(
    viewModelFactory: ChannelInfoViewModelFactory,
    elevation: Dp,
    onNavigationIconClick: () -> Unit,
) {
    val viewModel = viewModel<MessageListHeaderViewModel>(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    MessageListHeader(
        channel = state.channel,
        currentUser = state.currentUser,
        connectionState = state.connectionState,
        elevation = elevation,
        onBackPressed = onNavigationIconClick,
        leadingContent = {
            DefaultChannelInfoScreenNavigationIcon(
                onClick = onNavigationIconClick,
            )
        },
        trailingContent = { // Add members button
            // https://linear.app/stream/issue/AND-537
        },
    )
}

@Composable
private fun GroupChannelInfoContent(
    state: ChannelInfoViewState,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onViewAction: (action: ChannelInfoViewAction) -> Unit = {},
    onPinnedMessagesClick: () -> Unit = {},
) {
    val isLoading = state is ChannelInfoViewState.Loading
    ContentBox(
        modifier = modifier.fillMaxSize(),
        contentAlignment = if (isLoading) Alignment.Center else Alignment.TopCenter,
        isLoading = isLoading,
    ) {
        val content = state as ChannelInfoViewState.Content
        LazyColumn(
            state = listState,
        ) {
            items(
                items = content.members,
                key = { it.user.id },
            ) { member ->
                GroupChannelInfoMemberButton(
                    modifier = Modifier.animateItem(),
                    member = member,
                    onClick = null, // Membership detail flow https://linear.app/stream/issue/AND-537
                )
            }
            if (content.members.canExpand) {
                if (content.members.isCollapsed) {
                    item {
                        GroupChannelInfoExpandMemberButton(
                            collapsedCount = content.members.collapsedCount,
                            onClick = { onViewAction(ChannelInfoViewAction.ExpandMembersClick) },
                        )
                    }
                }
            }
            item {
                StreamHorizontalDivider(thickness = 8.dp)
            }
            items(content.options) { option ->
                ChannelInfoContentOption(
                    option = option,
                    isGroupChannel = false,
                    onViewAction = onViewAction,
                    onPinnedMessagesClick = onPinnedMessagesClick,
                )
            }
        }
    }
}

@Composable
private fun GroupChannelInfoMemberButton(
    member: ChannelInfoViewState.Content.Member,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ChannelInfoOption(
        modifier = modifier,
        onClick = onClick,
    ) {
        val user = member.user
        UserAvatar(
            modifier = Modifier.size(ChatTheme.dimens.channelAvatarSize),
            user = user,
        )
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = user.name.takeIf(String::isNotBlank) ?: user.id,
                style = ChatTheme.typography.body,
                color = ChatTheme.colors.textHighEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = user.getLastSeenText(LocalContext.current),
                style = ChatTheme.typography.footnote,
                color = ChatTheme.colors.textLowEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val role = when (val role = member.role) {
            is ChannelInfoViewState.Content.Role.Owner ->
                stringResource(id = R.string.stream_ui_channel_info_member_owner)

            is ChannelInfoViewState.Content.Role.Member ->
                ""

            is ChannelInfoViewState.Content.Role.Moderator ->
                stringResource(id = R.string.stream_ui_channel_info_member_moderator)

            is ChannelInfoViewState.Content.Role.Other ->
                role.value
        }
        Text(
            text = role,
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textLowEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GroupChannelInfoExpandMemberButton(
    collapsedCount: Int,
    onClick: () -> Unit,
) {
    ChannelInfoOption(
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
        )
        Text(text = stringResource(R.string.stream_ui_channel_info_expand_button, collapsedCount))
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupChannelInfoContentLoadingPreview() {
    ChatTheme {
        GroupChannelInfoContent(
            state = ChannelInfoViewState.Loading,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupChannelInfoContentCollapsedPreview() {
    ChatTheme {
        GroupChannelInfoContent(
            state = ChannelInfoViewState.Content(
                members = ExpandableList(
                    items = listOf(
                        ChannelInfoViewState.Content.Member(
                            user = PreviewUserData.user1.copy(lastActive = Date()),
                            role = ChannelInfoViewState.Content.Role.Owner,
                        ),
                        ChannelInfoViewState.Content.Member(
                            user = PreviewUserData.user2.copy(online = true),
                            role = ChannelInfoViewState.Content.Role.Owner,
                        ),
                        ChannelInfoViewState.Content.Member(
                            user = PreviewUserData.user3,
                            role = ChannelInfoViewState.Content.Role.Owner,
                        ),
                        ChannelInfoViewState.Content.Member(
                            user = PreviewUserData.user4,
                            role = ChannelInfoViewState.Content.Role.Owner,
                        ),
                    ),
                    minimumVisibleItems = 2,
                    isCollapsed = true,
                ),
                options = listOf(
                    ChannelInfoViewState.Content.Option.RenameChannel(name = "Group Channel", isReadOnly = false),
                    ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                    ChannelInfoViewState.Content.Option.HideChannel(isHidden = true),
                    ChannelInfoViewState.Content.Option.PinnedMessages,
                    ChannelInfoViewState.Content.Option.Separator,
                    ChannelInfoViewState.Content.Option.LeaveChannel,
                    ChannelInfoViewState.Content.Option.DeleteChannel,
                ),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupChannelInfoContentExpandedPreview() {
    ChatTheme {
        GroupChannelInfoContent(
            state = ChannelInfoViewState.Content(
                members = ExpandableList(
                    items = listOf(
                        ChannelInfoViewState.Content.Member(
                            user = PreviewUserData.user1.copy(lastActive = Date()),
                            role = ChannelInfoViewState.Content.Role.Owner,
                        ),
                        ChannelInfoViewState.Content.Member(
                            user = PreviewUserData.user2.copy(online = true),
                            role = ChannelInfoViewState.Content.Role.Owner,
                        ),
                        ChannelInfoViewState.Content.Member(
                            user = PreviewUserData.user3,
                            role = ChannelInfoViewState.Content.Role.Owner,
                        ),
                        ChannelInfoViewState.Content.Member(
                            user = PreviewUserData.user4,
                            role = ChannelInfoViewState.Content.Role.Owner,
                        ),
                    ),
                    minimumVisibleItems = 2,
                    isCollapsed = false,
                ),
                options = listOf(
                    ChannelInfoViewState.Content.Option.RenameChannel(name = "Group Channel", isReadOnly = true),
                    ChannelInfoViewState.Content.Option.MuteChannel(isMuted = true),
                    ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                    ChannelInfoViewState.Content.Option.PinnedMessages,
                    ChannelInfoViewState.Content.Option.Separator,
                    ChannelInfoViewState.Content.Option.LeaveChannel,
                    ChannelInfoViewState.Content.Option.DeleteChannel,
                ),
            ),
        )
    }
}
