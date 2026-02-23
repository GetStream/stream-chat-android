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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a meta information about the message that is shown above the message bubble.
 *
 * @param painter The icon to be shown.
 * @param text The text to be shown.
 * @param modifier Modifier for styling.
 * @param contentPadding The inner padding inside the component.
 */
@Composable
public fun MessageHeaderLabel(
    painter: Painter,
    modifier: Modifier = Modifier,
    text: String? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 2.dp, horizontal = 4.dp),
) {
    Row(
        modifier = modifier.padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 2.dp)
                .size(14.dp),
            painter = painter,
            contentDescription = null,
            tint = ChatTheme.colors.textSecondary,
        )

        if (text != null) {
            Text(
                modifier = Modifier.testTag("Stream_MessageHeaderLabel"),
                text = text,
                style = ChatTheme.typography.metadataDefault,
                color = ChatTheme.colors.textSecondary,
            )
        }
    }
}
