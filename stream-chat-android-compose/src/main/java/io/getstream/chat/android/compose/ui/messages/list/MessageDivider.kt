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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

@Composable
internal fun MessageDivider(text: String, modifier: Modifier = Modifier) {
    val colors = ChatTheme.colors

    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = ChatTheme.typography.metadataEmphasis,
            color = colors.chatTextSystem,
            modifier = Modifier
                .background(colors.backgroundCoreSurfaceSubtle, CircleShape)
                .padding(vertical = StreamTokens.spacing2xs, horizontal = StreamTokens.spacingSm),
        )
    }
}

@Preview
@Composable
private fun MessageDividerPreview() {
    ChatTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        ) {
            MessageDivider("Unread messages")
            MessageDivider("Today")
            MessageDivider("Tue, 25 Dec")
        }
    }
}
