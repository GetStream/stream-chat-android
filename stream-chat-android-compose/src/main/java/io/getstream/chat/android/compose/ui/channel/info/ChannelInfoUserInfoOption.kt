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

package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@Composable
internal fun ChannelInfoUserInfoOption(
    username: String,
    onClick: () -> Unit,
) {
    ChannelInfoOption(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.stream_compose_ic_person),
            contentDescription = null,
            tint = ChatTheme.colors.textSecondary,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = "@$username",
            style = ChatTheme.typography.bodyEmphasis,
            color = ChatTheme.colors.textPrimary,
        )
        Icon(
            painter = painterResource(R.drawable.stream_compose_ic_copy),
            contentDescription = stringResource(R.string.stream_ui_channel_info_copy_user_handle),
            tint = ChatTheme.colors.textSecondary,
        )
    }
}
