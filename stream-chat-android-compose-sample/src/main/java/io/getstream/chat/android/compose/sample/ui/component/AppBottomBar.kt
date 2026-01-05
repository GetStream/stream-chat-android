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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.components.channels.UnreadCountIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.R as UiCommonR

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.barsBackground),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        AppBottomBarOptionTile(
            icon = R.drawable.ic_chats,
            text = R.string.app_bottom_bar_chats,
            isSelected = selectedOption == AppBottomBarOption.CHATS,
            onClick = { onOptionSelected(AppBottomBarOption.CHATS) },
            decorationBadge = {
                if (unreadChannelsCount > 0) {
                    UnreadCountIndicator(unreadChannelsCount)
                }
            },
        )
        AppBottomBarOptionTile(
            icon = UiCommonR.drawable.stream_compose_ic_mentions,
            text = R.string.app_bottom_bar_mentions,
            isSelected = selectedOption == AppBottomBarOption.MENTIONS,
            onClick = { onOptionSelected(AppBottomBarOption.MENTIONS) },
        )
        AppBottomBarOptionTile(
            icon = R.drawable.ic_threads,
            text = R.string.app_bottom_bar_threads,
            isSelected = selectedOption == AppBottomBarOption.THREADS,
            onClick = { onOptionSelected(AppBottomBarOption.THREADS) },
            decorationBadge = {
                if (unreadThreadsCount > 0) {
                    UnreadCountIndicator(unreadThreadsCount)
                }
            },
        )
    }
}

/**
 * Defines the possible options of the app bottom bar.
 */
enum class AppBottomBarOption {
    CHATS,
    MENTIONS,
    THREADS,
}

@Composable
private fun AppBottomBarOptionTile(
    @DrawableRes icon: Int,
    @StringRes text: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    decorationBadge: (@Composable () -> Unit)? = null,
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) ChatTheme.colors.textHighEmphasis else ChatTheme.colors.textLowEmphasis,
    )
    Box(
        modifier = Modifier
            .clickable(
                indication = ripple(bounded = false),
                interactionSource = null,
                onClick = onClick,
            )
            .padding(4.dp),
    ) {
        // Content
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = contentColor,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(text),
                fontSize = 12.sp,
                color = contentColor,
            )
        }
        // Decoration badge
        decorationBadge?.let {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                decorationBadge()
            }
        }
    }
}
