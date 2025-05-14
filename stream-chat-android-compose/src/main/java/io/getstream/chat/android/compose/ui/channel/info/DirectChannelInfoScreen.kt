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
import androidx.compose.foundation.lazy.items
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
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastSeenText
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModelFactory
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
public fun DirectChannelInfoScreen(
    viewModelFactory: ChannelInfoViewModelFactory,
    modifier: Modifier = Modifier,
    viewModelKey: String? = null,
    onNavigationIconClick: () -> Unit = {},
    onPinnedMessagesClick: () -> Unit = {},
    topBar: @Composable () -> Unit = {
        DirectChannelInfoTopBar(
            onNavigationIconClick = onNavigationIconClick,
        )
    },
) {
    val viewModel = viewModel<ChannelInfoViewModel>(key = viewModelKey, factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    DirectChannelInfoContent(
        state = state,
        modifier = modifier,
        onNavigationIconClick = onNavigationIconClick,
        onPinnedMessagesClick = onPinnedMessagesClick,
        onViewAction = viewModel::onViewAction,
        topBar = topBar,
    )

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
        isGroupChannel = false,
        onViewAction = viewModel::onViewAction,
        onDismiss = { modal = null },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectChannelInfoTopBar(
    onNavigationIconClick: () -> Unit,
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            DefaultChannelInfoScreenNavigationIcon(
                onClick = onNavigationIconClick,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ChatTheme.colors.barsBackground),
    )
}

@Suppress("LongMethod")
@Composable
private fun DirectChannelInfoContent(
    state: ChannelInfoViewState,
    modifier: Modifier = Modifier,
    onNavigationIconClick: () -> Unit = {},
    onPinnedMessagesClick: () -> Unit = {},
    onViewAction: (action: ChannelInfoViewAction) -> Unit = {},
    topBar: @Composable () -> Unit = {
        DirectChannelInfoTopBar(
            onNavigationIconClick = onNavigationIconClick,
        )
    },
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        containerColor = ChatTheme.colors.barsBackground,
    ) { padding ->
        val isLoading = state is ChannelInfoViewState.Loading
        ContentBox(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = if (isLoading) Alignment.Center else Alignment.TopCenter,
            isLoading = isLoading,
        ) {
            val content = state as ChannelInfoViewState.Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val user = content.members.first().user
                UserAvatar(
                    modifier = Modifier.size(72.dp),
                    user = user,
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
                LazyColumn {
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
    }
}

@Preview
@Composable
private fun DirectChannelInfoContentLoadingPreview() {
    ChatTheme {
        DirectChannelInfoContent(
            state = ChannelInfoViewState.Loading,
        )
    }
}

@Preview
@Composable
private fun DirectChannelInfoContentPreview() {
    ChatTheme {
        DirectChannelInfoContent(
            state = ChannelInfoViewState.Content(
                members = ExpandableList(
                    items = listOf(
                        ChannelInfoViewState.Content.Member(
                            user = PreviewUserData.user1.copy(lastActive = Date()),
                            role = ChannelInfoViewState.Content.Role.Owner,
                        ),
                    ),
                ),
                options = listOf(
                    ChannelInfoViewState.Content.Option.UserInfo(id = "userId"),
                    ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
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
