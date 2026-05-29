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
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.mentionRegex
import java.util.Locale
import java.util.regex.Pattern

/**
 * Utility for linkifying message text: scans a [TextView] for URLs, email addresses, and
 * mention tokens (`@user`, `@channel`, `@here`, role mentions) referenced by a [Message], and
 * applies clickable spans for each.
 *
 * This class is a simplified version of [Linkify] and differs only in one following way
 * It doesn't remove any existing URLSpan from the Spannable.
 */
@InternalStreamChatApi
public object Linkify {

    /**
     * Scans the provided TextView and turns URLs, email addresses and every mention token
     * present in [message] into clickable links.
     *
     * NOTE: Because this implementation doesn't remove existing URLSpan,
     * make sure it is not repeatedly called on same text.
     *
     * @param textView TextView whose text will be scanned and marked up with clickable spans.
     * @param message Message providing the mention tokens to linkify alongside URLs and email addresses.
     */
    public fun addLinks(textView: TextView, message: Message) {
        val original = textView.text
        val spannable = original as? Spannable ?: SpannableString.valueOf(original)
        val specs = gatherSpecs(spannable, message).pruneOverlaps(spannable)
        if (specs.isEmpty()) return
        specs.forEach { spannable.setSpan(it.span, it.start, it.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        addLinkMovementMethod(textView)
        if (spannable !== original) textView.text = spannable
    }

    private fun gatherSpecs(spannable: Spannable, message: Message): List<SpanSpec> = buildList {
        addAll(urlSpecs(spannable))
        addAll(emailSpecs(spannable))
        message.mentionedUsers.forEach { addAll(mentionSpecs(spannable, Mention.User(it))) }
        if (message.mentionedChannel) addAll(mentionSpecs(spannable, Mention.Channel))
        if (message.mentionedHere) addAll(mentionSpecs(spannable, Mention.Here))
        message.mentionedRoles.forEach { addAll(mentionSpecs(spannable, Mention.Role(it))) }
        message.mentionedGroups.forEach { addAll(mentionSpecs(spannable, Mention.Group(it))) }
    }

    @SuppressLint("RestrictedApi")
    private fun urlSpecs(spannable: Spannable): List<SpanSpec> = gatherSpanSpecs(
        spannable,
        PatternsCompat.AUTOLINK_WEB_URL,
        Linkify.sUrlMatchFilter,
    ) { it.makeUrlSpan(listOf("http://", "https://", "rtsp://")) }

    @SuppressLint("RestrictedApi")
    private fun emailSpecs(spannable: Spannable): List<SpanSpec> = gatherSpanSpecs(
        spannable,
        PatternsCompat.AUTOLINK_EMAIL_ADDRESS,
        null,
    ) { it.makeUrlSpan(listOf("mailto:")) }

    private fun mentionSpecs(spannable: Spannable, mention: Mention): List<SpanSpec> {
        if (mention.display.isEmpty()) return emptyList()
        return gatherSpanSpecs(
            spannable,
            mentionRegex(mention.display).toPattern(),
            null,
        ) { MentionSpan(mention) }
    }

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

    /**
     * Drops any spec fully contained by, or fully containing, a URL span: either one already
     * on the buffer or one gathered in this pass.
     *
     * E.g. prevents `@user` in `https://example.com/@user` from getting its own span.
     */
    private fun List<SpanSpec>.pruneOverlaps(text: Spannable): List<SpanSpec> {
        val existingUrlSpans = text.getSpans(0, text.length, URLSpan::class.java).map {
            SpanSpec(span = it, start = text.getSpanStart(it), end = text.getSpanEnd(it))
        }
        val newUrlSpecs = filter { it.span is URLSpan }
        val dropped = mutableSetOf<SpanSpec>()
        existingUrlSpans.forEach { link ->
            filterTo(dropped) { it.contains(link) || link.contains(it) }
        }
        newUrlSpecs.forEach { link ->
            // only drop non-URL specs, otherwise a URL spec would drop itself (self-containment)
            filterTo(dropped) { it.span !is URLSpan && (it.contains(link) || link.contains(it)) }
        }
        return this - dropped
    }

    private fun SpanSpec.contains(other: SpanSpec): Boolean =
        start <= other.start && end >= other.end

    private data class SpanSpec(
        val span: ClickableSpan,
        val start: Int,
        val end: Int,
    )
}
