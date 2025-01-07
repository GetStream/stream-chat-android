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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows an option when the user scrolls away from the bottom of the list. If there are any new messages it also gives
 * the user information on how many messages they haven't read.
 *
 * @param unreadCount The count of unread messages.
 * @param onClick The handler that's triggered when the user taps on the action.
 */
@Composable
internal fun MessagesScrollingOption(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .wrapContentSize(),
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .padding(top = 12.dp)
                .size(48.dp)
                .testTag("Stream_ScrollToBottomButton"),
            shape = CircleShape,
            shadowElevation = 4.dp,
            color = ChatTheme.colors.barsBackground,
        ) {
            Icon(
                modifier = Modifier.padding(16.dp),
                painter = painterResource(R.drawable.stream_compose_ic_arrow_down),
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent,
            )
        }

        if (unreadCount != 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                color = ChatTheme.colors.primaryAccent,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    text = unreadCount.toString(),
                    style = ChatTheme.typography.footnoteBold,
                    color = Color.White,
                )
            }
        }
    }
}
