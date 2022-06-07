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

package io.getstream.chat.android.ui.common.internal

import android.content.res.ColorStateList
import android.graphics.Color
import kotlin.math.roundToInt

internal fun adjustColorLBrightness(color: Int, factor: Float): Int {
    val a = Color.alpha(color)
    val r = (Color.red(color) * factor).roundToInt()
    val g = (Color.green(color) * factor).roundToInt()
    val b = (Color.blue(color) * factor).roundToInt()
    return Color.argb(
        a,
        r.coerceAtMost(255),
        g.coerceAtMost(255),
        b.coerceAtMost(255)
    )
}

internal fun getColorList(normalColor: Int, selectedColor: Int, disabledColor: Int) = ColorStateList(
    arrayOf(
        intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_selected),
        intArrayOf(android.R.attr.state_enabled, android.R.attr.state_selected),
        intArrayOf(-android.R.attr.state_enabled)
    ),
    intArrayOf(normalColor, selectedColor, disabledColor)
)
