/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.channel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.BaseConnectedActivity
import io.getstream.chat.android.compose.sample.ui.pinned.PinnedMessagesActivity
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

/**
 * Activity showing information about a group channel (chat).
 */
class GroupChannelInfoActivity : BaseConnectedActivity() {

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        /**
         * Creates an [Intent] for starting the [GroupChannelInfoActivity].
         *
         * @param context The calling [Context], used for building the [Intent].
         * @param channelId The ID of the channel for which the pinned messages are shown.
         */
        fun createIntent(context: Context, channelId: String) =
            Intent(context, GroupChannelInfoActivity::class.java)
                .putExtra(KEY_CHANNEL_ID, channelId)
    }

    private val messagesViewModelFactory by lazy {
        MessagesViewModelFactory(
            context = this,
            channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
            autoTranslationEnabled = ChatApp.autoTranslationEnabled,
            isComposerLinkPreviewEnabled = ChatApp.isComposerLinkPreviewEnabled,
        )
    }
    private val viewModelFactory by lazy {
        GroupChannelInfoViewModelFactory(
            cid = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
        )
    }
    private val viewModel by viewModels<GroupChannelInfoViewModel>(factoryProducer = { viewModelFactory })
    private val messageListViewModel by viewModels<MessageListViewModel>(factoryProducer = { messagesViewModelFactory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                val state by viewModel.state.collectAsState()
                GroupChannelInfoScreen(
                    state = state,
                    onBack = ::finish,
                    onPinnedMessagesClick = ::openPinnedMessages,
                )
            }
        }
    }

    private fun openPinnedMessages() {
        val intent = PinnedMessagesActivity.createIntent(
            context = this,
            channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
        )
        startActivity(intent)
    }

    @Composable
    private fun GroupChannelInfoScreen(
        state: GroupChannelInfoViewModel.State,
        onBack: () -> Unit,
        onPinnedMessagesClick: () -> Unit,
    ) {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            topBar = {
                GroupChannelHeader(onBack)
            },
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ChatTheme.colors.appBackground)
                        .padding(padding),
                ) {
                    LazyColumn {
                        // Members
                        items(state.members) { member ->
                            ChannelInfoMemberItem(member, createdBy = state.createdBy)
                            ChannelInfoContentDivider(height = 1.dp)
                        }
                        item {
                            ChannelInfoContentDivider(height = 8.dp)
                        }
                        // Pinned messages
                        item {
                            ChannelInfoOptionItem(
                                icon = R.drawable.stream_compose_ic_message_pinned,
                                text = stringResource(id = R.string.channel_info_option_pinned_messages),
                                onClick = onPinnedMessagesClick,
                            )
                            ChannelInfoContentDivider(height = 1.dp)
                        }
                    }
                }
            },
        )
    }

    @Composable
    private fun GroupChannelHeader(onBack: () -> Unit) {
        val user by messageListViewModel.user.collectAsState()
        val connectionState by messageListViewModel.connectionState.collectAsState()
        MessageListHeader(
            channel = messageListViewModel.channel,
            currentUser = user,
            connectionState = connectionState,
            onBackPressed = onBack,
            trailingContent = {
                Spacer(modifier = Modifier.width(44.dp))
            },
        )
    }
}
