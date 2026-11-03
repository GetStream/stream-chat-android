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

package io.getstream.chat.android.ai.openui.utils

import io.getstream.chat.android.ai.openui.lang.JS
import java.text.NumberFormat
import java.util.Locale

/** Numbers as people read them, in the device's locale. */
internal object NumberFormatting {
    private const val MAX_DECIMALS = 6
    private const val MAX_PERCENT_DECIMALS = 2
    private const val PERCENT = 100

    /** A number with up to six decimals, such as `1,234.5`. */
    fun decimal(value: Double, locale: Locale = Locale.getDefault()): String {
        if (!value.isFinite()) return JS.numberToString(value)
        return NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = MAX_DECIMALS }.format(value)
    }

    /** A whole number, such as `1,234`. */
    fun integer(value: Int, locale: Locale = Locale.getDefault()): String =
        NumberFormat.getIntegerInstance(locale).format(value)

    /** A percentage of 100, such as `12.5%` for `12.5`. */
    fun percent(value: Double, locale: Locale = Locale.getDefault()): String {
        if (!value.isFinite()) return JS.numberToString(value) + "%"
        return NumberFormat.getPercentInstance(locale)
            .apply { maximumFractionDigits = MAX_PERCENT_DECIMALS }
            .format(value / PERCENT)
    }
}
