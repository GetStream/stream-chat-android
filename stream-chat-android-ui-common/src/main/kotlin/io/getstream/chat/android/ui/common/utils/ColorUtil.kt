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

package io.getstream.chat.android.ui.common.utils

import android.graphics.Color
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlin.math.roundToInt

/**
 * Used for gradient color adjustment when the user doesn't have an image.
 *
 * @param color The color to adjust.
 * @param factor The factor by which we adjust the color.
 * @return [Int] ARGB value of the color after adjustment.
 */
@InternalStreamChatApi
public fun adjustColorBrightness(color: Int, factor: Float): Int {
    val a = Color.alpha(color)
    val r = (Color.red(color) * factor).roundToInt()
    val g = (Color.green(color) * factor).roundToInt()
    val b = (Color.blue(color) * factor).roundToInt()
    return Color.argb(
        a,
        r.coerceAtMost(MAX_COLOR_COMPONENT_VALUE),
        g.coerceAtMost(MAX_COLOR_COMPONENT_VALUE),
        b.coerceAtMost(MAX_COLOR_COMPONENT_VALUE),
    )
}

/**
 * Maximum value a color component can have.
 */
private const val MAX_COLOR_COMPONENT_VALUE = 255
