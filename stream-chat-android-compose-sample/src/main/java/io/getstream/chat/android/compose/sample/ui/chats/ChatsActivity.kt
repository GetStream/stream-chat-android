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
import androidx.activity.compose.setContent
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.isAnonymousChannel
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.feature.channel.ChannelConstants.CHANNEL_ARG_DRAFT
import io.getstream.chat.android.compose.sample.feature.channel.add.AddChannelActivity
import io.getstream.chat.android.compose.sample.feature.channel.list.CustomChatEventHandlerFactory
import io.getstream.chat.android.compose.sample.ui.BaseConnectedActivity
import io.getstream.chat.android.compose.sample.ui.channel.ChannelInfoScreen
import io.getstream.chat.android.compose.sample.ui.channel.ChannelInfoViewModel
import io.getstream.chat.android.compose.sample.ui.channel.ChannelInfoViewModelFactory
import io.getstream.chat.android.compose.sample.ui.channel.DefaultChannelInfoNavigationIcon
import io.getstream.chat.android.compose.sample.ui.channel.GroupChannelInfoScreen
import io.getstream.chat.android.compose.sample.ui.channel.GroupChannelInfoViewModel
import io.getstream.chat.android.compose.sample.ui.channel.GroupChannelInfoViewModelFactory
import io.getstream.chat.android.compose.sample.ui.component.AppBottomBar
import io.getstream.chat.android.compose.sample.ui.component.AppBottomBarOption
import io.getstream.chat.android.compose.sample.ui.component.CustomChatComponentFactory
import io.getstream.chat.android.compose.sample.ui.login.UserLoginActivity
import io.getstream.chat.android.compose.sample.ui.pinned.PinnedMessagesActivity
import io.getstream.chat.android.compose.ui.channels.SearchMode
import io.getstream.chat.android.compose.ui.chats.ChatsScreen
import io.getstream.chat.android.compose.ui.chats.ExtraContentMode
import io.getstream.chat.android.compose.ui.chats.ListContentMode
import io.getstream.chat.android.compose.ui.chats.rememberExtraContentMode
import io.getstream.chat.android.compose.ui.components.channels.ChannelOptionItemVisibility
import io.getstream.chat.android.compose.ui.theme.ChannelOptionsTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.AdaptiveLayoutInfo
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.state.extensions.globalState
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
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
        var listContentMode by rememberSaveable { mutableStateOf(ListContentMode.Channels) }
        var extraContentMode by rememberExtraContentMode()
        ChatsScreen(
            channelViewModelFactory = channelViewModelFactory,
            messagesViewModelFactoryProvider = { _, (channelId, messageId, parentMessageId) ->
                // TODO Reset extra content mode when switching channels
                // extraContentMode = ExtraContentMode.Hidden // Reset extra content mode when switching channels
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
            extraContentMode = extraContentMode,
            onBackPress = ::finish,
            onListTopBarAvatarClick = {
                lifecycleScope.launch {
                    ChatHelper.disconnectUser()
                    openUserLogin()
                }
            },
            onListTopBarActionClick = ::openAddChannel,
            onDetailTopBarTitleClick = { channel ->
                extraContentMode = if (channel.isGroupChannel) {
                    ExtraContentMode.GroupChannelInfo(channel.cid)
                } else {
                    ExtraContentMode.SingleChannelInfo(channel.cid)
                }
            },
            onViewChannelInfoAction = { channel ->
                extraContentMode = if (channel.isGroupChannel) {
                    ExtraContentMode.GroupChannelInfo(channel.cid)
                } else {
                    ExtraContentMode.SingleChannelInfo(channel.cid)
                }
            },
            listBottomBarContent = {
                ListFooterContent { option ->
                    listContentMode = when (option) {
                        AppBottomBarOption.CHATS -> ListContentMode.Channels
                        AppBottomBarOption.THREADS -> ListContentMode.Threads
                    }
                }
            },
            extraContent = { mode ->
                when (mode) {
                    is ExtraContentMode.SingleChannelInfo -> SingleChannelInfoContent(
                        channelId = mode.id,
                        onNavigationIconClick = { extraContentMode = ExtraContentMode.Hidden },
                    )

                    is ExtraContentMode.GroupChannelInfo -> GroupChannelInfoContent(
                        channelId = mode.id,
                        onNavigationIconClick = { extraContentMode = ExtraContentMode.Hidden },
                    )

                    ExtraContentMode.Hidden -> Unit
                }
            },
        )
    }

    @Composable
    private fun ListFooterContent(onOptionSelected: (option: AppBottomBarOption) -> Unit) {
        val globalState = ChatClient.instance().globalState
        val unreadChannelsCount by globalState.channelUnreadCount.collectAsState()
        val unreadThreadsCount by globalState.unreadThreadsCount.collectAsState()
        var selectedOption by rememberSaveable { mutableStateOf(AppBottomBarOption.CHATS) }
        AppBottomBar(
            unreadChannelsCount = unreadChannelsCount,
            unreadThreadsCount = unreadThreadsCount,
            selectedOption = selectedOption,
            onOptionSelected = { option ->
                selectedOption = option
                onOptionSelected(option)
            },
        )
    }

    @Composable
    private fun SingleChannelInfoContent(
        channelId: String,
        onNavigationIconClick: () -> Unit,
    ) {
        val viewModel = viewModel(
            ChannelInfoViewModel::class.java,
            key = channelId,
            factory = ChannelInfoViewModelFactory(channelId),
        )
        val state by viewModel.state.collectAsState()
        ChannelInfoScreen(
            state = state,
            onPinnedMessagesClick = { openPinnedMessages(channelId) },
            onConfirmDelete = viewModel::onDeleteChannel,
            navigationIcon = {
                if (AdaptiveLayoutInfo.singlePaneWindow()) {
                    DefaultChannelInfoNavigationIcon(onClick = onNavigationIconClick)
                } else {
                    CloseButton(onClick = onNavigationIconClick)
                }
            },
        )
    }

    @Composable
    private fun GroupChannelInfoContent(
        channelId: String,
        onNavigationIconClick: () -> Unit,
    ) {
        val viewModel = viewModel(
            GroupChannelInfoViewModel::class.java,
            key = channelId,
            factory = GroupChannelInfoViewModelFactory(channelId),
        )
        val state by viewModel.state.collectAsState()
        GroupChannelInfoScreen(
            state = state,
            onNavigationIconClick = onNavigationIconClick,
            onPinnedMessagesClick = { openPinnedMessages(channelId) },
            navigationIcon = if (AdaptiveLayoutInfo.singlePaneWindow()) {
                null
            } else {
                { CloseButton(onClick = onNavigationIconClick) }
            },
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

    private fun openPinnedMessages(channelId: String) {
        startActivity(PinnedMessagesActivity.createIntent(applicationContext, channelId))
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

private val Channel.isGroupChannel: Boolean
    get() = memberCount > 2 || !isAnonymousChannel()
