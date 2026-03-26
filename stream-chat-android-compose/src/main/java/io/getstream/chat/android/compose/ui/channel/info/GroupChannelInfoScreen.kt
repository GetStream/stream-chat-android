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

package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.components.FullscreenDialog
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChannelAvatarParams
import io.getstream.chat.android.compose.ui.theme.ChannelInfoScreenModalParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.GroupChannelInfoAvatarContainerParams
import io.getstream.chat.android.compose.ui.theme.GroupChannelInfoExpandMembersItemParams
import io.getstream.chat.android.compose.ui.theme.GroupChannelInfoMemberItemParams
import io.getstream.chat.android.compose.ui.theme.GroupChannelInfoMemberSectionParams
import io.getstream.chat.android.compose.ui.theme.GroupChannelInfoTopBarParams
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.theme.UserAvatarParams
import io.getstream.chat.android.compose.ui.util.ViewModelStore
import io.getstream.chat.android.compose.ui.util.bottomBorder
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.getLastSeenText
import io.getstream.chat.android.compose.ui.util.topBorder
import io.getstream.chat.android.compose.viewmodel.channel.AddMembersViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelHeaderViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModelFactory
import io.getstream.chat.android.models.Channel
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
 * @param onActionClick Callback invoked when the action button is clicked.
 */
@Composable
public fun GroupChannelInfoScreen(
    viewModelFactory: ChannelInfoViewModelFactory,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    onNavigationIconClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
) {
    val headerViewModel = viewModel<ChannelHeaderViewModel>(factory = viewModelFactory)
    val infoViewModel = viewModel<ChannelInfoViewModel>(factory = viewModelFactory)
    val headerState by headerViewModel.state.collectAsStateWithLifecycle()
    val infoState by infoViewModel.state.collectAsStateWithLifecycle()

    var showAddMembers by remember { mutableStateOf(false) }

    GroupChannelInfoScaffold(
        modifier = modifier,
        currentUser = currentUser,
        headerState = headerState,
        infoState = infoState,
        onNavigationIconClick = onNavigationIconClick,
        onActionClick = onActionClick,
        onAddMembersClick = { showAddMembers = true },
        onViewAction = infoViewModel::onViewAction,
    )

    GroupChannelInfoScreenModal(infoViewModel)
    if (showAddMembers) {
        FullscreenDialog(onDismissRequest = { showAddMembers = false }) {
            ViewModelStore {
                val addMembersViewModel = viewModel<AddMembersViewModel>(factory = viewModelFactory)
                AddMembersScreen(
                    viewModel = addMembersViewModel,
                    onDismiss = { showAddMembers = false },
                    onConfirm = { userIds ->
                        infoViewModel.addMembers(userIds)
                        showAddMembers = false
                    },
                )
            }
        }
    }
}

