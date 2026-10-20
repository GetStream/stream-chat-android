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

package io.getstream.chat.android.compose.sample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.data.PredefinedUserCredentials
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.models.Channel
import io.getstream.log.StreamLog
import kotlinx.coroutines.launch

/**
 * Grouped channel list for the e2e tests: one tab per group, each backed by its own grouped [ChannelListViewModel].
 *
 * The user connects on tap, with the same test tag as the login screen, so a test can seed the
 * mock server after the app starts and before the grouped query runs.
 */
class GroupedChannelsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme(allowUIAutomationTest = true) {
                var isConnected by rememberSaveable { mutableStateOf(false) }
                if (isConnected) {
                    GroupedChannelsScreen(
                        onChannelClick = { channel ->
                            startActivity(MessagesActivity.createIntent(this, channelId = channel.cid))
                        },
                    )
                } else {
                    LoginButton(onClick = { connectUser(onSuccess = { isConnected = true }) })
                }
            }
        }
    }

    private fun connectUser(onSuccess: () -> Unit) {
        lifecycleScope.launch {
            ChatHelper.connectUser(
                userCredentials = PredefinedUserCredentials.availableUsers.first(),
                onSuccess = onSuccess,
                onError = { error -> logger.e { "[connectUser] failed: $error" } },
            )
        }
    }

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, GroupedChannelsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
    }
}

private val GroupKeys = listOf("all", "new", "current", "old")

// Sent explicitly, so the page size the e2e tests depend on doesn't follow the server default.
private const val GroupPageSize = 10

private val logger = StreamLog.getLogger("Chat:GroupedChannels")

@Composable
private fun LoginButton(onClick: () -> Unit) {
    Text(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground)
            .systemBarsPadding()
            .testTag("Stream_UserLoginItem")
            .clickable(onClick = onClick)
            .padding(16.dp),
        text = "Login",
        color = ChatTheme.colors.textHighEmphasis,
    )
}

@Composable
private fun GroupedChannelsScreen(onChannelClick: (Channel) -> Unit) {
    val viewModels = GroupKeys.associateWith { group ->
        viewModel<ChannelListViewModel>(
            key = group,
            factory = viewModelFactory {
                initializer { ChannelListViewModel(chatClient = ChatClient.instance(), groupKey = group) }
            },
        )
    }
    // The grouped view models only observe their state, so one grouped query fills every tab.
    LaunchedEffect(Unit) {
        ChatClient.instance().queryGroupedChannels(groups = GroupKeys, limit = GroupPageSize).enqueue { result ->
            result.onError { error -> logger.e { "[queryGroupedChannels] failed: $error" } }
        }
    }
    var selectedGroup by rememberSaveable { mutableStateOf(GroupKeys.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground)
            .systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            GroupKeys.forEach { group ->
                GroupTab(
                    group = group,
                    isSelected = group == selectedGroup,
                    onClick = { selectedGroup = group },
                )
            }
        }
        key(selectedGroup) {
            ChannelList(
                modifier = Modifier.weight(1f),
                viewModel = viewModels.getValue(selectedGroup),
                onChannelClick = onChannelClick,
            )
        }
    }
}

@Composable
private fun GroupTab(
    group: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .testTag("Stream_GroupTab_$group")
            .clickable(onClick = onClick)
            .padding(12.dp),
        text = group,
        color = ChatTheme.colors.textHighEmphasis,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
    )
}
