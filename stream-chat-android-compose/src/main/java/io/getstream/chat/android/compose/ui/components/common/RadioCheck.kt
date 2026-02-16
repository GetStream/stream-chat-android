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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.ifNotNull

@Composable
internal fun RadioCheck(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    borderColor: Color = ChatTheme.colors.controlRadioCheckBorder,
    enabled: Boolean = true,
) {
    val colors = ChatTheme.colors
    Box(
        modifier = modifier
            .size(24.dp)
            .run {
                when {
                    !checked && enabled -> border(1.dp, borderColor, CircleShape)
                    !checked -> border(1.dp, colors.borderUtilityDisabled, CircleShape)
                    enabled -> background(colors.controlRadioCheckBgSelected, CircleShape)
                    else -> background(colors.backgroundCoreDisabled, CircleShape)
                }
            }
            .ifNotNull(onCheckedChange) { onCheckedChange ->
                clickable(
                    enabled = enabled,
                    bounded = false,
                    onClick = { onCheckedChange(!checked) },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(12.dp),
                painter = painterResource(R.drawable.stream_compose_ic_checkmark),
                tint = colors.controlRadioCheckIconSelected,
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
private fun RadioCheckPreview() {
    ChatTheme {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RadioCheck(checked = false, enabled = true, onCheckedChange = {})
            RadioCheck(checked = true, enabled = true, onCheckedChange = {})
            RadioCheck(checked = false, enabled = false, onCheckedChange = {})
            RadioCheck(checked = true, enabled = false, onCheckedChange = {})
        }
    }
}
