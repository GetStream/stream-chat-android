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

package io.getstream.chat.android.compose.sample.feature.channel.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.feature.channel.ChannelConstants.CHANNEL_ARG_DRAFT
import io.getstream.chat.android.compose.sample.feature.channel.add.AddChannelActivity
import io.getstream.chat.android.compose.sample.ui.BaseConnectedActivity
import io.getstream.chat.android.compose.sample.ui.MessagesActivity
import io.getstream.chat.android.compose.sample.ui.channel.ChannelInfoActivity
import io.getstream.chat.android.compose.sample.ui.component.AppBottomBar
import io.getstream.chat.android.compose.sample.ui.component.AppBottomBarOption
import io.getstream.chat.android.compose.sample.ui.component.CustomChatComponentFactory
import io.getstream.chat.android.compose.sample.ui.login.UserLoginActivity
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.channels.SearchMode
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeader
import io.getstream.chat.android.compose.ui.channels.info.SelectedChannelMenu
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.components.SearchInput
import io.getstream.chat.android.compose.ui.components.channels.ChannelOptionItemVisibility
import io.getstream.chat.android.compose.ui.mentions.MentionList
import io.getstream.chat.android.compose.ui.theme.ChannelOptionsTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.threads.ThreadList
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.mentions.MentionListViewModel
import io.getstream.chat.android.compose.viewmodel.mentions.MentionListViewModelFactory
import io.getstream.chat.android.compose.viewmodel.threads.ThreadListViewModel
import io.getstream.chat.android.compose.viewmodel.threads.ThreadsViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.state.extensions.globalState
import kotlinx.coroutines.launch

class ChannelsActivity : BaseConnectedActivity() {

