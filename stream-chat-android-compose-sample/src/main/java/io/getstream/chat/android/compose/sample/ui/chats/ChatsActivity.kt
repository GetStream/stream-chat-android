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

@file:OptIn(ExperimentalStreamChatApi::class)

package io.getstream.chat.android.compose.sample.ui.chats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.feature.channel.ChannelConstants.CHANNEL_ARG_DRAFT
import io.getstream.chat.android.compose.sample.feature.channel.add.AddChannelActivity
import io.getstream.chat.android.compose.sample.feature.channel.isGroupChannel
import io.getstream.chat.android.compose.sample.feature.channel.list.CustomChatEventHandlerFactory
import io.getstream.chat.android.compose.sample.ui.BaseConnectedActivity
import io.getstream.chat.android.compose.sample.ui.component.AppBottomBar
import io.getstream.chat.android.compose.sample.ui.component.AppBottomBarOption
import io.getstream.chat.android.compose.sample.ui.component.CustomChatComponentFactory
import io.getstream.chat.android.compose.sample.ui.login.UserLoginActivity
import io.getstream.chat.android.compose.sample.ui.pinned.PinnedMessagesScreen
import io.getstream.chat.android.compose.ui.channel.info.DirectChannelInfoScreen
import io.getstream.chat.android.compose.ui.channel.info.GroupChannelInfoScreen
import io.getstream.chat.android.compose.ui.channels.SearchMode
import io.getstream.chat.android.compose.ui.chats.ChatListContentMode
import io.getstream.chat.android.compose.ui.chats.ChatMessageSelection
import io.getstream.chat.android.compose.ui.chats.ChatsScreen
import io.getstream.chat.android.compose.ui.components.channels.ChannelOptionItemVisibility
import io.getstream.chat.android.compose.ui.theme.ChannelOptionsTheme
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.CompoundComponentFactory
import io.getstream.chat.android.compose.ui.util.adaptivelayout.AdaptiveLayoutInfo
import io.getstream.chat.android.compose.ui.util.adaptivelayout.ThreePaneDestination
import io.getstream.chat.android.compose.ui.util.adaptivelayout.ThreePaneNavigator
import io.getstream.chat.android.compose.ui.util.adaptivelayout.ThreePaneRole
import io.getstream.chat.android.compose.ui.util.adaptivelayout.rememberThreePaneNavigator
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModelFactory
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.compose.viewmodel.pinned.PinnedMessageListViewModel
import io.getstream.chat.android.compose.viewmodel.pinned.PinnedMessageListViewModelFactory
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.state.extensions.globalState
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.common.state.messages.list.ChannelHeaderViewState
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatsActivity : BaseConnectedActivity() {

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"
        private const val KEY_MESSAGE_ID = "messageId"
        private const val KEY_PARENT_MESSAGE_ID = "parentMessageId"

        fun createIntent(
            context: Context,
            channelId: String? = null,
            messageId: String? = null,
            parentMessageId: String? = null,
        ): Intent =
            Intent(context, ChatsActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(KEY_CHANNEL_ID, channelId)
                .putExtra(KEY_MESSAGE_ID, messageId)
                .putExtra(KEY_PARENT_MESSAGE_ID, parentMessageId)
    }

    private val channelId by lazy { intent.getStringExtra(KEY_CHANNEL_ID) }
    private val messageId by lazy { intent.getStringExtra(KEY_MESSAGE_ID) }
    private val parentMessageId by lazy { intent.getStringExtra(KEY_PARENT_MESSAGE_ID) }

    private val channelViewModelFactory by lazy {
        val chatClient = ChatClient.instance()
        val currentUserId = chatClient.getCurrentUser()?.id ?: ""
        ChannelViewModelFactory(
            chatClient = chatClient,
            querySort = QuerySortByField
                .descByName<Channel>("pinned_at") // pinned channels first
                .desc("last_updated"), // then by last updated
            filters = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(currentUserId)),
                Filters.or(Filters.notExists(CHANNEL_ARG_DRAFT), Filters.eq(CHANNEL_ARG_DRAFT, false)),
            ),
            chatEventHandlerFactory = CustomChatEventHandlerFactory(),
        )
    }

    private val messagesViewModelFactory by lazy {
        channelId?.let { cid ->
            buildMessagesViewModelFactory(
                channelId = cid,
                messageId = messageId,
                parentMessageId = parentMessageId,
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTheme(
                dateFormatter = ChatApp.dateFormatter,
                autoTranslationEnabled = ChatApp.autoTranslationEnabled,
                allowUIAutomationTest = true,
                componentFactory = CustomChatComponentFactory(),
                channelOptionsTheme = ChannelOptionsTheme.defaultTheme(
                    optionVisibility = ChannelOptionItemVisibility(
                        isViewInfoVisible = AdaptiveLayoutInfo.singlePaneWindow(),
                        isPinChannelVisible = true,
                    ),
                ),
            ) {
                ScreenContent()
            }
        }
    }

    @Composable
    private fun ScreenContent() {
        var listContentMode by rememberSaveable { mutableStateOf(ChatListContentMode.Channels) }
        val navigator = rememberThreePaneNavigator()
        ChatsScreen(
            navigator = navigator,
            channelViewModelFactory = channelViewModelFactory,
            messagesViewModelFactoryProvider = { _, (channelId, messageId, parentMessageId) ->
                if (channelId == null) {
                    messagesViewModelFactory
                } else {
                    buildMessagesViewModelFactory(
                        channelId = channelId,
                        messageId = messageId,
                        parentMessageId = parentMessageId,
                    )
                }
            },
            title = stringResource(id = R.string.app_name),
            searchMode = SearchMode.Messages,
            listContentMode = listContentMode,
            onBackPress = ::finish,
            onListTopBarAvatarClick = {
                GlobalScope.launch {
                    openUserLogin()
                    // Give time for login activity to start before disconnecting the user,
                    // so we prevent showing disconnected states.
                    delay(UserLoginActivity.DELAY_BEFORE_LOGOUT_IN_MILLIS)
                    ChatHelper.disconnectUser()
                }
            },
            onListTopBarActionClick = ::openAddChannel,
            onDetailTopBarTitleClick = navigator::navigateToChannelInfo,
            onViewChannelInfoClick = navigator::navigateToChannelInfo,
            listBottomBarContent = {
                ListFooterContent(
                    listContentMode = listContentMode,
                    onOptionSelected = { option ->
                        listContentMode = when (option) {
                            AppBottomBarOption.CHATS -> ChatListContentMode.Channels
                            AppBottomBarOption.MENTIONS -> ChatListContentMode.Mentions
                            AppBottomBarOption.THREADS -> ChatListContentMode.Threads
                        }
                    },
                )
            },
            infoContent = { arguments ->
                InfoContent(
                    navigator = navigator,
                    mode = arguments as InfoContentMode,
                )
            },
        )
    }

    @Composable
    private fun ListFooterContent(
        listContentMode: ChatListContentMode,
        onOptionSelected: (option: AppBottomBarOption) -> Unit,
    ) {
        val globalState = ChatClient.instance().globalState
        val unreadChannelsCount by globalState.channelUnreadCount.collectAsStateWithLifecycle()
        val unreadThreadsCount by globalState.unreadThreadsCount.collectAsStateWithLifecycle()
        val selectedOption = when (listContentMode) {
            ChatListContentMode.Channels -> AppBottomBarOption.CHATS
            ChatListContentMode.Mentions -> AppBottomBarOption.MENTIONS
            ChatListContentMode.Threads -> AppBottomBarOption.THREADS
        }
        AppBottomBar(
            unreadChannelsCount = unreadChannelsCount,
            unreadThreadsCount = unreadThreadsCount,
            selectedOption = selectedOption,
            onOptionSelected = onOptionSelected,
        )
    }

    @Composable
    private fun InfoContent(
        navigator: ThreePaneNavigator,
        mode: InfoContentMode,
    ) {
        val singlePane = AdaptiveLayoutInfo.singlePaneWindow()
        BackHandler(enabled = navigator.canNavigateBack()) { navigator.navigateBack() }

        when (mode) {
            is InfoContentMode.DirectChannelInfo -> DirectChannelInfoContent(
                channelId = mode.channelId,
                onNavigationIconClick = { navigator.navigateBack() },
                onNavigateUp = { navigator.popUpTo(pane = ThreePaneRole.List) },
                onNavigateToPinnedMessages = { navigator.navigateToPinnedMessages(mode.channelId) },
            )

            is InfoContentMode.GroupChannelInfo -> GroupChannelInfoContent(
                channelId = mode.channelId,
                onNavigationIconClick = { navigator.navigateBack() },
                onNavigateUp = { navigator.popUpTo(pane = ThreePaneRole.List) },
                onNavigateToPinnedMessages = { navigator.navigateToPinnedMessages(mode.channelId) },
                onNavigateToChannel = { channelId ->
                    navigator.navigateToChannel(
                        channelId = channelId,
                        singlePane = singlePane,
                    )
                },
            )

            is InfoContentMode.PinnedMessages -> PinnedMessagesContent(
                channelId = mode.channelId,
                onNavigationIconClick = { navigator.navigateBack() },
                onMessageClick = { message ->
                    navigator.navigateToMessage(
                        channelId = message.cid,
                        messageId = message.id,
                        singlePane = singlePane,
                    )
                },
            )

            is InfoContentMode.Hidden -> Unit
        }
    }

    @Composable
    private fun DirectChannelInfoContent(
        channelId: String,
        onNavigationIconClick: () -> Unit,
        onNavigateUp: () -> Unit,
        onNavigateToPinnedMessages: () -> Unit,
    ) {
        val viewModelFactory = remember(channelId) {
            ChannelInfoViewModelFactory(context = applicationContext, cid = channelId)
        }
        val viewModel = viewModel<ChannelInfoViewModel>(factory = viewModelFactory)

        viewModel.OnChannelInfoEvents(onNavigateUp, onNavigateToPinnedMessages)

        if (AdaptiveLayoutInfo.singlePaneWindow()) {
            DirectChannelInfoScreen(
                viewModelFactory = viewModelFactory,
                onNavigationIconClick = onNavigationIconClick,
            )
        } else {
            CompoundComponentFactory(
                factory = {
                    object : ChatComponentFactory by it {
                        @OptIn(ExperimentalMaterial3Api::class)
                        @Composable
                        override fun DirectChannelInfoTopBar(
                            headerState: ChannelHeaderViewState,
                            listState: LazyListState,
                            onNavigationIconClick: () -> Unit,
                        ) {
                            TopAppBar(
                                title = {},
                                navigationIcon = { CloseButton(onClick = onNavigationIconClick) },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = ChatTheme.colors.barsBackground,
                                ),
                            )
                        }
                    }
                },
            ) {
                DirectChannelInfoScreen(
                    viewModelFactory = viewModelFactory,
                    onNavigationIconClick = onNavigationIconClick,
                )
            }
        }
    }

    @Composable
    private fun GroupChannelInfoContent(
        channelId: String,
        onNavigationIconClick: () -> Unit,
        onNavigateUp: () -> Unit,
        onNavigateToPinnedMessages: () -> Unit,
        onNavigateToChannel: (cid: String) -> Unit,
    ) {
        val viewModelFactory = remember(channelId) {
            ChannelInfoViewModelFactory(context = applicationContext, cid = channelId)
        }
        val viewModel = viewModel<ChannelInfoViewModel>(factory = viewModelFactory)

        viewModel.OnChannelInfoEvents(
            onNavigateUp = onNavigateUp,
            onNavigateToPinnedMessages = onNavigateToPinnedMessages,
            onNavigateToChannel = onNavigateToChannel,
        )

        if (AdaptiveLayoutInfo.singlePaneWindow()) {
            GroupChannelInfoScreen(
                viewModelFactory = viewModelFactory,
                onNavigationIconClick = onNavigationIconClick,
            )
        } else {
            CompoundComponentFactory(
                factory = {
                    object : ChatComponentFactory by it {
                        @Composable
                        override fun GroupChannelInfoTopBar(
                            headerState: ChannelHeaderViewState,
                            infoState: ChannelInfoViewState,
                            listState: LazyListState,
                            onNavigationIconClick: () -> Unit,
                            onAddMembersClick: () -> Unit,
                        ) {
                            GroupChannelInfoTopBar(
                                headerState = headerState,
                                infoState = infoState,
                                listState = listState,
                                navigationIcon = { CloseButton(onClick = onNavigationIconClick) },
                                onAddMembersClick = onAddMembersClick,
                            )
                        }
                    }
                },
            ) {
                GroupChannelInfoScreen(
                    viewModelFactory = viewModelFactory,
                    onNavigationIconClick = onNavigationIconClick,
                )
            }
        }
    }

    @Composable
    private fun ChannelInfoViewModel.OnChannelInfoEvents(
        onNavigateUp: () -> Unit,
        onNavigateToPinnedMessages: () -> Unit,
        onNavigateToChannel: (cid: String) -> Unit = {},
    ) {
        LaunchedEffect(this) {
            events.collectLatest { event ->
                when (event) {
                    is ChannelInfoViewEvent.Navigation -> when (event) {
                        is ChannelInfoViewEvent.NavigateUp -> onNavigateUp()
                        is ChannelInfoViewEvent.NavigateToPinnedMessages -> onNavigateToPinnedMessages()
                        is ChannelInfoViewEvent.NavigateToChannel -> onNavigateToChannel(event.cid)
                        // https://linear.app/stream/issue/AND-582/compose-support-draft-messages-in-chatsactivity
                        is ChannelInfoViewEvent.NavigateToDraftChannel -> Unit
                    }

                    is ChannelInfoViewEvent.Error -> showError(event)
                    is ChannelInfoViewEvent.Modal -> Unit
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun GroupChannelInfoTopBar(
        headerState: ChannelHeaderViewState,
        infoState: ChannelInfoViewState,
        listState: LazyListState,
        navigationIcon: @Composable () -> Unit,
        onAddMembersClick: () -> Unit,
    ) {
        val elevation by animateDpAsState(
            targetValue = if (listState.canScrollBackward) {
                ChatTheme.dimens.headerElevation
            } else {
                0.dp
            },
        )
        val channelName = when (headerState) {
            is ChannelHeaderViewState.Loading -> ""
            is ChannelHeaderViewState.Content -> headerState.channel.name
        }

        Surface(shadowElevation = elevation) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.stream_ui_channel_info_group_title),
                            style = ChatTheme.typography.title3Bold,
                            color = ChatTheme.colors.textHighEmphasis,
                        )
                        Text(
                            text = channelName,
                            style = ChatTheme.typography.footnote,
                            color = ChatTheme.colors.textLowEmphasis,
                        )
                    }
                },
                navigationIcon = navigationIcon,
                expandedHeight = 56.dp,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChatTheme.colors.barsBackground),
                actions = {
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
    private fun PinnedMessagesContent(
        channelId: String,
        onNavigationIconClick: () -> Unit,
        onMessageClick: (message: Message) -> Unit,
    ) {
        val viewModel = viewModel<PinnedMessageListViewModel>(
            key = channelId,
            factory = PinnedMessageListViewModelFactory(channelId),
        )
        PinnedMessagesScreen(
            viewModel = viewModel,
            onNavigationIconClick = onNavigationIconClick,
            onMessageClick = onMessageClick,
        )
    }

    private fun buildMessagesViewModelFactory(
        channelId: String,
        messageId: String?,
        parentMessageId: String?,
    ) = MessagesViewModelFactory(
        context = applicationContext,
        channelId = channelId,
        messageId = messageId,
        parentMessageId = parentMessageId,
        autoTranslationEnabled = ChatApp.autoTranslationEnabled,
        deletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
        isComposerLinkPreviewEnabled = ChatApp.isComposerLinkPreviewEnabled,
    )

    private fun openAddChannel() {
        startActivity(Intent(applicationContext, AddChannelActivity::class.java))
    }

    private fun openUserLogin() {
        startActivity(UserLoginActivity.createIntent(applicationContext))
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_close),
            contentDescription = stringResource(id = R.string.stream_compose_cancel),
            tint = ChatTheme.colors.textHighEmphasis,
        )
    }
}

