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

package io.getstream.chat.android.compose.sample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.login.UserLoginActivity
import io.getstream.chat.android.compose.state.channels.list.ChannelItemState
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeader
import io.getstream.chat.android.compose.ui.channels.info.SelectedChannelMenu
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.components.SearchInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import kotlinx.coroutines.launch

class ChannelsActivity : BaseConnectedActivity() {

    private val factory by lazy {
        ChannelViewModelFactory(
            ChatClient.instance(),
            QuerySortByField.descByName("last_updated"),
            null,
        )
    }

    private val listViewModel: ChannelListViewModel by viewModels { factory }

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
            ChatTheme(dateFormatter = ChatApp.dateFormatter) {
                ChannelsScreen(
                    viewModelFactory = factory,
                    title = stringResource(id = R.string.app_name),
                    isShowingHeader = true,
                    isShowingSearch = true,
                    onItemClick = ::openMessages,
                    onBackPressed = ::finish,
                    onHeaderAvatarClick = {
                        listViewModel.viewModelScope.launch {
                            ChatHelper.disconnectUser()
                            openUserLogin()
                        }
                    },
                )

//                MyCustomUiSimplified()
//                MyCustomUi()
            }
        }
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
                modifier = Modifier.fillMaxSize(),
                itemContent = {
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
    private fun CustomChannelListItem(channelItem: ChannelItemState, user: User?) {
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

        val user by listViewModel.user.collectAsState()
        val delegatedSelectedChannel by listViewModel.selectedChannel
        val connectionState by listViewModel.connectionState.collectAsState()

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
                        listViewModel.setSearchQuery(it)
                    },
                )

                ChannelList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = listViewModel,
                    onChannelClick = ::openMessages,
                    onChannelLongClick = { listViewModel.selectChannel(it) },
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
                    isMuted = listViewModel.isChannelMuted(selectedChannel.cid),
                    selectedChannel = selectedChannel,
                    currentUser = user,
                    onChannelOptionClick = { action -> listViewModel.performChannelAction(action) },
                    onDismiss = { listViewModel.dismissChannelAction() },
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

    private fun openUserLogin() {
        finish()
        startActivity(UserLoginActivity.createIntent(this))
        overridePendingTransition(0, 0)
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, ChannelsActivity::class.java)
        }
    }
}
