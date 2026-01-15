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

package io.getstream.chat.android.compose.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable

@Composable
internal fun ContextualMenuContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = ChatTheme.colors

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(StreamTokens.radiusLg),
        color = colors.backgroundElevationElevation2,
        shadowElevation = 4.dp,
        border = BorderStroke(StreamTokens.borderStrokeSubtle, colors.borderCoreSurfaceSubtle),
    ) {
        Column(content = content)
    }
}

@Composable
internal fun ContextualMenuItem(
    label: String,
    leadingIcon: Painter? = null,
    trailingIcon: Painter? = null,
    destructive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val colors = ChatTheme.colors

    Row(
        modifier = Modifier
            .defaultMinSize(minWidth = 250.dp, minHeight = 40.dp)
            .width(IntrinsicSize.Min)
            .clip(RoundedCornerShape(StreamTokens.radiusMd))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = StreamTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        val (textColor, iconColor) = when {
            !enabled -> colors.stateTextDisabled to colors.stateTextDisabled
            destructive -> colors.accentError to colors.accentError
            else -> colors.textPrimary to colors.textSecondary
        }

        leadingIcon?.let {
            Icon(
                painter = it,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp),
            )
        }

        Text(
            text = label,
            color = textColor,
            modifier = Modifier.weight(1f),
        )

        trailingIcon?.let {
            Icon(
                painter = it,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun ContextualMenuDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth(1f)
            .padding(vertical = StreamTokens.spacing2xs),
        color = ChatTheme.colors.borderCoreSurfaceSubtle,
    )
}

@Preview(showBackground = true)
@Composable
private fun ContextualMenuPreview() {
    ChatTheme {
        ContextualMenuContent(
            Modifier
                .padding(32.dp)
                .width(IntrinsicSize.Min),
        ) {
            MenuItemPreview(enabled = true, destructive = false)
            MenuItemPreview(enabled = false, destructive = false)
            MenuItemPreview(enabled = false, destructive = true)
            ContextualMenuDivider()
            MenuItemPreview(enabled = true, destructive = true)
        }
    }
}

@Composable
private fun MenuItemPreview(enabled: Boolean, destructive: Boolean) {
    ContextualMenuItem(
        label = "{{ label }}",
        destructive = destructive,
        enabled = enabled,
        leadingIcon = painterResource(R.drawable.stream_compose_ic_copy),
        trailingIcon = painterResource(R.drawable.stream_compose_ic_checkmark),
    ) {}
}
