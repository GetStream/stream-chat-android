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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.state.OnlineIndicatorAlignment
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.components.avatar.DefaultOnlineIndicator
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
        containerColor = ChatTheme.colors.barsBackground,
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
    TopAppBar(
        title = {},
        navigationIcon = {
            ChannelInfoNavigationIcon(
                onClick = onNavigationIconClick,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ChatTheme.colors.barsBackground),
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
        Column(
            modifier = Modifier.matchParentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content.members.firstOrNull()?.user?.let { user ->
                ChatTheme.componentFactory.DirectChannelInfoAvatarContainer(user)
            }
            LazyColumn(state = listState) {
                items(content.options) { option ->
                    with(ChatTheme.componentFactory) {
                        ChannelInfoOptionItem(
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

@Composable
internal fun DirectChannelInfoAvatarContainer(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ChatTheme.componentFactory.UserAvatar(
            modifier = Modifier.size(72.dp),
            user = user,
            textStyle = ChatTheme.typography.title3Bold,
            showOnlineIndicator = user.shouldShowOnlineIndicator(
                userPresence = ChatTheme.userPresence,
                currentUser = null,
            ),
            onlineIndicator = { DefaultOnlineIndicator(onlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd) },
            onClick = null,
        )
        Text(
            text = user.name.takeIf(String::isNotBlank) ?: user.id,
            style = ChatTheme.typography.title3Bold,
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
                ChannelInfoViewState.Content.Option.UserInfo(user = member.user),
                ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                ChannelInfoViewState.Content.Option.PinnedMessages,
                ChannelInfoViewState.Content.Option.Separator,
                ChannelInfoViewState.Content.Option.DeleteChannel,
            ),
        ),
    )
}
