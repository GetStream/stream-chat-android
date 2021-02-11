package io.getstream.chat.android.ui.common.extensions.internal

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
 * @param items the occurrences of items for which ranges should be derived
 * @return the ranges of every occurrence of every item
 */
internal fun String.getOccurrenceRanges(items: List<String>? = null, ignoreCase: Boolean = false): List<IntRange> {
    val regexOptions: Set<RegexOption> = setOfNotNull(RegexOption.IGNORE_CASE.takeIf { ignoreCase })
    return items?.flatMap { item ->
        Regex(item, regexOptions).findAll(this).map { it.range }
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
