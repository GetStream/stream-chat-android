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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Stream-themed toggle switch built on top of Material 3 [Switch].
 *
 * @param checked Whether this switch is checked.
 * @param onCheckedChange Called when the switch is toggled. Pass `null` to make the switch non-interactive
 * (e.g. when the toggle is handled by a parent clickable).
 * @param modifier Modifier for this switch.
 * @param enabled Whether this switch is enabled.
 */
@Composable
internal fun StreamSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = ChatTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedTrackColor = colors.accentPrimary,
            uncheckedTrackColor = Color.Transparent,
            checkedThumbColor = colors.backgroundCoreOnAccent,
            uncheckedThumbColor = colors.accentNeutral,
            checkedBorderColor = Color.Transparent,
            uncheckedBorderColor = colors.borderCoreDefault,
            disabledCheckedTrackColor = colors.backgroundUtilityDisabled,
            disabledUncheckedTrackColor = Color.Transparent,
            disabledCheckedThumbColor = colors.backgroundCoreOnAccent,
            disabledUncheckedThumbColor = colors.textDisabled.copy(alpha = 0.38f),
            disabledCheckedBorderColor = Color.Transparent,
            disabledUncheckedBorderColor = colors.backgroundUtilityDisabled,
        ),
    )
}
