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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ReactionResolver
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.ifNotNull

/**
 * Component for rendering reaction emoji toggles.
 *
 * @param type The string representation of the reaction.
 * @param emoji The emoji character the [type] maps to, if any. See [ReactionResolver].
 * @param size The size of the reaction toggle.
 * @param checked Whether the toggle is checked.
 * @param onCheckedChange Callback when the checked state of the toggle changes.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun ReactionToggle(
    type: String,
    emoji: String?,
    size: ReactionToggleSize,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    emoji?.let {
        val containerSize = size.toContainerSize()
        ChatTheme.componentFactory.ReactionIcon(
            type = type,
            emoji = emoji,
            size = size.toIconSize(),
            modifier = modifier
                .applyIf(checked) {
                    background(ChatTheme.colors.backgroundCoreSelected, CircleShape)
                }
                .ifNotNull(onCheckedChange) { onChange ->
                    clip(CircleShape).clickable { onChange(!checked) }
                }
                .defaultMinSize(minWidth = containerSize, minHeight = containerSize)
                .wrapContentSize(),
        )
    }
}

/** Defines the size class of the reaction toggle. */
public enum class ReactionToggleSize {
    Medium, Large, ExtraLarge
}

private fun ReactionToggleSize.toContainerSize(): Dp = when (this) {
    ReactionToggleSize.Medium -> 32.dp
    ReactionToggleSize.Large -> 40.dp
    ReactionToggleSize.ExtraLarge -> 48.dp
}

private fun ReactionToggleSize.toIconSize() = when (this) {
    ReactionToggleSize.Medium -> ReactionIconSize.Medium
    ReactionToggleSize.Large -> ReactionIconSize.Medium
    ReactionToggleSize.ExtraLarge -> ReactionIconSize.Large
}

@Preview(showBackground = true)
@Composable
private fun ReactionTogglePreview() {
    ChatTheme {
        Column(Modifier.padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReactionToggleSize.entries.forEach { size ->
                    ReactionToggle(
                        type = "like",
                        emoji = "👍",
                        size = size,
                        checked = false,
                        onCheckedChange = {},
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReactionToggleSize.entries.forEach { size ->
                    ReactionToggle(
                        type = "like",
                        emoji = "👍",
                        size = size,
                        checked = true,
                        onCheckedChange = {},
                    )
                }
            }
        }
    }
}
