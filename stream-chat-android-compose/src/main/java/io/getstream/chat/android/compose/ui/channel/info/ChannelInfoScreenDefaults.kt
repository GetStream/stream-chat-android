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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.BackButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

@Composable
internal fun ChannelInfoNavigationIcon(onClick: () -> Unit) {
    BackButton(
        painter = painterResource(id = R.drawable.stream_design_ic_arrow_left),
        onBackPressed = onClick,
    )
}

/**
 * Renders the channel/contact name heading on the channel info screen, with a trailing
 * mute icon when the chat is muted.
 *
 * @param title The channel name (group) or contact name (direct message) to display.
 * @param isMuted Whether the chat is muted. When true, a mute icon is shown after the title.
 * @param modifier The [Modifier] to be applied to this title.
 */
@Composable
internal fun ChannelInfoTitle(
    title: String,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = StreamTokens.spacingXs,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(weight = 1f, fill = false),
            text = title,
            style = ChatTheme.typography.headingLarge,
            color = ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isMuted) {
            Icon(
                painter = painterResource(id = R.drawable.stream_design_ic_mute),
                contentDescription = stringResource(R.string.stream_compose_channel_item_muted),
                tint = ChatTheme.colors.textPrimary,
            )
        }
    }
}
