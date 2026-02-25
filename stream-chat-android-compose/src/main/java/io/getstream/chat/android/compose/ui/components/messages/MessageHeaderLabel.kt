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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.ifNotNull

/**
 * Represents meta information about the message that is shown above the message bubble.
 *
 * @param iconId The drawable resource ID for the annotation icon.
 * @param text The main annotation text. If null, only the icon is shown.
 * @param trailingText Optional trailing text shown after a separator dot.
 * @param contentColor The color for the icon and main text.
 * @param trailingTextColor The color for the trailing text.
 * @param onClick Optional click handler for the annotation row.
 */
@Composable
internal fun MessageAnnotation(
    @DrawableRes iconId: Int,
    text: String? = null,
    trailingText: String? = null,
    contentColor: Color = ChatTheme.colors.textPrimary,
    trailingTextColor: Color = ChatTheme.colors.textPrimary,
    onClick: (() -> Unit)? = null,
) {
    val typography = ChatTheme.typography

    Row(
        modifier = Modifier
            .ifNotNull(onClick) { clickable(interactionSource = null, indication = null, onClick = it) }
            .padding(vertical = StreamTokens.spacing2xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(iconId),
            contentDescription = null,
            tint = contentColor,
        )

        if (text != null) {
            Text(
                modifier = Modifier.testTag("Stream_MessageHeaderLabel"),
                text = text,
                style = typography.metadataEmphasis,
                color = contentColor,
            )
        }

        if (trailingText != null) {
            Text(
                text = "·",
                style = typography.metadataEmphasis,
                color = contentColor,
            )
            Text(
                text = trailingText,
                style = typography.metadataDefault,
                color = trailingTextColor,
            )
        }
    }
}
