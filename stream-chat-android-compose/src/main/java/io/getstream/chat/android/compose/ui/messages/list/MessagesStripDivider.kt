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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.bottomBorder
import io.getstream.chat.android.compose.ui.util.topBorder

@Composable
internal fun MessagesStripDivider(text: String, modifier: Modifier = Modifier) {
    val colors = ChatTheme.colors
    Text(
        modifier = modifier
            .padding(vertical = StreamTokens.spacingXs)
            .topBorder(colors.borderCoreSubtle)
            .bottomBorder(colors.borderCoreSubtle)
            .fillMaxWidth()
            .background(colors.backgroundCoreSurfaceSubtle)
            .padding(horizontal = StreamTokens.spacingMd, vertical = StreamTokens.spacingXs),
        text = text,
        color = colors.chatTextSystem,
        style = ChatTheme.typography.metadataEmphasis,
        textAlign = TextAlign.Center,
    )
}

@Preview
@Composable
private fun MessagesStripDividerUnreadPreview() {
    ChatTheme {
        MessagesStripDividerUnread()
    }
}

@Composable
internal fun MessagesStripDividerUnread() {
    MessagesStripDivider(text = "9 unread messages")
}

@Preview
@Composable
private fun MessagesStripDividerRepliesPreview() {
    ChatTheme {
        MessagesStripDividerReplies()
    }
}

@Composable
internal fun MessagesStripDividerReplies() {
    MessagesStripDivider(text = "5 replies")
}
