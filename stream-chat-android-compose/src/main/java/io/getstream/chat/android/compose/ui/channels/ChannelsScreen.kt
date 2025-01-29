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

package io.getstream.chat.android.compose.ui.channels

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.compose.ui.channels.info.SelectedChannelMenu
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.components.SearchInput
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.channels.actions.ArchiveChannel
import io.getstream.chat.android.ui.common.state.channels.actions.DeleteConversation
import io.getstream.chat.android.ui.common.state.channels.actions.LeaveGroup
import io.getstream.chat.android.ui.common.state.channels.actions.MuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.PinChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnarchiveChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnmuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnpinChannel
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo

/**
 * Default root Channel screen component, that provides the necessary ViewModel.
 *
 * It can be used without most parameters for default behavior, that can be tweaked if necessary.
 * @param viewModelFactory The factory used to build the ViewModels and power the behavior.
 * You can use the default implementation by not passing in an instance yourself, or you
 * can customize the behavior using its parameters.
 * @param viewModelKey Key to differentiate between instances of [ChannelListViewModel].
 * @param title Header title.
 * @param isShowingHeader If we show the header or hide it.
 * @param searchMode The search mode for the screen.
 * @param onHeaderActionClick Handler for the default header action.
 * @param onHeaderAvatarClick Handle for when the user clicks on the header avatar.
 * @param onChannelClick Handler for Channel item clicks.
 * @param onViewChannelInfoAction Handler for when the user selects the [ViewInfo] option for a [Channel].
 * @param onBackPressed Handler for back press action.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
@Suppress("LongMethod")
public fun ChannelsScreen(
    viewModelFactory: ChannelViewModelFactory = ChannelViewModelFactory(),
    viewModelKey: String? = null,
    title: String = "Stream Chat",
    isShowingHeader: Boolean = true,
    searchMode: SearchMode = SearchMode.None,
    onHeaderActionClick: () -> Unit = {},
    onHeaderAvatarClick: () -> Unit = {},
    onChannelClick: (Channel) -> Unit = {},
    onSearchMessageItemClick: (Message) -> Unit = {},
    onViewChannelInfoAction: (Channel) -> Unit = {},
    onBackPressed: () -> Unit = {},
) {
    val listViewModel: ChannelListViewModel = viewModel(
        ChannelListViewModel::class.java,
        key = viewModelKey,
        factory = viewModelFactory,
    )

    val selectedChannel by listViewModel.selectedChannel
    val user by listViewModel.user.collectAsState()
    val connectionState by listViewModel.connectionState.collectAsState()

    BackHandler(enabled = true) {
        if (selectedChannel != null) {
            listViewModel.selectChannel(null)
        } else {
            onBackPressed()
        }
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("Stream_ChannelsScreen"),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (isShowingHeader) {
                    ChatTheme.componentFactory.ChannelListHeader(
                        modifier = Modifier,
                        onHeaderActionClick = onHeaderActionClick,
                        onAvatarClick = { onHeaderAvatarClick() },
                        currentUser = user,
                        title = title,
                        connectionState = connectionState,
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(color = ChatTheme.colors.appBackground),
            ) {
                if (searchMode != SearchMode.None) {
                    SearchInput(
                        modifier = Modifier
                            .testTag("Stream_SearchInput")
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        query = searchQuery,
                        onSearchStarted = {},
                        onValueChange = remember(listViewModel) {
                            {
                                searchQuery = it
                                listViewModel.setSearchQuery(
                                    when {
                                        it.isBlank() -> SearchQuery.Empty
                                        searchMode == SearchMode.Channels -> SearchQuery.Channels(it)
                                        searchMode == SearchMode.Messages -> SearchQuery.Messages(it)
                                        else -> SearchQuery.Empty
                                    },
                                )
                            }
                        },
                    )
                }

                ChannelList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = listViewModel,
                    onChannelClick = onChannelClick,
                    onSearchResultClick = onSearchMessageItemClick,
                    onChannelLongClick = remember(listViewModel) {
                        {
                            listViewModel.selectChannel(it)
                        }
                    },
                )
            }
        }

        val channel = selectedChannel ?: Channel()
        AnimatedVisibility(
            visible = channel.cid.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)),
        ) {
            SelectedChannelMenu(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .animateEnterExit(
                        enter = slideInVertically(
                            initialOffsetY = { height -> height },
                            animationSpec = tween(),
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { height -> height },
                            animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2),
                        ),
                    ),
                selectedChannel = channel,
                currentUser = user,
                isMuted = listViewModel.isChannelMuted(channel.cid),
                onChannelOptionClick = remember(listViewModel) {
                    {
                            action ->
                        when (action) {
                            is ViewInfo -> onViewChannelInfoAction(action.channel)
                            is MuteChannel -> listViewModel.muteChannel(action.channel)
                            is UnmuteChannel -> listViewModel.unmuteChannel(action.channel)
                            is PinChannel -> listViewModel.pinChannel(action.channel)
                            is UnpinChannel -> listViewModel.unpinChannel(action.channel)
                            is ArchiveChannel -> listViewModel.archiveChannel(action.channel)
                            is UnarchiveChannel -> listViewModel.unarchiveChannel(action.channel)
                            else -> listViewModel.performChannelAction(action)
                        }
                    }
                },
                onDismiss = remember(listViewModel) { { listViewModel.dismissChannelAction() } },
            )
        }

        val activeAction = listViewModel.activeChannelAction

        if (activeAction is LeaveGroup) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = stringResource(
                    id = R.string.stream_compose_selected_channel_menu_leave_group_confirmation_title,
                ),
                message = stringResource(
                    id = R.string.stream_compose_selected_channel_menu_leave_group_confirmation_message,
                    ChatTheme.channelNameFormatter.formatChannelName(activeAction.channel, user),
                ),
                onPositiveAction = remember(listViewModel) { { listViewModel.leaveGroup(activeAction.channel) } },
                onDismiss = remember(listViewModel) { { listViewModel.dismissChannelAction() } },
            )
        } else if (activeAction is DeleteConversation) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = stringResource(
                    id = R.string.stream_compose_selected_channel_menu_delete_conversation_confirmation_title,
                ),
                message = stringResource(
                    id = R.string.stream_compose_selected_channel_menu_delete_conversation_confirmation_message,
                    ChatTheme.channelNameFormatter.formatChannelName(activeAction.channel, user),
                ),
                onPositiveAction =
                remember(listViewModel) { { listViewModel.deleteConversation(activeAction.channel) } },
                onDismiss = remember(listViewModel) { { listViewModel.dismissChannelAction() } },
            )
        }
    }
}

public enum class SearchMode {
    None,
    Channels,
    Messages,
}
