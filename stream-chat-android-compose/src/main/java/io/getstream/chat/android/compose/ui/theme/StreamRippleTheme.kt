/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A modified version of the default [RippleTheme] from [MaterialTheme] which
 * works in case the [MaterialTheme] is not initialized.
 */
@Immutable
internal object StreamRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor(): Color {
        return RippleTheme.defaultRippleColor(
            contentColor = LocalContentColor.current,
            lightTheme = !isSystemInDarkTheme(),
        )
    }

    @Composable
    override fun rippleAlpha(): RippleAlpha {
        return RippleTheme.defaultRippleAlpha(
            contentColor = LocalContentColor.current,
            lightTheme = !isSystemInDarkTheme(),
        )
    }
}
