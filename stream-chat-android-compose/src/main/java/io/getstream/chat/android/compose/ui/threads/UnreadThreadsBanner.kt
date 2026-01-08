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

package io.getstream.chat.android.compose.ui.threads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable

/**
 * Composable a banner showing the number of unread threads.
 * It will not be shown if [unreadThreads] is zero.
 *
 * @param unreadThreads The number of unread threads.
 * @param modifier [Modifier] instance for general styling.
 * @param onClick Action invoked when the user clicks on the banner.
 */
@Composable
public fun UnreadThreadsBanner(
    unreadThreads: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    if (unreadThreads > 0) {
        val clickableModifier = if (onClick != null) {
            Modifier.clickable { onClick() }
        } else {
            Modifier
        }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ChatTheme.colors.textHighEmphasis)
                .then(clickableModifier)
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val text =
                pluralStringResource(R.plurals.stream_compose_thread_list_new_threads, unreadThreads, unreadThreads)
            Text(
                modifier = Modifier.weight(1f),
                text = text,
                fontSize = 16.sp,
                color = ChatTheme.colors.barsBackground,
                lineHeight = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                modifier = Modifier.minimumInteractiveComponentSize(),
                painter = painterResource(id = R.drawable.stream_compose_ic_union),
                contentDescription = "Reload threads",
                tint = ChatTheme.colors.barsBackground,
            )
        }
    }
}

@Composable
@Preview
private fun UnreadThreadsBannerPreview() {
    ChatTheme {
        Surface {
            Column {
                UnreadThreadsBanner(
                    unreadThreads = 17,
                    modifier = Modifier.padding(8.dp),
                    onClick = {},
                )
                UnreadThreadsBanner(
                    unreadThreads = 1,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}
