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

package io.getstream.chat.android.ui.utils

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.widget.TextView
import androidx.core.util.PatternsCompat
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.User
import java.util.Locale
import java.util.regex.Pattern

/**
 * Linkify links the part of the text based on matching pattern.
 *
 * This class is a simplified version of [Linkify] and differs only in one following way
 * It doesn't remove any existing URLSpan from the Spannable.
 */
@InternalStreamChatApi
public object Linkify {

    /**
     * Scans the provided TextView and turns all occurrences
     * of the link types into clickable links.
     * If matches are found the movement method for the TextView is set to
     * LinkMovementMethod.
     *
     * NOTE: Because this implementation doesn't remove existing URLSpan,
     * make sure it is not repeatedly called on same text.
     *
     * @param textView TextView whose text is to be marked-up with links.
     * @param mentionableUsers List of users to be marked-up with links.
     */
    public fun addLinks(
        textView: TextView,
        mentionableUsers: List<User>,
    ) {
        val t: CharSequence = textView.text

        if (t is Spannable) {
            if (addLinks(t, mentionableUsers)) {
                addLinkMovementMethod(textView)
            }
        } else {
            val s = SpannableString.valueOf(t)
            if (addLinks(s, mentionableUsers)) {
                addLinkMovementMethod(textView)
                textView.text = s
            }
        }
    }

    /**
     * Scans the provided spannable text and turns all occurrences
     * of the link types into clickable links (Currently only support web urls).
     *
     * @param spannable Spannable whose text is to be marked-up with links.
     * @return True if at least one link is found and applied.
     */
    @SuppressLint("RestrictedApi")
    private fun addLinks(
        spannable: Spannable,
        mentionableUsers: List<User>,
    ): Boolean =
        (
            gatherSpanSpecs(
                spannable,
                PatternsCompat.AUTOLINK_WEB_URL,
                Linkify.sUrlMatchFilter,
            ) { it.makeUrlSpan(listOf("http://", "https://", "rtsp://")) } + gatherSpanSpecs(
                spannable,
                PatternsCompat.AUTOLINK_EMAIL_ADDRESS,

                null,
            ) { it.makeUrlSpan(listOf("mailto:")) } + mentionableUsers.flatMap { user ->
                gatherSpanSpecs(
                    spannable,
                    Pattern.compile("((?:\\B|^)(@${user.name})(?:\\b|\$))"),
                    null,
                ) { UserSpan(user) }
            }
            ).pruneOverlaps(spannable)
            .map { spannable.setSpan(it.span, it.start, it.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
            .isNotEmpty()

    private fun addLinkMovementMethod(t: TextView) {
        val m = t.movementMethod
        if (m !is LinkMovementMethod) {
            if (t.linksClickable) {
                t.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    /**
     * Create a URLSpan from url string.
     * If the url starts with any of the prefixes, the prefix is replaced with the prefix itself to ensure the
     * url is valid.
     * Otherwise, the first prefix is prepended to the url.
     *
     * @param prefixes List of prefixes to check for.
     * @return URLSpan with the valid url.
     */
    private fun String.makeUrlSpan(prefixes: List<String>): URLSpan = URLSpan(
        prefixes
            .map { it.lowercase(Locale.US) }
            .fold(this to false) { acc, prefix ->
                acc.first
                    .takeIf { it.startsWith(prefix, ignoreCase = true) }
                    ?.replace(prefix, prefix, ignoreCase = true)
                    ?.let { it to true }
                    ?: acc
            }
            .takeIf { it.second }
            ?.first
            ?: (prefixes.first() + this),
    )

    /**
     * Apply the regex pattern to the text and return the list of LinkSpecs.
     *
     * @param spannable Spannable text to apply the pattern.
     * @param pattern Pattern to apply.
     * @param matchFilter Filter to apply on the matched text.
     * @param createSpan Function to create the span from the matched text.
     *
     * @return List of SpanSpec.
     */
    private fun gatherSpanSpecs(
        spannable: Spannable,
        pattern: Pattern,
        matchFilter: Linkify.MatchFilter?,
        createSpan: (String) -> ClickableSpan,
    ): List<SpanSpec> {
        val specs = mutableListOf<SpanSpec>()
        val m = pattern.matcher(spannable)
        while (m.find()) {
            val start = m.start()
            val end = m.end()
            if (matchFilter == null || matchFilter.acceptMatch(spannable, start, end)) {
                m.group(0)?.let(createSpan)?.let { specs.add(SpanSpec(span = it, start = start, end = end)) }
            }
        }
        return specs
    }

    private fun List<SpanSpec>.pruneOverlaps(text: Spannable): List<SpanSpec> =
        this - text.getSpans(0, text.length, URLSpan::class.java).map {
            SpanSpec(
                span = it,
                start = text.getSpanStart(it),
                end = text.getSpanEnd(it),
            )
        }.flatMap { link ->
            this.filter { it.start <= link.start && it.end >= link.end } +
                this.filter { link.start <= it.start && link.end >= it.end }
        }.toSet()

    private data class SpanSpec(
        val span: ClickableSpan,
        val start: Int,
        val end: Int,
    )
}
