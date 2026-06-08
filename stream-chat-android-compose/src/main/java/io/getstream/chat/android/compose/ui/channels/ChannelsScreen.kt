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

package io.getstream.chat.android.compose.ui.channels

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ChannelListAction
import io.getstream.chat.android.compose.state.channels.list.ChannelListEvent
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.components.channels.buildDefaultChannelActions
import io.getstream.chat.android.compose.ui.theme.ChannelListHeaderParams
import io.getstream.chat.android.compose.ui.theme.ChannelListSearchInputParams
import io.getstream.chat.android.compose.ui.theme.ChannelMenuParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.StreamSnackbarHost
import io.getstream.chat.android.compose.ui.util.StreamSnackbarVariant
import io.getstream.chat.android.compose.ui.util.StreamSnackbarVisuals
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo
import kotlinx.coroutines.launch

/**
 * Default root Channel screen component, that provides the necessary ViewModel.
 *
 * It can be used without most parameters for default behavior, that can be tweaked if necessary.
 * @param viewModelFactory The factory used to build the ViewModels and power the behavior.
 * You can use the default implementation by not passing in an instance yourself, or you
 * can customize the behavior using its parameters.
 * @param viewModelKey Key to differentiate between instances of [ChannelListViewModel].
 * @param title Header title. Also drives the screen's `paneTitle` semantic, announced by TalkBack
 * when the screen appears as a pane (e.g. an adaptive-layout pane or a Compose Navigation route).
 * @param isShowingHeader If we show the header or hide it.
 * @param searchMode The search mode for the screen.
 * @param onHeaderActionClick Handler for the default header action.
 * @param onHeaderAvatarClick Handle for when the user clicks on the header avatar.
 * @param onStartChatClick Handler for the "Start a chat" button in the empty state.
 * If null, the button is hidden. Defaults to null.
 * @param onChannelClick Handler for Channel item clicks.
 * @param onViewChannelInfoAction Handler for when the user selects the [ViewInfo] option for a [Channel].
 * @param onBackPressed Handler for back press action.
 * @param isBackPressEnabled Indicator if the default back handler is enabled. Set to `false` to fully disable the back
 * handling and catch the back-press event in a [BackHandler] higher in the compose hierarchy. Default: `true`.
 */
