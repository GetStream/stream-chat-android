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

package io.getstream.chat.android.compose.ui.components.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamSpacings

@Composable
internal fun StreamButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: StreamButtonStyle = StreamButtonStyleDefaults.primarySolid,
    size: StreamButtonSize = StreamButtonSize.Medium,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        enabled = enabled,
        shape = RoundedCornerShape(percent = 50),
        color = style.containerColor(enabled),
        contentColor = style.contentColor(enabled),
        border = style.border(enabled),
        interactionSource = remember(::MutableInteractionSource),
    ) {
        CompositionLocalProvider(LocalTextStyle provides defaultButtonTextStyle) {
            Row(
                Modifier
                    .defaultMinSize(minHeight = size.minimumSize, minWidth = size.minimumSize)
                    .padding(horizontal = StreamSpacings.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

private val defaultButtonTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 20.sp,
)

@Preview(showBackground = true)
@Composable
private fun StreamButtonPreview() {
    ChatTheme {
        val styles = listOf(
            StreamButtonStyleDefaults.primarySolid to "Primary Solid",
            StreamButtonStyleDefaults.primaryGhost to "Primary Ghost",
            StreamButtonStyleDefaults.secondaryOutline to "Secondary Outline",
            StreamButtonStyleDefaults.secondaryGhost to "Secondary Ghost",
            StreamButtonStyleDefaults.destructiveSolid to "Destructive Solid",
            StreamButtonStyleDefaults.destructiveGhost to "Destructive Ghost",
        )

        Column(
            modifier = Modifier.padding(StreamSpacings.md),
            verticalArrangement = Arrangement.spacedBy(StreamSpacings.xs),
        ) {
            styles.forEach { (style, name) ->
                Row(horizontalArrangement = Arrangement.spacedBy(StreamSpacings.xs)) {
                    StreamButton(
                        onClick = {},
                        style = style,
                        modifier = Modifier.defaultMinSize(150.dp),
                    ) {
                        Text(name)
                    }
                    StreamButton(
                        onClick = {},
                        style = style,
                        enabled = false,
                        modifier = Modifier.defaultMinSize(150.dp),
                    ) {
                        Text(name)
                    }
                }
            }
        }
    }
}
