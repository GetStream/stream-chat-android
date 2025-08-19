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

package io.getstream.chat.android.compose.sample.ui.profile

import android.content.res.Resources
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.components.BackButton
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.models.UnreadChannel
import io.getstream.chat.android.models.UnreadChannelByType
import io.getstream.chat.android.models.UnreadCounts
import io.getstream.chat.android.models.UnreadThread
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigationIconClick: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = viewModel<UserProfileViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showUnreadCounts by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackButton(
                        modifier = Modifier.mirrorRtl(layoutDirection = LocalLayoutDirection.current),
                        painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
                        onBackPressed = onNavigationIconClick,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChatTheme.colors.appBackground,
                ),
            )
        },
        containerColor = ChatTheme.colors.appBackground,
    ) { paddingValues ->
        UserProfileScreenContent(
            modifier = Modifier.padding(paddingValues),
            state = state,
            onUnreadCountsClick = {
                showUnreadCounts = true
                viewModel.loadUnreadCounts()
            },
        )
    }
    if (showUnreadCounts) {
        ModalBottomSheet(
            onDismissRequest = { showUnreadCounts = false },
            containerColor = ChatTheme.colors.appBackground,
        ) {
            UnreadCounts(state.unreadCounts)
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.errors.collectLatest { error ->
            showUnreadCounts = false
            Toast.makeText(context, error.message, Toast.LENGTH_SHORT)
                .show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
private fun UserProfileScreenContent(
    state: UserProfileViewState,
    modifier: Modifier = Modifier,
    onUnreadCountsClick: () -> Unit = {},
) {
    when (val user = state.user) {
        null -> {
            LoadingIndicator(
                modifier = modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            )
        }

        else -> {
            Column(
                modifier = modifier
                    .fillMaxWidth(),
            ) {
                UserAvatar(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                        .size(72.dp),
                    user = user,
                    showOnlineIndicator = false,
                    onClick = null,
                )
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .padding(start = 16.dp),
                ) {
                    Text(
                        text = "Name",
                        style = ChatTheme.typography.title3,
                        color = ChatTheme.colors.textHighEmphasis,
                    )
                    Text(
                        text = user.name.takeIf(String::isNotBlank) ?: user.id,
                        style = ChatTheme.typography.body,
                        color = ChatTheme.colors.textLowEmphasis,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Divider()
                val avgResponseTimeInSeconds = user.avgResponseTime ?: 0
                if (avgResponseTimeInSeconds > 0) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(start = 16.dp),
                    ) {
                        Text(
                            text = "Average Response Time",
                            style = ChatTheme.typography.title3,
                            color = ChatTheme.colors.textHighEmphasis,
                        )
                        Text(
                            text = formatTime(
                                resources = LocalContext.current.resources,
                                seconds = avgResponseTimeInSeconds,
                            ),
                            style = ChatTheme.typography.body,
                            color = ChatTheme.colors.textLowEmphasis,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Divider()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = null,
                            indication = ripple(),
                            onClick = onUnreadCountsClick,
                        )
                        .padding(start = 16.dp)
                        .minimumInteractiveComponentSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Unread Counts",
                        style = ChatTheme.typography.title3,
                        color = ChatTheme.colors.textHighEmphasis,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = ChatTheme.colors.textLowEmphasis,
                    )
                }
            }
        }
    }
}

@Composable
private fun UnreadCounts(unreadCounts: UnreadCounts?) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        val counts = unreadCounts ?: UnreadCounts()
        val unreadCountByTeam = remember(counts.messagesCountByTeam) {
            counts.messagesCountByTeam.entries.toList()
        }
        val summaries = remember(
            counts.messagesCount,
            counts.messagesCount,
            counts.threadsCount,
        ) {
            mapOf(
                "Total Unread Messages" to counts.messagesCount,
                "Total Unread Channels" to counts.channels.size,
                "Total Unread Threads" to counts.threadsCount,
            ).entries.toList()
        }
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
        ) {
            summary(items = summaries)
            unreadChannels(items = counts.channels)
            unreadThreads(items = counts.threads)
            unreadChannelsByType(items = counts.channelsByType)
            unreadChannelsByTeam(items = unreadCountByTeam)
        }
        if (unreadCounts == null) {
            LoadingIndicator()
        }
    }
}

