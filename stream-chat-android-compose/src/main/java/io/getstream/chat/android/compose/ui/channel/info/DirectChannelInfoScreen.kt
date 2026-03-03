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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
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
 * A stateful screen component that displays the channel info for a direct channel,
 * including the options available for the channel.
 *
 * @param viewModelFactory The factory used to create the [ChannelInfoViewModel].
 * @param modifier The [Modifier] to be applied to this screen.
 * @param onNavigationIconClick Callback invoked when the navigation icon is clicked.
 */
@Composable
public fun DirectChannelInfoScreen(
    viewModelFactory: ChannelInfoViewModelFactory,
    modifier: Modifier = Modifier,
    onNavigationIconClick: () -> Unit = {},
) {
    val headerViewModel = viewModel<ChannelHeaderViewModel>(factory = viewModelFactory)
    val infoViewModel = viewModel<ChannelInfoViewModel>(factory = viewModelFactory)
    val headerState by headerViewModel.state.collectAsStateWithLifecycle()
    val infoState by infoViewModel.state.collectAsStateWithLifecycle()

    DirectChannelInfoScaffold(
        modifier = modifier,
        headerState = headerState,
        infoState = infoState,
        onNavigationIconClick = onNavigationIconClick,
        onViewAction = infoViewModel::onViewAction,
    )

    DirectChannelInfoScreenModal(infoViewModel)
}

@Composable
private fun DirectChannelInfoScaffold(
    modifier: Modifier,
    headerState: ChannelHeaderViewState,
    infoState: ChannelInfoViewState,
    onNavigationIconClick: () -> Unit = {},
    onViewAction: (action: ChannelInfoViewAction) -> Unit = {},
) {
    val listState = rememberLazyListState()
    Scaffold(
        modifier = modifier,
        topBar = {
            ChatTheme.componentFactory.DirectChannelInfoTopBar(
                headerState = headerState,
                listState = listState,
                onNavigationIconClick = onNavigationIconClick,
            )
        },
        containerColor = ChatTheme.colors.backgroundElevationElevation1,
    ) { padding ->
        DirectChannelInfoContent(
            state = infoState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            listState = listState,
            onViewAction = onViewAction,
        )
    }
}

@Composable
private fun DirectChannelInfoScreenModal(viewModel: ChannelInfoViewModel) {
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
        isGroupChannel = false,
        onViewAction = viewModel::onViewAction,
        onMemberViewEvent = viewModel::onMemberViewEvent,
        onDismiss = { modal = null },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DirectChannelInfoTopBar(
    onNavigationIconClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.stream_ui_channel_info_contact_title),
                style = ChatTheme.typography.headingMedium,
                maxLines = 1,
            )
        },
        navigationIcon = {
            ChannelInfoNavigationIcon(
                onClick = onNavigationIconClick,
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = ChatTheme.colors.backgroundElevationElevation1,
        ),
    )
}

@Composable
private fun DirectChannelInfoContent(
    state: ChannelInfoViewState,
    modifier: Modifier,
    listState: LazyListState,
    onViewAction: (action: ChannelInfoViewAction) -> Unit,
) {
    val isLoading = state is ChannelInfoViewState.Loading
    ContentBox(
        modifier = modifier,
        isLoading = isLoading,
    ) {
        val content = state as ChannelInfoViewState.Content
        val navigationOptions = content.options.filterNavigation()
        val actionOptions = content.options.filterActions()

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
            item {
                content.members.firstOrNull()?.user?.let { user ->
                    ChatTheme.componentFactory.DirectChannelInfoAvatarContainer(user)
                }
            }
            if (navigationOptions.isNotEmpty()) {
                item {
                    ChannelInfoSection {
                        navigationOptions.forEach { option ->
                            ChannelInfoOptionContent(
                                option = option,
                                isGroupChannel = false,
                                onViewAction = onViewAction,
                            )
                        }
                    }
                }
            }
            if (actionOptions.isNotEmpty()) {
                item {
                    ChannelInfoSection {
                        actionOptions.forEach { option ->
                            ChannelInfoOptionContent(
                                option = option,
                                isGroupChannel = false,
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
internal fun DirectChannelInfoAvatarContainer(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        ChatTheme.componentFactory.UserAvatar(
            modifier = Modifier.size(AvatarSize.ExtraExtraLarge),
            user = user,
            showIndicator = user.shouldShowOnlineIndicator(
                userPresence = ChatTheme.userPresence,
                currentUser = null,
            ),
            showBorder = false,
        )
        Text(
            text = user.name.takeIf(String::isNotBlank) ?: user.id,
            style = ChatTheme.typography.headingLarge,
            color = ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = user.getLastSeenText(LocalContext.current),
            style = ChatTheme.typography.metadataDefault,
            color = ChatTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Filters navigation options: Pinned Messages, Photos & Videos, Files.
 */
internal fun List<ChannelInfoViewState.Content.Option>.filterNavigation() = filter { option ->
    option is ChannelInfoViewState.Content.Option.PinnedMessages ||
        option is ChannelInfoViewState.Content.Option.MediaAttachments ||
        option is ChannelInfoViewState.Content.Option.FilesAttachments
}

/**
 * Filters action options: Mute, Hide, Leave, Delete.
 */
internal fun List<ChannelInfoViewState.Content.Option>.filterActions() = filter { option ->
    option is ChannelInfoViewState.Content.Option.MuteChannel ||
        option is ChannelInfoViewState.Content.Option.HideChannel ||
        option is ChannelInfoViewState.Content.Option.LeaveChannel ||
        option is ChannelInfoViewState.Content.Option.DeleteChannel
}

@Preview
@Composable
private fun DirectChannelInfoContentLoadingPreview() {
    ChatTheme {
        DirectChannelInfoLoading()
    }
}

@Composable
internal fun DirectChannelInfoLoading() {
    DirectChannelInfoScaffold(
        modifier = Modifier.fillMaxSize(),
        headerState = ChannelHeaderViewState.Loading,
        infoState = ChannelInfoViewState.Loading,
        onNavigationIconClick = {},
        onViewAction = {},
    )
}

@Preview
@Composable
private fun DirectChannelInfoContentPreview() {
    ChatTheme {
        DirectChannelInfoContent()
    }
}

@Composable
internal fun DirectChannelInfoContent() {
    val member = Member(user = PreviewUserData.user1.copy(lastActive = Date()))
    DirectChannelInfoScaffold(
        modifier = Modifier.fillMaxSize(),
        headerState = ChannelHeaderViewState.Content(
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Connected,
            channel = PreviewChannelData.channelWithImage,
        ),
        infoState = ChannelInfoViewState.Content(
            owner = member.user,
            members = ExpandableList(
                items = listOf(member),
            ),
            options = listOf(
                ChannelInfoViewState.Content.Option.PinnedMessages,
                ChannelInfoViewState.Content.Option.MediaAttachments,
                ChannelInfoViewState.Content.Option.FilesAttachments,
                ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                ChannelInfoViewState.Content.Option.DeleteChannel,
            ),
        ),
    )
}