@Composable
private fun GroupChannelInfoScaffold(
    modifier: Modifier,
    currentUser: User?,
    headerState: ChannelHeaderViewState,
    infoState: ChannelInfoViewState,
    onNavigationIconClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    onAddMembersClick: () -> Unit = {},
    onViewAction: (action: ChannelInfoViewAction) -> Unit = {},
) {
    val listState = rememberLazyListState()
    Scaffold(
        modifier = modifier,
        topBar = {
            ChatTheme.componentFactory.GroupChannelInfoTopBar(
                params = GroupChannelInfoTopBarParams(
                    headerState = headerState,
                    infoState = infoState,
                    listState = listState,
                    onNavigationIconClick = onNavigationIconClick,
                    onActionClick = onActionClick,
                ),
            )
        },
        containerColor = ChatTheme.colors.backgroundCoreApp,
    ) { padding ->
        GroupChannelInfoContent(
            modifier = Modifier.padding(padding),
            headerState = headerState,
            listState = listState,
            state = infoState,
            currentUser = currentUser,
            onAddMembersClick = onAddMembersClick,
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
        params = ChannelInfoScreenModalParams(
            modal = modal,
            isGroupChannel = true,
            onViewAction = viewModel::onViewAction,
            onMemberViewEvent = viewModel::onMemberViewEvent,
            onDismiss = { modal = null },
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GroupChannelInfoTopBar(
    headerState: ChannelHeaderViewState,
    infoState: ChannelInfoViewState,
    listState: LazyListState,
    onNavigationIconClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.bottomBorder(color = ChatTheme.colors.borderCoreSubtle),
        title = {
            Text(
                text = stringResource(R.string.stream_ui_channel_info_group_title),
                style = ChatTheme.typography.headingMedium,
                maxLines = 1,
            )
        },
        navigationIcon = {
            ChannelInfoNavigationIcon(
                onClick = onNavigationIconClick,
            )
        },
        actions = {
            if (infoState is ChannelInfoViewState.Content &&
                infoState.options.any { option -> option is ChannelInfoViewState.Content.Option.EditChannel }
            ) {
                StreamTextButton(
                    style = StreamButtonStyleDefaults.secondaryOutline,
                    text = stringResource(id = R.string.stream_ui_channel_info_edit_action),
                    onClick = onActionClick,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = ChatTheme.colors.backgroundCoreApp,
            scrolledContainerColor = ChatTheme.colors.backgroundCoreApp,
            titleContentColor = ChatTheme.colors.textPrimary,
            navigationIconContentColor = ChatTheme.colors.textPrimary,
            actionIconContentColor = ChatTheme.colors.textPrimary,
        ),
    )
}

@Suppress("LongMethod", "LongParameterList")
@Composable
private fun GroupChannelInfoContent(
    headerState: ChannelHeaderViewState,
    state: ChannelInfoViewState,
    modifier: Modifier,
    currentUser: User?,
    listState: LazyListState,
    onAddMembersClick: () -> Unit,
    onViewAction: (action: ChannelInfoViewAction) -> Unit,
) {
    val isLoading = state is ChannelInfoViewState.Loading
    ContentBox(
        modifier = modifier.fillMaxSize(),
        isLoading = isLoading,
    ) {
        val content = state as ChannelInfoViewState.Content
        val header = headerState as? ChannelHeaderViewState.Content
        val navigationOptions = content.options.filterNavigation()
        val actionOptions = content.options.filterActions()
        val totalMembers = content.members.size + content.members.collapsedCount
        val showAddButton = content.options.any {
            it is ChannelInfoViewState.Content.Option.AddMember
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.matchParentSize(),
            contentPadding = PaddingValues(
                start = StreamTokens.spacingMd,
                end = StreamTokens.spacingMd,
                top = StreamTokens.spacing2xl,
                bottom = StreamTokens.spacing3xl,
            ),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingMd),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar section
            if (header != null) {
                item {
                    ChatTheme.componentFactory.GroupChannelInfoAvatarContainer(
                        params = GroupChannelInfoAvatarContainerParams(
                            channel = header.channel,
                            currentUser = header.currentUser,
                            members = content.members,
                        ),
                    )
                }
            }

            // Navigation section
            if (navigationOptions.isNotEmpty()) {
                item {
                    ChannelInfoSection {
                        navigationOptions.forEach { option ->
                            ChannelInfoOptionContent(
                                option = option,
                                isGroupChannel = true,
                                onViewAction = onViewAction,
                            )
                        }
                    }
                }
            }

            // Members section
            if (content.members.isNotEmpty()) {
                item {
                    ChatTheme.componentFactory.GroupChannelInfoMemberSection(
                        params = GroupChannelInfoMemberSectionParams(
                            members = content.members,
                            currentUser = currentUser,
                            owner = content.owner,
                            totalMemberCount = totalMembers,
                            showAddButton = showAddButton,
                            onAddMembersClick = onAddMembersClick,
                            onViewAction = onViewAction,
                        ),
                    )
                }
            }

            // Actions section
            if (actionOptions.isNotEmpty()) {
                item {
                    ChannelInfoSection {
                        actionOptions.forEach { option ->
                            ChannelInfoOptionContent(
                                option = option,
                                isGroupChannel = true,
                                onViewAction = onViewAction,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun GroupChannelInfoAvatarContainer(
    channel: Channel,
    currentUser: User?,
    members: ExpandableList<Member>,
) {
    val totalMembers = members.size + members.collapsedCount
    val onlineCount = channel.members.count { it.user.online }
    Column(
        modifier = Modifier.padding(bottom = StreamTokens.spacingMd),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChatTheme.componentFactory.ChannelAvatar(
            params = ChannelAvatarParams(
                modifier = Modifier.size(AvatarSize.ExtraExtraLarge),
                channel = channel,
                currentUser = currentUser,
                showIndicator = false,
            ),
        )
        Spacer(modifier = Modifier.height(StreamTokens.spacingMd))
        Text(
            text = ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser),
            style = ChatTheme.typography.headingLarge,
            color = ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(StreamTokens.spacingXs))
        Text(
            text = stringResource(
                R.string.stream_ui_channel_info_member_count_online,
                totalMembers,
                onlineCount,
            ),
            style = ChatTheme.typography.captionDefault,
            color = ChatTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Suppress("LongParameterList")
@Composable
internal fun GroupChannelInfoMemberSection(
    members: ExpandableList<Member>,
    currentUser: User?,
    owner: User,
    totalMemberCount: Int,
    showAddButton: Boolean,
    onAddMembersClick: () -> Unit,
    onViewAction: (action: ChannelInfoViewAction) -> Unit,
) {
    ChannelInfoSection {
        // Header row: "N members" + "Add" button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StreamTokens.spacingMd)
                .defaultMinSize(minHeight = LocalMinimumInteractiveComponentSize.current),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pluralStringResource(
                    R.plurals.stream_ui_channel_info_member_count,
                    totalMemberCount,
                    totalMemberCount,
                ),
                style = ChatTheme.typography.headingSmall,
                color = ChatTheme.colors.textPrimary,
            )
            if (showAddButton) {
                ChatTheme.componentFactory.GroupChannelInfoAddMembersButton(
                    params = GroupChannelInfoAddMembersButtonParams(onAddMembersClick),
                )
            }
        }

        // Member items
        members.forEach { member ->
            val isCurrentUserMember = member.getUserId() == currentUser?.id
            ChatTheme.componentFactory.GroupChannelInfoMemberItem(
                params = GroupChannelInfoMemberItemParams(
                    currentUser = currentUser,
                    member = member,
                    isOwner = owner.id == member.getUserId(),
                    onClick = if (isCurrentUserMember) {
                        null
                    } else {
                        { onViewAction(ChannelInfoViewAction.MemberClick(member = member)) }
                    },
                ),
            )
        }

        // "View all" footer
        if (members.canExpand && members.isCollapsed) {
            ChatTheme.componentFactory.GroupChannelInfoExpandMembersItem(
                params = GroupChannelInfoExpandMembersItemParams(
                    collapsedCount = members.collapsedCount,
                    onClick = { onViewAction(ChannelInfoViewAction.ExpandMembersClick) },
                ),
            )
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
            params = UserAvatarParams(
                modifier = Modifier.size(AvatarSize.Medium),
                user = user,
                showIndicator = true,
                showBorder = false,
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            val errorColor = ChatTheme.colors.accentError
            Text(
                text = user.name.takeIf(String::isNotBlank) ?: user.id,
                style = ChatTheme.typography.bodyDefault,
                color = if (member.banned) errorColor else ChatTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = user.getLastSeenText(LocalContext.current),
                style = ChatTheme.typography.metadataDefault,
                color = if (member.banned) errorColor else ChatTheme.colors.textTertiary,
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
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textTertiary,
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
    Box(
        modifier = Modifier
            .padding(top = StreamTokens.spacingSm)
            .topBorder(color = ChatTheme.colors.borderCoreDefault)
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.stream_ui_channel_info_view_all),
            style = ChatTheme.typography.bodyEmphasis,
            color = ChatTheme.colors.buttonSecondaryText,
        )
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
    val channel = PreviewChannelData.channelWithImage.copy(name = "Channel Name")
    GroupChannelInfoScaffold(
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
        headerState = ChannelHeaderViewState.Content(
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Connected,
            channel = channel,
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
                ChannelInfoViewState.Content.Option.AddMember,
                ChannelInfoViewState.Content.Option.EditChannel(name = channel.name),
                ChannelInfoViewState.Content.Option.PinnedMessages,
                ChannelInfoViewState.Content.Option.MediaAttachments,
                ChannelInfoViewState.Content.Option.FilesAttachments,
                ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                ChannelInfoViewState.Content.Option.LeaveChannel,
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
                ChannelInfoViewState.Content.Option.AddMember,
                ChannelInfoViewState.Content.Option.PinnedMessages,
                ChannelInfoViewState.Content.Option.MediaAttachments,
                ChannelInfoViewState.Content.Option.FilesAttachments,
                ChannelInfoViewState.Content.Option.MuteChannel(isMuted = true),
                ChannelInfoViewState.Content.Option.LeaveChannel,
            ),
        ),
    )
}