@Composable
@Suppress("LongMethod")
public fun ChannelsScreen(
    viewModelFactory: ChannelListViewModelFactory = ChannelListViewModelFactory(),
    viewModelKey: String? = null,
    title: String = "Chats",
    isShowingHeader: Boolean = true,
    searchMode: SearchMode = SearchMode.None,
    onHeaderActionClick: () -> Unit = {},
    onHeaderAvatarClick: () -> Unit = {},
    onStartChatClick: (() -> Unit)? = null,
    onChannelClick: (Channel) -> Unit = {},
    onSearchMessageItemClick: (Message) -> Unit = {},
    onViewChannelInfoAction: (Channel) -> Unit = {},
    onBackPressed: () -> Unit = {},
    isBackPressEnabled: Boolean = true,
) {
    val listViewModel: ChannelListViewModel = viewModel(
        ChannelListViewModel::class.java,
        key = viewModelKey,
        factory = viewModelFactory,
    )

    val selectedChannel by listViewModel.selectedChannel
    val user by listViewModel.user.collectAsState()
    val connectionState by listViewModel.connectionState.collectAsState()

    BackHandler(enabled = isBackPressEnabled) {
        if (selectedChannel != null) {
            listViewModel.selectChannel(null)
        } else {
            onBackPressed()
        }
    }

    var searchQuery by rememberSaveable { mutableStateOf(listViewModel.searchQuery.query) }

    val snackbarHostState = remember(::SnackbarHostState)
    EventHandler(listViewModel, snackbarHostState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("Stream_ChannelsScreen"),
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .semantics { paneTitle = title },
            snackbarHost = { StreamSnackbarHost(snackbarHostState) },
            topBar = {
                if (isShowingHeader) {
                    ChatTheme.componentFactory.ChannelListHeader(
                        params = ChannelListHeaderParams(
                            onHeaderActionClick = onHeaderActionClick,
                            onAvatarClick = { onHeaderAvatarClick() },
                            currentUser = user,
                            title = title,
                            connectionState = connectionState,
                        ),
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(color = ChatTheme.colors.backgroundCoreApp),
            ) {
                if (searchMode != SearchMode.None) {
                    ChatTheme.componentFactory.ChannelListSearchInput(
                        params = ChannelListSearchInputParams(
                            modifier = Modifier
                                .testTag("Stream_SearchInput")
                                .padding(
                                    top = StreamTokens.spacingMd,
                                    bottom = StreamTokens.spacingXs,
                                    start = StreamTokens.spacingMd,
                                    end = StreamTokens.spacingMd,
                                )
                                .fillMaxWidth(),
                            query = searchQuery,
                            onValueChange = remember(listViewModel, searchMode) {
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
                        ),
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
                    onStartChatClick = onStartChatClick,
                )
            }
        }

        if (selectedChannel != null) {
            val channel = selectedChannel!!
            val channelActions = buildDefaultChannelActions(
                selectedChannel = channel,
                ownCapabilities = channel.ownCapabilities,
                viewModel = listViewModel,
                onViewInfoAction = { ch ->
                    listViewModel.dismissChannelAction()
                    onViewChannelInfoAction(ch)
                },
            )

            ChatTheme.componentFactory.ChannelMenu(
                params = ChannelMenuParams(
                    selectedChannel = channel,
                    currentUser = user,
                    channelActions = channelActions,
                    onChannelOptionConfirm = remember(listViewModel) {
                        {
                                action: ChannelAction ->
                            listViewModel.executeOrConfirm(action)
                        }
                    },
                    onDismiss = remember(listViewModel) { { listViewModel.dismissChannelAction() } },
                ),
            )
        }

        val activeAction = listViewModel.activeChannelAction
        val popup = activeAction?.confirmationPopup

        if (popup != null) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = popup.title,
                message = popup.message,
                onPositiveAction = remember(listViewModel) { { listViewModel.confirmPendingAction() } },
                onDismiss = remember(listViewModel) { { listViewModel.dismissChannelAction() } },
            )
        }
    }
}

/**
 * Collects [ChannelListViewModel.events] and surfaces them as a snackbar: an error pill for a failed channel action,
 * and a confirmation pill when a channel is deleted.
 */
@Composable
private fun EventHandler(viewModel: ChannelListViewModel, snackbarHostState: SnackbarHostState) {
    val snackbarScope = rememberCoroutineScope()
    val resources = LocalResources.current

    fun showSnackbar(@StringRes resId: Int, variant: StreamSnackbarVariant) {
        snackbarScope.launch {
            snackbarHostState.showSnackbar(
                StreamSnackbarVisuals(
                    message = resources.getString(resId),
                    variant = variant,
                    duration = SnackbarDuration.Short,
                ),
            )
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ChannelListEvent.ActionError ->
                    showSnackbar(event.action.errorMessageResId(), StreamSnackbarVariant.Error)

                is ChannelListEvent.ChannelDeleted ->
                    showSnackbar(R.string.stream_compose_channel_list_channel_deleted, StreamSnackbarVariant.Default)
            }
        }
    }
}

/**
 * The snackbar message shown when [this] channel action fails.
 */
@StringRes
private fun ChannelListAction.errorMessageResId(): Int = when (this) {
    ChannelListAction.MuteChannel -> R.string.stream_compose_channel_list_action_error_mute_channel
    ChannelListAction.UnmuteChannel -> R.string.stream_compose_channel_list_action_error_unmute_channel
    ChannelListAction.PinChannel -> R.string.stream_compose_channel_list_action_error_pin_channel
    ChannelListAction.UnpinChannel -> R.string.stream_compose_channel_list_action_error_unpin_channel
    ChannelListAction.ArchiveChannel -> R.string.stream_compose_channel_list_action_error_archive_channel
    ChannelListAction.UnarchiveChannel -> R.string.stream_compose_channel_list_action_error_unarchive_channel
    ChannelListAction.DeleteChannel -> R.string.stream_compose_channel_list_action_error_delete_channel
    ChannelListAction.LeaveGroup -> R.string.stream_compose_channel_list_action_error_leave_group
    ChannelListAction.MuteUser -> R.string.stream_compose_channel_list_action_error_mute_user
    ChannelListAction.UnmuteUser -> R.string.stream_compose_channel_list_action_error_unmute_user
    ChannelListAction.BlockUser -> R.string.stream_compose_channel_list_action_error_block_user
    ChannelListAction.UnblockUser -> R.string.stream_compose_channel_list_action_error_unblock_user
}

/**
 * The types of search modes in the channel screen.
 */
public enum class SearchMode {
    /**
     * No search mode.
     */
    None,

    /**
     * Search for channels.
     */
    Channels,

    /**
     * Search for messages.
     */
    Messages,
}