private fun LazyListScope.summary(
    items: List<Map.Entry<String, Int>>,
) {
    item {
        PaneTitle(
            text = "SUMMARY",
            padding = PaddingValues(
                start = 16.dp,
                bottom = 8.dp,
                end = 16.dp,
            ),
        )
    }
    itemsIndexed(
        items = items,
        key = { _, item -> item.key },
    ) { index, (title, count) ->
        PaneRow(
            index = index,
            lastIndex = items.lastIndex,
        ) {
            Text(
                text = title,
                style = ChatTheme.typography.body,
                color = ChatTheme.colors.textHighEmphasis,
            )
            Text(
                text = "$count",
                style = ChatTheme.typography.bodyBold,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }
    }
}

private fun LazyListScope.unreadChannels(
    items: List<UnreadChannel>,
) {
    item {
        PaneTitle(text = "UNREAD CHANNELS (${items.size})")
    }
    itemsIndexed(
        items = items,
        key = { _, item -> item.cid },
    ) { index, item ->
        PaneRow(
            index = index,
            lastIndex = items.lastIndex,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.cid,
                    style = ChatTheme.typography.bodyBold,
                    color = ChatTheme.colors.textHighEmphasis,
                )
                Text(
                    text = "Last read ${ChatTheme.dateFormatter.formatRelativeTime(item.lastRead)}",
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textLowEmphasis,
                )
            }
            CountText(
                count = item.messagesCount,
                backgroundColor = Color.Red.copy(alpha = 0.1f),
            )
        }
    }
}

private fun LazyListScope.unreadThreads(
    items: List<UnreadThread>,
) {
    item {
        PaneTitle(text = "UNREAD THREADS (${items.size})")
    }
    itemsIndexed(
        items = items,
        key = { _, item -> item.parentMessageId },
    ) { index, item ->
        PaneRow(
            index = index,
            lastIndex = items.lastIndex,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.parentMessageId,
                    style = ChatTheme.typography.bodyBold,
                    color = ChatTheme.colors.textHighEmphasis,
                )
                Text(
                    text = "Last read ${ChatTheme.dateFormatter.formatRelativeTime(item.lastRead)}",
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textLowEmphasis,
                )
                Text(
                    text = "Last read message: ${item.lastReadMessageId}",
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textLowEmphasis,
                )
            }
            CountText(
                count = item.messagesCount,
                backgroundColor = Color.Orange.copy(alpha = 0.1f),
            )
        }
    }
}

@Suppress("MagicNumber")
private val Color.Companion.Orange get() = Color(0xFFFFA500)

@Suppress("MagicNumber")
private val Color.Companion.Purple get() = Color(0xFF800080)

private fun LazyListScope.unreadChannelsByType(
    items: List<UnreadChannelByType>,
) {
    item {
        PaneTitle(text = "UNREAD BY CHANNEL TYPE")
    }
    itemsIndexed(
        items = items,
        key = { _, item -> item.channelType },
    ) { index, item ->
        PaneRow(
            index = index,
            lastIndex = items.lastIndex,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.channelType,
                    style = ChatTheme.typography.bodyBold,
                    color = ChatTheme.colors.textHighEmphasis,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${item.channelsCount} channels",
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textLowEmphasis,
                )
            }
            CountText(
                count = item.messagesCount,
                backgroundColor = Color.Blue.copy(alpha = 0.1f),
            )
        }
    }
}

