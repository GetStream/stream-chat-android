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
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.feature.channel.ChannelConstants.CHANNEL_ARG_DRAFT
import io.getstream.chat.android.compose.sample.feature.channel.add.AddChannelActivity
import io.getstream.chat.android.compose.sample.feature.channel.list.CustomChatEventHandlerFactory
import io.getstream.chat.android.compose.sample.ui.BaseConnectedActivity
import io.getstream.chat.android.compose.sample.ui.MessagesActivity
import io.getstream.chat.android.compose.sample.ui.channel.ChannelInfoActivity
import io.getstream.chat.android.compose.sample.ui.component.AppBottomBar
import io.getstream.chat.android.compose.sample.ui.component.AppBottomBarOption
import io.getstream.chat.android.compose.sample.ui.login.UserLoginActivity
import io.getstream.chat.android.compose.ui.channels.SearchMode
import io.getstream.chat.android.compose.ui.chats.ChatsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.threads.ThreadList
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.threads.ThreadListViewModel
import io.getstream.chat.android.compose.viewmodel.threads.ThreadsViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.state.extensions.globalState
import kotlinx.coroutines.launch

class ChatsActivity : BaseConnectedActivity() {

    private val channelViewModelFactory by lazy {
        val chatClient = ChatClient.instance()
        val currentUserId = chatClient.getCurrentUser()?.id ?: ""
        ChannelViewModelFactory(
            chatClient = chatClient,
            querySort = QuerySortByField.descByName("last_updated"),
            filters = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(currentUserId)),
                Filters.or(Filters.notExists(CHANNEL_ARG_DRAFT), Filters.eq(CHANNEL_ARG_DRAFT, false)),
            ),
            chatEventHandlerFactory = CustomChatEventHandlerFactory(),
        )
    }
    private val threadsViewModelFactory by lazy { ThreadsViewModelFactory() }

    private val threadsViewModel: ThreadListViewModel by viewModels { threadsViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var selectedTab by rememberSaveable { mutableStateOf(AppBottomBarOption.CHATS) }
            val globalState = ChatClient.instance().globalState
            val unreadChannelsCount by globalState.channelUnreadCount.collectAsState()
            val unreadThreadsCount by globalState.unreadThreadsCount.collectAsState()
            var showBottomBar by remember { mutableStateOf(true) }

            ChatTheme(
                dateFormatter = ChatApp.dateFormatter,
                autoTranslationEnabled = ChatApp.autoTranslationEnabled,
                allowUIAutomationTest = true,
            ) {
                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            AppBottomBar(
                                unreadChannelsCount = unreadChannelsCount,
                                unreadThreadsCount = unreadThreadsCount,
                                selectedOption = selectedTab,
                                onOptionSelected = { selectedTab = it },
                            )
                        }
                    },
                    content = { scaffoldPadding ->
                        when (selectedTab) {
                            AppBottomBarOption.CHATS -> ChannelsContent(
                                modifier = Modifier.padding(scaffoldPadding),
                                onNavigateToMessages = { channelId, singlePanel ->
                                    showBottomBar = !singlePanel || channelId == null
                                },
                            )

                            AppBottomBarOption.THREADS -> ThreadsContent(
                                modifier = Modifier.padding(scaffoldPadding),
                            )
                        }
                    },
                )
            }
        }
    }

    @Composable
    private fun ChannelsContent(
        modifier: Modifier,
        onNavigateToMessages: (channelId: String?, singlePanel: Boolean) -> Unit,
    ) {
        ChatsScreen(
            modifier = modifier,
            channelViewModelFactory = channelViewModelFactory,
            title = stringResource(id = R.string.app_name),
            isShowingHeader = true,
            searchMode = SearchMode.Messages,
            onNavigateToMessages = onNavigateToMessages,
            onBackPressed = ::finish,
            onChannelsHeaderAvatarClick = {
                lifecycleScope.launch {
                    ChatHelper.disconnectUser()
                    openUserLogin()
                }
            },
            onChannelsHeaderActionClick = ::openAddChannel,
            onViewChannelInfoAction = ::openChannelInfo,
            onMessagesHeaderTitleClick = ::openChannelInfo,
        )
    }

    @Composable
    private fun ThreadsContent(modifier: Modifier) {
        ThreadList(
            modifier = modifier,
            viewModel = threadsViewModel,
            onThreadClick = ::openThread,
        )
    }

    private fun openThread(thread: Thread) {
        startActivity(
            MessagesActivity.createIntent(
                context = applicationContext,
                channelId = thread.parentMessage.cid,
                parentMessageId = thread.parentMessageId,
            ),
        )
    }

    private fun openAddChannel() {
        startActivity(Intent(applicationContext, AddChannelActivity::class.java))
    }

    private fun openUserLogin() {
        startActivity(UserLoginActivity.createIntent(applicationContext))
    }

    private fun openChannelInfo(channel: Channel) {
        startActivity(ChannelInfoActivity.createIntent(applicationContext, channel.cid))
    }

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, ChatsActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}
