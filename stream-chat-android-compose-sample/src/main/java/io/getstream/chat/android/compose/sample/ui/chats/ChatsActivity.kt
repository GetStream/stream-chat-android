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

package io.getstream.chat.android.compose.sample.ui.chats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
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
import io.getstream.chat.android.compose.ui.components.BackButton
import io.getstream.chat.android.compose.ui.components.channels.ChannelOptionItemVisibility
import io.getstream.chat.android.compose.ui.theme.ChannelOptionsTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.adaptivelayout.AdaptiveLayoutInfo
import io.getstream.chat.android.compose.ui.util.adaptivelayout.ThreePaneDestination
import io.getstream.chat.android.compose.ui.util.adaptivelayout.ThreePaneNavigator
import io.getstream.chat.android.compose.ui.util.adaptivelayout.ThreePaneRole
import io.getstream.chat.android.compose.ui.util.adaptivelayout.rememberThreePaneNavigator
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoViewModelFactory
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.compose.viewmodel.pinned.PinnedMessageListViewModel
import io.getstream.chat.android.compose.viewmodel.pinned.PinnedMessageListViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.state.extensions.globalState
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
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
            )

            is InfoContentMode.PinnedMessages -> PinnedMessagesContent(
                channelId = mode.channelId,
                onNavigationIconClick = { navigator.navigateBack() },
                onMessageClick = { message ->
                    navigator.navigateToMessage(
                        channelId = message.cid,
                        messageId = message.id,
                        replace = !singlePane,
                        popUp = singlePane,
                    )
                },
            )

            is InfoContentMode.Hidden -> Unit
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DirectChannelInfoContent(
        channelId: String,
        onNavigationIconClick: () -> Unit,
        onNavigateUp: () -> Unit,
        onNavigateToPinnedMessages: () -> Unit,
    ) {
        val viewModelFactory = ChannelInfoViewModelFactory(context = applicationContext, cid = channelId)
        val viewModel = viewModel<ChannelInfoViewModel>(key = channelId, factory = viewModelFactory)

        viewModel.handleChannelInfoEvents(onNavigateUp, onNavigateToPinnedMessages)

        if (AdaptiveLayoutInfo.singlePaneWindow()) {
            DirectChannelInfoScreen(
                viewModelFactory = viewModelFactory,
                viewModelKey = channelId,
                onNavigationIconClick = onNavigationIconClick,
            )
        } else {
            DirectChannelInfoScreen(
                viewModelFactory = viewModelFactory,
                viewModelKey = channelId,
                onNavigationIconClick = onNavigationIconClick,
                topBar = {
                    TopAppBar(
                        navigationIcon = { CloseButton(onClick = onNavigationIconClick) },
                        title = {},
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = ChatTheme.colors.barsBackground),
                    )
                },
            )
        }
    }

    @Composable
    private fun GroupChannelInfoContent(
        channelId: String,
        onNavigationIconClick: () -> Unit,
        onNavigateUp: () -> Unit,
        onNavigateToPinnedMessages: () -> Unit,
    ) {
        val viewModelFactory = ChannelInfoViewModelFactory(context = applicationContext, cid = channelId)
        val viewModel = viewModel<ChannelInfoViewModel>(key = channelId, factory = viewModelFactory)

        viewModel.handleChannelInfoEvents(onNavigateUp, onNavigateToPinnedMessages)

        if (AdaptiveLayoutInfo.singlePaneWindow()) {
            GroupChannelInfoScreen(
                viewModelFactory = viewModelFactory,
                viewModelKey = channelId,
                onNavigationIconClick = onNavigationIconClick,
            )
        } else {
            GroupChannelInfoScreen(
                viewModelFactory = viewModelFactory,
                viewModelKey = channelId,
                topBar = { elevation ->
                    GroupChannelInfoTopBar(
                        elevation = elevation,
                        onNavigationIconClick = onNavigationIconClick,
                        navigationIcon = { CloseButton(onClick = onNavigationIconClick) },
                    )
                },
            )
        }
    }

    @Composable
    private fun ChannelInfoViewModel.handleChannelInfoEvents(
        onNavigateUp: () -> Unit,
        onNavigateToPinnedMessages: () -> Unit,
    ) {
        LaunchedEffect(this) {
            events.collectLatest { event ->
                when (event) {
                    is ChannelInfoViewEvent.NavigateUp -> onNavigateUp()
                    is ChannelInfoViewEvent.NavigateToPinnedMessages -> onNavigateToPinnedMessages()
                    is ChannelInfoViewEvent.Error -> showError(event)
                    else -> Unit
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun GroupChannelInfoTopBar(
        elevation: Dp,
        onNavigationIconClick: () -> Unit,
        navigationIcon: @Composable () -> Unit = {
            BackButton(
                modifier = Modifier.mirrorRtl(layoutDirection = LocalLayoutDirection.current),
                painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
                onBackPressed = onNavigationIconClick,
            )
        },
    ) {
        Surface(shadowElevation = elevation.takeIf { it > 1.dp } ?: 0.dp) {
            TopAppBar(
                title = {},
                navigationIcon = navigationIcon,
                expandedHeight = 56.dp,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChatTheme.colors.barsBackground),
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
    replace: Boolean,
    popUp: Boolean,
) {
    navigateTo(
        destination = ThreePaneDestination(
            pane = ThreePaneRole.Detail,
            arguments = ChatMessageSelection(channelId, messageId),
        ),
        replace = replace,
        popUpTo = if (popUp) {
            ThreePaneRole.List
        } else {
            null
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