private fun LazyListScope.unreadChannelsByTeam(
    items: List<Map.Entry<String, Int>>,
) {
    item {
        PaneTitle(text = "UNREAD BY TEAM (${items.size})")
    }
    itemsIndexed(
        items = items,
        key = { _, item -> item.key },
    ) { index, (team, count) ->
        PaneRow(
            index = index,
            lastIndex = items.lastIndex,
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = team,
                style = ChatTheme.typography.body,
                color = ChatTheme.colors.textHighEmphasis,
            )
            CountText(
                count = count,
                backgroundColor = Color.Purple.copy(alpha = 0.1f),
            )
        }
    }
}

@Composable
private fun PaneTitle(
    text: String,
    padding: PaddingValues = PaddingValues(
        top = 24.dp,
        start = 16.dp,
        bottom = 8.dp,
        end = 16.dp,
    ),
) {
    Text(
        modifier = Modifier.padding(padding),
        text = text,
        style = ChatTheme.typography.footnote,
        color = ChatTheme.colors.textLowEmphasis,
    )
}

@Composable
private fun PaneRow(
    index: Int,
    lastIndex: Int,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .run {
                val shape = when (index) {
                    0 -> if (lastIndex == 0) {
                        // Single item in the list
                        RoundedCornerShape(12.dp)
                    } else {
                        // Top item in the list
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    }
                    // Bottom item in the list
                    lastIndex -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    // Middle item in the list
                    else -> RectangleShape
                }
                background(
                    color = ChatTheme.colors.barsBackground,
                    shape = shape,
                )
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        content = content,
    )
}

@Composable
private fun CountText(
    count: Int,
    backgroundColor: Color,
) {
    Text(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        text = count.toString(),
        style = ChatTheme.typography.bodyBold,
        color = ChatTheme.colors.textLowEmphasis,
    )
}

@Composable
private fun Divider() {
    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
}

@Suppress("MagicNumber")
private fun formatTime(
    resources: Resources,
    seconds: Long,
): String {
    val minutes = (seconds / 60).toInt()
    val remainingSeconds = (seconds % 60).toInt()
    return buildString {
        if (minutes > 0) {
            append(resources.getQuantityString(R.plurals.time_minutes, minutes, minutes))
        }
        if (remainingSeconds > 0) {
            if (isNotEmpty()) append(" ")
            append(resources.getQuantityString(R.plurals.time_seconds, remainingSeconds, remainingSeconds))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserProfileScreenContentPreview() {
    ChatTheme {
        UserProfileScreenContent(
            modifier = Modifier.fillMaxSize(),
            state = UserProfileViewState(
                user = User(
                    id = "user-id",
                    name = "John Doe",
                    avgResponseTime = 300,
                ),
            ),
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun UnreadCountsLoadingPreview() {
    ChatTheme {
        UnreadCounts(
            unreadCounts = null,
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun UnreadCountsPreview() {
    val lastReadDate = Calendar.getInstance().apply {
        set(2025, Calendar.AUGUST, 15, 8, 15)
    }.time
    ChatTheme {
        UnreadCounts(
            unreadCounts = UnreadCounts(
                messagesCount = 10,
                threadsCount = 5,
                messagesCountByTeam = mapOf("team1" to 5, "team2" to 3),
                channels = listOf(
                    UnreadChannel(
                        cid = "messaging:1",
                        messagesCount = 3,
                        lastRead = lastReadDate,
                    ),
                    UnreadChannel(
                        cid = "messaging:2",
                        messagesCount = 7,
                        lastRead = lastReadDate,
                    ),
                ),
                threads = listOf(
                    UnreadThread(
                        parentMessageId = "message:1",
                        messagesCount = 2,
                        lastRead = lastReadDate,
                        lastReadMessageId = "message:2",
                    ),
                ),
                channelsByType = listOf(
                    UnreadChannelByType(
                        channelType = "messaging",
                        channelsCount = 2,
                        messagesCount = 10,
                    ),
                ),
            ),
        )
    }
}