private fun ThreePaneNavigator.navigateToChannelInfo(channel: Channel) {
    navigateTo(
        destination = ThreePaneDestination(
            pane = ThreePaneRole.Info,
            arguments = if (channel.isGroupChannel) {
                InfoContentMode.GroupChannelInfo(channel.cid)
            } else {
                InfoContentMode.DirectChannelInfo(channel.cid)
            },
        ),
    )
}

private fun ThreePaneNavigator.navigateToPinnedMessages(channelId: String) {
    navigateTo(
        destination = ThreePaneDestination(
            pane = ThreePaneRole.Info,
            arguments = InfoContentMode.PinnedMessages(channelId),
        ),
    )
}

private fun ThreePaneNavigator.navigateToMessage(
    channelId: String,
    messageId: String,
    singlePane: Boolean,
) {
    navigateTo(
        destination = ThreePaneDestination(
            pane = ThreePaneRole.Detail,
            arguments = ChatMessageSelection(channelId, messageId),
        ),
        replace = !singlePane,
        popUpTo = if (singlePane) {
            ThreePaneRole.List
        } else {
            null
        },
    )
}

private fun ThreePaneNavigator.navigateToChannel(
    channelId: String,
    singlePane: Boolean,
) {
    navigateTo(
        destination = ThreePaneDestination(
            pane = ThreePaneRole.Detail,
            arguments = ChatMessageSelection(channelId),
        ),
        replace = !singlePane,
        popUpTo = if (singlePane) {
            null
        } else {
            ThreePaneRole.Detail
        },
    )
}

private fun Context.showError(error: ChannelInfoViewEvent.Error) {
    val message = when (error) {
        ChannelInfoViewEvent.RenameChannelError,
        -> R.string.stream_ui_channel_info_rename_group_error

        ChannelInfoViewEvent.MuteChannelError,
        ChannelInfoViewEvent.UnmuteChannelError,
        -> R.string.stream_ui_channel_info_mute_conversation_error

        ChannelInfoViewEvent.HideChannelError,
        ChannelInfoViewEvent.UnhideChannelError,
        -> R.string.stream_ui_channel_info_hide_conversation_error

        ChannelInfoViewEvent.LeaveChannelError,
        -> R.string.stream_ui_channel_info_leave_conversation_error

        ChannelInfoViewEvent.DeleteChannelError,
        -> R.string.stream_ui_channel_info_delete_conversation_error

        ChannelInfoViewEvent.BanMemberError,
        -> R.string.stream_ui_channel_info_ban_member_error

        ChannelInfoViewEvent.UnbanMemberError,
        -> R.string.stream_ui_channel_info_unban_member_error

        ChannelInfoViewEvent.RemoveMemberError,
        -> R.string.stream_ui_channel_info_remove_member_error
    }
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}
