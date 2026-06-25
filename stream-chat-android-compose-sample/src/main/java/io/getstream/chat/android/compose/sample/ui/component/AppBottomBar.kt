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

package io.getstream.chat.android.compose.sample.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.components.channels.UnreadCountIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.R as ComposeR

/**
 * Renders the default app bottom bar for switching between chats/threads.
 *
 * @param unreadChannelsCount The number of unread channels.
 * @param unreadThreadsCount The number of unread threads.
 * @param selectedOption The currently selected [AppBottomBarOption].
 * @param onOptionSelected Action when invoked when the user clicks on an [AppBottomBarOption].
 */
@Composable
fun AppBottomBar(
    unreadChannelsCount: Int,
    unreadThreadsCount: Int,
    selectedOption: AppBottomBarOption,
    onOptionSelected: (AppBottomBarOption) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp, color = ChatTheme.colors.borderCoreSubtle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatTheme.colors.backgroundCoreElevation1)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            AppBottomBarOptionTile(
                selectedIcon = ComposeR.drawable.stream_design_ic_message_bubble_fill,
                unselectedIcon = ComposeR.drawable.stream_design_ic_message_bubble,
                text = R.string.app_bottom_bar_chats,
                isSelected = selectedOption == AppBottomBarOption.CHATS,
                onClick = { onOptionSelected(AppBottomBarOption.CHATS) },
                decorationBadge = if (unreadChannelsCount > 0) {
                    { NavBarBadge(unreadChannelsCount) }
                } else {
                    null
                },
            )
            AppBottomBarOptionTile(
                selectedIcon = ComposeR.drawable.stream_design_ic_thread_fill,
                unselectedIcon = ComposeR.drawable.stream_design_ic_thread,
                text = R.string.app_bottom_bar_threads,
                isSelected = selectedOption == AppBottomBarOption.THREADS,
                onClick = { onOptionSelected(AppBottomBarOption.THREADS) },
                decorationBadge = if (unreadThreadsCount > 0) {
                    { NavBarBadge(unreadThreadsCount) }
                } else {
                    null
                },
            )
        }
    }
}

/**
 * Defines the possible options of the app bottom bar.
 */
enum class AppBottomBarOption {
    CHATS,
    THREADS,
}

@Composable
private fun AppBottomBarOptionTile(
    @DrawableRes selectedIcon: Int,
    @DrawableRes unselectedIcon: Int,
    @StringRes text: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    decorationBadge: (@Composable () -> Unit)? = null,
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) ChatTheme.colors.textPrimary else ChatTheme.colors.textTertiary,
    )
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(
                indication = ripple(),
                interactionSource = null,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = 56.dp, minWidth = 72.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Box {
            Icon(
                painter = painterResource(if (isSelected) selectedIcon else unselectedIcon),
                contentDescription = null,
                tint = contentColor,
            )
            decorationBadge?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-6).dp),
                ) {
                    it()
                }
            }
        }
        Text(
            text = stringResource(text),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
        )
    }
}

@Composable
private fun NavBarBadge(count: Int) {
    Box(
        modifier = Modifier
            .background(color = ChatTheme.colors.borderCoreOnInverse, shape = CircleShape)
            .padding(2.dp),
    ) {
        UnreadCountIndicator(
            modifier = Modifier.defaultMinSize(minHeight = 16.dp, minWidth = 16.dp),
            unreadCount = count,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppBottomBarChatsSelectedPreview() {
    ChatTheme {
        AppBottomBar(
            unreadChannelsCount = 100,
            unreadThreadsCount = 4,
            selectedOption = AppBottomBarOption.CHATS,
            onOptionSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppBottomBarThreadsSelectedPreview() {
    ChatTheme {
        AppBottomBar(
            unreadChannelsCount = 0,
            unreadThreadsCount = 0,
            selectedOption = AppBottomBarOption.THREADS,
            onOptionSelected = {},
        )
    }
}
