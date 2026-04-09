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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.theme.animation.FadingVisibility
import io.getstream.chat.android.compose.ui.util.ifNotNull

@Composable
internal fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    borderColor: Color = ChatTheme.colors.controlCheckboxBorder,
    enabled: Boolean = true,
) {
    val colors = ChatTheme.colors
    Box(
        modifier = modifier
            .size(20.dp)
            .run {
                when {
                    !checked && enabled -> border(1.dp, borderColor, CheckboxShape)
                    !checked -> border(1.dp, colors.borderUtilityDisabled, CheckboxShape)
                    enabled -> background(colors.controlCheckboxBgSelected, CheckboxShape)
                    else -> background(colors.backgroundUtilityDisabled, CheckboxShape)
                }
            }
            .ifNotNull(onCheckedChange) { onChange ->
                clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = false),
                    enabled = enabled,
                    onClick = { onChange(!checked) },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        FadingVisibility(checked) {
            if (checked) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.stream_design_ic_checkmark),
                    tint = if (enabled) ChatTheme.colors.controlCheckboxIcon else ChatTheme.colors.textDisabled,
                    contentDescription = null,
                )
            }
        }
    }
}

private val CheckboxShape = RoundedCornerShape(StreamTokens.radiusSm)

@Preview
@Composable
private fun CheckboxPreview() {
    ChatTheme {
        CheckboxStates()
    }
}

@Composable
internal fun CheckboxStates() {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Checkbox(checked = false, enabled = true, onCheckedChange = {})
        Checkbox(checked = true, enabled = true, onCheckedChange = {})
        Checkbox(checked = false, enabled = false, onCheckedChange = {})
        Checkbox(checked = true, enabled = false, onCheckedChange = {})
    }
}
