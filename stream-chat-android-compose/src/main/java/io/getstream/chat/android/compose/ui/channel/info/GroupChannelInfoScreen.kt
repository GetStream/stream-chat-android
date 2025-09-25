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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.OnlineIndicatorAlignment
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.avatar.DefaultOnlineIndicator
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastSeenText
import io.getstream.chat.android.compose.viewmodel.channel.ChannelHeaderViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModelFactory
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.common.state.messages.list.ChannelHeaderViewState
import io.getstream.chat.android.ui.common.utils.ExpandableList
import io.getstream.chat.android.ui.common.utils.extensions.shouldShowOnlineIndicator
import kotlinx.coroutines.flow.collectLatest
import java.util.Date

/**
 * A stateful screen component that displays the channel info for a group channel,
 * including its members and the options available for the channel.
 *
 * @param viewModelFactory The factory used to create the [ChannelInfoViewModel].
 * @param modifier The [Modifier] to be applied to this screen.
 * @param currentUser The current logged-in user. Defaults to the current user from the [ChatClient].
 * @param onNavigationIconClick Callback invoked when the navigation icon is clicked.
 * @param onAddMembersClick Callback invoked when the "Add Members" button is clicked.
 */
@Composable
public fun GroupChannelInfoScreen(
    viewModelFactory: ChannelInfoViewModelFactory,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    onNavigationIconClick: () -> Unit = {},
    onAddMembersClick: () -> Unit = {},
) {
    val headerViewModel = viewModel<ChannelHeaderViewModel>(factory = viewModelFactory)
    val infoViewModel = viewModel<ChannelInfoViewModel>(factory = viewModelFactory)
    val headerState by headerViewModel.state.collectAsStateWithLifecycle()
    val infoState by infoViewModel.state.collectAsStateWithLifecycle()

    GroupChannelInfoScaffold(
        modifier = modifier,
        currentUser = currentUser,
        headerState = headerState,
        infoState = infoState,
        onNavigationIconClick = onNavigationIconClick,
        onAddMembersClick = onAddMembersClick,
        onViewAction = infoViewModel::onViewAction,
    )

    GroupChannelInfoScreenModal(infoViewModel)
}

@Composable
private fun GroupChannelInfoScaffold(
    modifier: Modifier,
    currentUser: User?,
    headerState: ChannelHeaderViewState,
    infoState: ChannelInfoViewState,
    onNavigationIconClick: () -> Unit = {},
    onAddMembersClick: () -> Unit = {},
    onViewAction: (action: ChannelInfoViewAction) -> Unit = {},
) {
    val listState = rememberLazyListState()
    Scaffold(
        modifier = modifier,
        topBar = {
            ChatTheme.componentFactory.GroupChannelInfoTopBar(
                headerState = headerState,
                infoState = infoState,
                listState = listState,
                onNavigationIconClick = onNavigationIconClick,
                onAddMembersClick = onAddMembersClick,
            )
        },
        containerColor = ChatTheme.colors.barsBackground,
    ) { padding ->
        GroupChannelInfoContent(
            modifier = Modifier.padding(padding),
            listState = listState,
            state = infoState,
            currentUser = currentUser,
            onViewAction = onViewAction,
        )
    }
}

@Composable
private fun GroupChannelInfoScreenModal(viewModel: ChannelInfoViewModel) {
    var modal by remember { mutableStateOf<ChannelInfoViewEvent.Modal?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is ChannelInfoViewEvent.Modal) {
                modal = event
            }
        }
    }

    ChatTheme.componentFactory.ChannelInfoScreenModal(
        modal = modal,
        isGroupChannel = true,
        onViewAction = viewModel::onViewAction,
        onMemberViewEvent = viewModel::onMemberViewEvent,
        onDismiss = { modal = null },
    )
}

@Composable
internal fun GroupChannelInfoTopBar(
    headerState: ChannelHeaderViewState,
    infoState: ChannelInfoViewState,
    listState: LazyListState,
    onNavigationIconClick: () -> Unit,
    onAddMembersClick: () -> Unit,
) {
    val elevation by animateDpAsState(
        targetValue = if (listState.canScrollBackward) {
            ChatTheme.dimens.headerElevation
        } else {
            1.dp
        },
    )
    when (headerState) {
        is ChannelHeaderViewState.Loading -> LoadingIndicator(
            modifier = Modifier.fillMaxWidth(),
        )

        is ChannelHeaderViewState.Content -> MessageListHeader(
            channel = headerState.channel,
            currentUser = headerState.currentUser,
            connectionState = headerState.connectionState,
            elevation = elevation,
            onBackPressed = onNavigationIconClick,
            leadingContent = {
                ChannelInfoNavigationIcon(
                    onClick = onNavigationIconClick,
                )
            },
            trailingContent = {
                if (infoState is ChannelInfoViewState.Content &&
                    infoState.options.contains(ChannelInfoViewState.Content.Option.AddMember)
                ) {
                    ChatTheme.componentFactory.GroupChannelInfoAddMembersButton(
                        onClick = onAddMembersClick,
                    )
                }
            },
        )
    }
}

