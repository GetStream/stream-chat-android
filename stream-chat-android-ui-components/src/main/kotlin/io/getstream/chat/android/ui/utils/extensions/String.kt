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

package io.getstream.chat.android.ui.utils.extensions

import android.graphics.Typeface.BOLD
import android.graphics.Typeface.ITALIC
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan

internal val String.Companion.EMPTY: String
    get() = ""

/**
 * Returns a list of ranges corresponding to the indices of each occurrence of each item.
 * If no list is provided, the range of the entire string receiver is returned.
 *
 * @param items The occurrences of items for which ranges should be derived.
 * @return The ranges of every occurrence of every item.
 */
internal fun String.getOccurrenceRanges(items: List<String>? = null, ignoreCase: Boolean = false): List<IntRange> {
    val regexOptions: Set<RegexOption> = setOfNotNull(RegexOption.IGNORE_CASE.takeIf { ignoreCase })
    return items?.flatMap { item ->
        Regex(Regex.escape(item), regexOptions).findAll(this).map { it.range }
    } ?: listOf(0 until length)
}

internal fun String.applyTypeface(typeface: Int, ranges: List<IntRange>): SpannableString =
    SpannableString(this).apply {
        ranges.forEach {
            setSpan(StyleSpan(typeface), it.first, it.last + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

internal fun String.bold(items: List<String>? = null, ignoreCase: Boolean = false): SpannableString =
    applyTypeface(BOLD, getOccurrenceRanges(items, ignoreCase))

internal fun String.italicize(items: List<String>? = null, ignoreCase: Boolean = false): SpannableString =
    applyTypeface(ITALIC, getOccurrenceRanges(items, ignoreCase))
