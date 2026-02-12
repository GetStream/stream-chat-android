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

package io.getstream.chat.android.compose.ui.components.reactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Component for rendering reaction emojis.
 *
 * @param size The size of the reaction icon.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun ReactionIcon(
    type: String,
    emoji: String?,
    size: ReactionIconSize,
    modifier: Modifier = Modifier,
) {
    emoji?.let {
        Text(
            modifier = modifier.wrapContentSize(),
            text = it,
            fontSize = size.toFontSize(),
        )
    }
}

/** Defines the size class of the reaction icon. */
public enum class ReactionIconSize {
    Small, Medium, Large, ExtraLarge, ExtraExtraLarge
}

private fun ReactionIconSize.toFontSize() = when (this) {
    ReactionIconSize.Small -> 16.sp
    ReactionIconSize.Medium -> 24.sp
    ReactionIconSize.Large -> 32.sp
    ReactionIconSize.ExtraLarge -> 48.sp
    ReactionIconSize.ExtraExtraLarge -> 64.sp
}

@Preview(showBackground = true)
@Composable
private fun ReactionIconPreview() {
    ChatTheme {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReactionIconSize.entries.forEach { size ->
                ReactionIcon(
                    type = "like",
                    emoji = "👀",
                    size = size,
                )
            }
        }
    }
}
