package io.getstream.chat.android.ui.utils.extensions

import android.graphics.Typeface.BOLD
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan

internal val String.Companion.EMPTY: String
    get() = ""

internal fun String.bold(boldRanges: List<IntRange>): SpannableString =
    SpannableString(this).apply {
        boldRanges.forEach {
            setSpan(StyleSpan(BOLD), it.first, it.last + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

internal fun String.getMentionRanges(mentionTags: List<String>): List<IntRange> =
    mentionTags.flatMap { tag ->
        Regex(tag).findAll(this).map { it.range }
    }

internal fun String.bold(mentionTags: List<String>): Spanned = bold(getMentionRanges(mentionTags))
