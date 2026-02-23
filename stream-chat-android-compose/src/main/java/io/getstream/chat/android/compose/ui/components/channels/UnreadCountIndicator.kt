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

package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows the unread count badge for each channel item, to showcase how many messages
 * the user didn't read.
 *
 * @param unreadCount The number of messages the user didn't read.
 * @param modifier Modifier for styling.
 */
@Composable
public fun UnreadCountIndicator(
    unreadCount: Int,
    modifier: Modifier = Modifier,
    color: Color = ChatTheme.colors.accentError,
) {
    val displayText = if (unreadCount > LimitTooManyUnreadCount) UnreadCountMany else unreadCount.toString()
    val shape = RoundedCornerShape(9.dp)

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .background(shape = shape, color = color)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.testTag("Stream_UnreadCountIndicator"),
            text = displayText,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = ChatTheme.typography.captionBold,
        )
    }
}

/**
 * The unread count that's shown for many messages.
 */
private const val UnreadCountMany = "99+"
private const val LimitTooManyUnreadCount = 99

/**
 * Preview of [UnreadCountIndicator] with few unread messages.
 *
 * Should show a badge with the number of unread messages.
 */
@Preview(showBackground = true, name = "UnreadCountIndicator Preview (Few unread messages)")
@Composable
private fun FewMessagesUnreadCountIndicatorPreview() {
    ChatTheme {
        UnreadCountIndicator(unreadCount = 5)
    }
}

/**
 * Preview of [UnreadCountIndicator] with many unread messages.
 *
 * Should show a badge with the placeholder text.
 */
@Preview(showBackground = true, name = "UnreadCountIndicator Preview (Many unread messages)")
@Composable
private fun ManyMessagesUnreadCountIndicatorPreview() {
    ChatTheme {
        UnreadCountIndicator(unreadCount = 200)
    }
}
