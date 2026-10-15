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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.state.globalStateFlow
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.data.PredefinedUserCredentials
import io.getstream.chat.android.compose.sample.ui.channel.ChannelActivity
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.GroupedChannelsGroupQuery
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.log.StreamLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * Grouped channel list for the e2e tests: one tab per group, each backed by its own grouped
 * [ChannelListViewModel], with the tab's unread channel count from `GlobalState.groupedUnreadChannels`.
 *
 * The user connects on tap, with the same test tag as the login screen, so a test can seed the
 * mock server after the app starts and before the grouped query runs.
 */
class GroupedChannelsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleChatTheme {
                var isConnected by rememberSaveable { mutableStateOf(false) }
                if (isConnected) {
                    GroupedChannelsScreen(
                        isFiltered = intent.getBooleanExtra(KEY_IS_FILTERED, false),
                        onChannelClick = { channel ->
                            startActivity(ChannelActivity.createIntent(this, channelId = channel.cid))
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
        private const val KEY_IS_FILTERED = "isFiltered"

        /**
         * @param isFiltered Whether the groups are queried with [GroupFilters] and the `query_timestamp` sort.
         */
        fun createIntent(context: Context, isFiltered: Boolean): Intent =
            Intent(context, GroupedChannelsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(KEY_IS_FILTERED, isFiltered)
            }
    }
}

private val GroupKeys = listOf("all", "new", "current", "old")

// Sent explicitly, so the page size the e2e tests depend on doesn't follow the server default.
private const val GroupPageSize = 10

private const val RecentCutoff = "now-7d"

// The all group shows the new channels and the recent current ones, the current group only its recent channels,
// the new group its whole stored group and the old group has no filter.
private val GroupFilters: Map<String, FilterObject?> = mapOf(
    "all" to Filters.or(
        Filters.and(Filters.eq("group", "current"), Filters.greaterThanEquals("query_timestamp", RecentCutoff)),
        Filters.`in`("group", listOf("new")),
    ),
    "new" to Filters.neutral(),
    "current" to Filters.greaterThanEquals("query_timestamp", RecentCutoff),
    "old" to null,
)

private val logger = StreamLog.getLogger("Chat:GroupedChannels")

@Composable
private fun LoginButton(onClick: () -> Unit) {
    Text(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.backgroundCoreApp)
            .systemBarsPadding()
            .testTag("Stream_UserLoginItem")
            .clickable(onClick = onClick)
            .padding(16.dp),
        text = "Login",
        color = ChatTheme.colors.textPrimary,
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun GroupedChannelsScreen(isFiltered: Boolean, onChannelClick: (Channel) -> Unit) {
    val viewModels = GroupKeys.associateWith { group ->
        viewModel<ChannelListViewModel>(key = group, factory = ChannelListViewModelFactory(groupKey = group))
    }
    // The grouped view models only observe their state, so one grouped query fills every tab.
    LaunchedEffect(Unit) {
        val call = if (isFiltered) {
            ChatClient.instance().queryGroupedChannels(
                groups = GroupKeys.associateWith { group -> GroupedChannelsGroupQuery(filter = GroupFilters[group]) },
                sort = QuerySortByField.descByName("query_timestamp"),
                limit = GroupPageSize,
            )
        } else {
            ChatClient.instance().queryGroupedChannels(groups = GroupKeys, limit = GroupPageSize)
        }
        call.enqueue { result ->
            result.onError { error -> logger.e { "[queryGroupedChannels] failed: $error" } }
        }
    }
    val unreadByGroup by remember {
        ChatClient.instance().globalStateFlow.flatMapLatest { it.groupedUnreadChannels }
    }.collectAsStateWithLifecycle(initialValue = emptyMap())
    var selectedGroup by rememberSaveable { mutableStateOf(GroupKeys.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.backgroundCoreApp)
            .systemBarsPadding(),
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.End)
                .testTag("Stream_MarkAllReadButton")
                .clickable { ChatClient.instance().markAllRead().enqueue() }
                .padding(16.dp),
            text = "Mark all read",
            color = ChatTheme.colors.accentPrimary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            GroupKeys.forEach { group ->
                GroupTab(
                    group = group,
                    isSelected = group == selectedGroup,
                    unreadChannels = unreadByGroup[group] ?: 0,
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
    unreadChannels: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .testTag("Stream_GroupTab_$group")
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = group,
            color = ChatTheme.colors.textPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
        if (unreadChannels > 0) {
            Text(
                modifier = Modifier
                    .testTag("Stream_GroupTabBadge_$group")
                    .padding(start = 4.dp)
                    .background(ChatTheme.colors.accentError, CircleShape)
                    .padding(horizontal = 6.dp),
                text = unreadChannels.toString(),
                color = ChatTheme.colors.textOnAccent,
            )
        }
    }
}