@Composable
private fun GroupChannelInfoContent(
    state: ChannelInfoViewState,
    modifier: Modifier,
    currentUser: User?,
    listState: LazyListState,
    onViewAction: (action: ChannelInfoViewAction) -> Unit,
) {
    val isLoading = state is ChannelInfoViewState.Loading
    ContentBox(
        modifier = modifier.fillMaxSize(),
        isLoading = isLoading,
    ) {
        val content = state as ChannelInfoViewState.Content
        LazyColumn(
            modifier = Modifier.matchParentSize(),
            state = listState,
        ) {
            items(
                items = content.members,
                key = Member::getUserId,
            ) { member ->
                val isCurrentUserMember = member.getUserId() == currentUser?.id
                with(ChatTheme.componentFactory) {
                    GroupChannelInfoMemberItem(
                        currentUser = currentUser,
                        member = member,
                        isOwner = content.owner.id == member.getUserId(),
                        onClick = if (isCurrentUserMember) {
                            null
                        } else {
                            { onViewAction(ChannelInfoViewAction.MemberClick(member = member)) }
                        },
                    )
                }
            }
            if (content.members.canExpand && content.members.isCollapsed) {
                item {
                    with(ChatTheme.componentFactory) {
                        GroupChannelInfoExpandMembersItem(
                            collapsedCount = content.members.collapsedCount,
                            onClick = { onViewAction(ChannelInfoViewAction.ExpandMembersClick) },
                        )
                    }
                }
            }
            item {
                with(ChatTheme.componentFactory) {
                    ChannelInfoSeparatorItem()
                }
            }
            items(content.options) { option ->
                with(ChatTheme.componentFactory) {
                    ChannelInfoOptionItem(
                        option = option,
                        isGroupChannel = true,
                        onViewAction = onViewAction,
                    )
                }
            }
        }
    }
}

@Composable
internal fun GroupChannelInfoMemberItem(
    modifier: Modifier,
    currentUser: User?,
    member: Member,
    isOwner: Boolean,
    onClick: (() -> Unit)?,
) {
    ChannelInfoOption(
        modifier = modifier,
        onClick = onClick,
    ) {
        val user = member.user
        ChatTheme.componentFactory.UserAvatar(
            modifier = Modifier.size(ChatTheme.dimens.channelAvatarSize),
            user = user,
            textStyle = ChatTheme.typography.title3Bold,
            showOnlineIndicator = user.shouldShowOnlineIndicator(
                userPresence = ChatTheme.userPresence,
                currentUser = currentUser,
            ),
            onlineIndicator = { DefaultOnlineIndicator(onlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd) },
            onClick = null,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name.takeIf(String::isNotBlank) ?: user.id,
                style = ChatTheme.typography.bodyBold,
                color = if (member.banned) {
                    ChatTheme.colors.errorAccent
                } else {
                    ChatTheme.colors.textHighEmphasis
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = user.getLastSeenText(LocalContext.current),
                style = ChatTheme.typography.footnote,
                color = if (member.banned) {
                    ChatTheme.colors.errorAccent
                } else {
                    ChatTheme.colors.textLowEmphasis
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val role = if (isOwner) {
            stringResource(id = R.string.stream_ui_channel_info_member_owner)
        } else {
            when (val role = member.channelRole) {
                "channel_moderator" -> stringResource(id = R.string.stream_ui_channel_info_member_moderator)
                "channel_member" -> ""
                else -> role.orEmpty()
            }
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
internal fun GroupChannelInfoExpandMembersItem(
    collapsedCount: Int,
    onClick: () -> Unit,
) {
    CompositionLocalProvider(LocalContentColor.provides(ChatTheme.colors.textLowEmphasis)) {
        ChannelInfoOption(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
            )
            Text(text = stringResource(R.string.stream_ui_channel_info_expand_button, collapsedCount))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupChannelInfoLoadingPreview() {
    ChatTheme {
        GroupChannelInfoLoading()
    }
}

@Composable
internal fun GroupChannelInfoLoading() {
    GroupChannelInfoScaffold(
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
        headerState = ChannelHeaderViewState.Loading,
        infoState = ChannelInfoViewState.Loading,
    )
}

@Preview(showBackground = true)
@Composable
private fun GroupChannelInfoCollapsedMembersPreview() {
    ChatTheme {
        GroupChannelInfoCollapsedMembers()
    }
}

@Composable
internal fun GroupChannelInfoCollapsedMembers() {
    GroupChannelInfoScaffold(
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
        headerState = ChannelHeaderViewState.Content(
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Connected,
            channel = PreviewChannelData.channelWithImage,
        ),
        infoState = ChannelInfoViewState.Content(
            owner = PreviewUserData.user1,
            members = ExpandableList(
                items = listOf(
                    Member(user = PreviewUserData.user1.copy(lastActive = Date())),
                    Member(user = PreviewUserData.user2.copy(online = true)),
                    Member(user = PreviewUserData.user3),
                    Member(user = PreviewUserData.user4),
                ),
                minimumVisibleItems = 2,
                isCollapsed = true,
            ),
            options = listOf(
                ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = false),
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

@Preview(showBackground = true)
@Composable
private fun GroupChannelInfoExpandedMembersPreview() {
    ChatTheme {
        GroupChannelInfoExpandedMembers()
    }
}

@Composable
internal fun GroupChannelInfoExpandedMembers() {
    GroupChannelInfoScaffold(
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
        headerState = ChannelHeaderViewState.Content(
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Connected,
            channel = PreviewChannelData.channelWithImage,
        ),
        infoState = ChannelInfoViewState.Content(
            owner = PreviewUserData.user1,
            members = ExpandableList(
                items = listOf(
                    Member(user = PreviewUserData.user1.copy(lastActive = Date())),
                    Member(user = PreviewUserData.user2.copy(online = true)),
                    Member(user = PreviewUserData.user3),
                    Member(user = PreviewUserData.user4.copy(lastActive = Date()), banned = true),
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