    private val channelsViewModelFactory by lazy {
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
            isDraftMessageEnabled = true,
        )
    }

    private val channelsViewModel: ChannelListViewModel by viewModels { channelsViewModelFactory }
    private val mentionListViewModel: MentionListViewModel by viewModels { MentionListViewModelFactory() }
    private val threadsViewModel: ThreadListViewModel by viewModels { ThreadsViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * To use the Compose SDK/Components, simply call [setContent] to provide a Compose UI
         * definition, in which you gain access to all the UI component functions.
         *
         * You can use the default [ChannelsScreen] component that sets everything up for you,
         * or build a custom component yourself, like [MyCustomUi].
         */
        setContent {
            var selectedTab by rememberSaveable { mutableStateOf(AppBottomBarOption.CHATS) }
            val globalState = ChatClient.instance().globalState
            val unreadChannelsCount by globalState.channelUnreadCount.collectAsState()
            val unreadThreadsCount by globalState.unreadThreadsCount.collectAsState()

            ChatTheme(
                dateFormatter = ChatApp.dateFormatter,
                autoTranslationEnabled = ChatApp.autoTranslationEnabled,
                allowUIAutomationTest = true,
                componentFactory = CustomChatComponentFactory(),
                channelOptionsTheme = ChannelOptionsTheme.defaultTheme(
                    optionVisibility = ChannelOptionItemVisibility(
                        isPinChannelVisible = true,
                    ),
                ),
            ) {
                Scaffold(
                    modifier = Modifier.systemBarsPadding(),
                    bottomBar = {
                        AppBottomBar(
                            unreadChannelsCount = unreadChannelsCount,
                            unreadThreadsCount = unreadThreadsCount,
                            selectedOption = selectedTab,
                            onOptionSelected = { selectedTab = it },
                        )
                    },
                    containerColor = ChatTheme.colors.appBackground,
                ) { padding ->
                    Box(
                        modifier = Modifier.padding(padding),
                    ) {
                        when (selectedTab) {
                            AppBottomBarOption.CHATS -> ChannelsContent()
                            AppBottomBarOption.MENTIONS -> MentionsContent()
                            AppBottomBarOption.THREADS -> ThreadsContent()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ChannelsContent() {
        ChannelsScreen(
            viewModelFactory = channelsViewModelFactory,
            title = stringResource(id = R.string.app_name),
            isShowingHeader = true,
            searchMode = SearchMode.Messages,
            onChannelClick = ::openMessages,
            onSearchMessageItemClick = ::openMessages,
            onBackPressed = ::finish,
            onHeaderAvatarClick = {
                lifecycleScope.launch {
                    ChatHelper.disconnectUser()
                    openUserLogin()
                }
            },
            onHeaderActionClick = ::openAddChannel,
            onViewChannelInfoAction = ::viewChannelInfo,
        )
//                MyCustomUiSimplified()
//                MyCustomUi()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MentionsContent() {
        MentionList(
            viewModel = mentionListViewModel,
            modifier = Modifier.fillMaxSize(),
            onItemClick = ::openMessages,
            onEvent = { Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show() },
        )
    }

    @Composable
    private fun ThreadsContent() {
        ThreadList(
            viewModel = threadsViewModel,
            modifier = Modifier.fillMaxSize(),
            onThreadClick = ::openThread,
        )
    }

    /**
     * An example of a screen UI that's much more simple than the ChannelsScreen component, that features a custom
     * ChannelList item.
     */
    @Composable
    private fun MyCustomUiSimplified() {
        val user by ChatClient.instance().clientState.user.collectAsState()
        val connectionState by ChatClient.instance().clientState.connectionState.collectAsState()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                ChannelListHeader(
                    title = stringResource(id = R.string.app_name),
                    currentUser = user,
                    connectionState = connectionState,
                )
            },
        ) {
            ChannelList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                channelContent = {
                    CustomChannelListItem(channelItem = it, user = user)
                },
                divider = {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(0.5.dp)
                            .background(color = ChatTheme.colors.textLowEmphasis),
                    )
                },
            )
        }
    }

    /**
     * An example of a customized DefaultChannelItem component.
     */
    @Composable
    private fun CustomChannelListItem(channelItem: ItemState.ChannelItemState, user: User?) {
        ChannelItem(
            channelItem = channelItem,
            currentUser = user,
            onChannelLongClick = {},
            onChannelClick = {},
            trailingContent = {
                Spacer(modifier = Modifier.width(8.dp))
            },
            centerContent = {
                Text(
                    text = ChatTheme.channelNameFormatter.formatChannelName(it.channel, user),
                    style = ChatTheme.typography.bodyBold,
                    color = ChatTheme.colors.textHighEmphasis,
                )
            },
        )
    }

    /**
     * An example of what a custom UI can be, when not using [ChannelsScreen].
     *
     * It's important to note that if we want to use the [SelectedChannelMenu] to expose information and
     * options that the user can make with each channel, we need to use a [Box] and overlap the
     * two elements. This makes it easier as it's all presented in the same layer, rather than being
     * wrapped in drawers or more components.
     */
    @Composable
    private fun MyCustomUi() {
        var query by remember { mutableStateOf("") }

        val user by channelsViewModel.user.collectAsState()
        val delegatedSelectedChannel by channelsViewModel.selectedChannel
        val connectionState by channelsViewModel.connectionState.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                ChannelListHeader(
                    title = stringResource(id = R.string.app_name),
                    currentUser = user,
                    connectionState = connectionState,
                )

                SearchInput(
                    modifier = Modifier
                        .background(color = ChatTheme.colors.appBackground)
                        .fillMaxWidth()
                        .padding(8.dp),
                    query = query,
                    onValueChange = {
                        query = it
                        channelsViewModel.setSearchQuery(SearchQuery.Channels(it))
                    },
                )

                ChannelList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = channelsViewModel,
                    onChannelClick = ::openMessages,
                    onChannelLongClick = { channelsViewModel.selectChannel(it) },
                )
            }

            val selectedChannel = delegatedSelectedChannel
            if (selectedChannel != null) {
                SelectedChannelMenu(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.Center),
                    shape = RoundedCornerShape(16.dp),
                    isMuted = channelsViewModel.isChannelMuted(selectedChannel.cid),
                    selectedChannel = selectedChannel,
                    currentUser = user,
                    onChannelOptionClick = { action -> channelsViewModel.performChannelAction(action) },
                    onDismiss = { channelsViewModel.dismissChannelAction() },
                )
            }
        }
    }

    private fun openMessages(channel: Channel) {
        startActivity(
            MessagesActivity.createIntent(
                context = this,
                channelId = channel.cid,
                messageId = null,
                parentMessageId = null,
            ),
        )
    }

    private fun openMessages(message: Message) {
        startActivity(
            MessagesActivity.createIntent(
                context = this,
                channelId = message.cid,
                messageId = message.id,
                parentMessageId = message.parentId,
            ),
        )
    }

    private fun openThread(thread: Thread) {
        startActivity(
            MessagesActivity.createIntent(
                context = this,
                channelId = thread.parentMessage.cid,
                parentMessageId = thread.parentMessageId,
            ),
        )
    }

    private fun openAddChannel() {
        startActivity(Intent(this, AddChannelActivity::class.java))
    }

    private fun openUserLogin() {
        finish()
        startActivity(UserLoginActivity.createIntent(this))
        overridePendingTransition(0, 0)
    }

    private fun viewChannelInfo(channel: Channel) {
        startActivity(ChannelInfoActivity.createIntent(this, channel.cid))
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, ChannelsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }
}
