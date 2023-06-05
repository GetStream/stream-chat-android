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

package io.getstream.chat.android.ui.utils

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.widget.TextView
import androidx.core.util.PatternsCompat
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Linkify links the part of the text based on matching pattern.
 *
 * This class is a simplified version of [Linkify] and differs only in one following way
 * It doesn't remove any existing URLSpan from the Spannable.
 */
@InternalStreamChatApi
public object Linkify {

    private val COMPARATOR: Comparator<LinkSpec> = Comparator { a, b ->
        when {
            a.start < b.start -> -1
            a.end > b.end -> -1
            a.start > b.start -> 1
            a.end < b.end -> 1
            else -> 0
        }
    }

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
     */
    public fun addLinks(textView: TextView) {
        val t: CharSequence = textView.text

        if (t is Spannable) {
            if (addLinks(t)) {
                addLinkMovementMethod(textView)
            }
        } else {
            val s = SpannableString.valueOf(t)
            if (addLinks(s)) {
                addLinkMovementMethod(textView)
                textView.text = s
            }
        }
    }

    /**
     * Scans the provided spannable text and turns all occurrences
     * of the link types into clickable links (Currently only support web urls).
     *
     * @param text Spannable whose text is to be marked-up with links.
     * @return True if at least one link is found and applied.
     */
    @SuppressLint("RestrictedApi")
    private fun addLinks(text: Spannable): Boolean {
        val links = mutableListOf<LinkSpec>()
        gatherLinks(
            links, text, PatternsCompat.AUTOLINK_WEB_URL, arrayOf("http://", "https://", "rtsp://"),
            Linkify.sUrlMatchFilter, null
        )
        gatherLinks(
            links, text, PatternsCompat.AUTOLINK_EMAIL_ADDRESS, arrayOf("mailto:"),
            null, null
        )

        pruneOverlaps(links, text)

        if (links.isEmpty()) return false

        links.forEach { link ->
            if (link.markwonAddedSpan == null) {
                applyLink(link.url!!, link.start, link.end, text)
            }
        }

        return true
    }

    private fun addLinkMovementMethod(t: TextView) {
        val m = t.movementMethod
        if (m !is LinkMovementMethod) {
            if (t.linksClickable) {
                t.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    private fun applyLink(
        url: String,
        start: Int,
        end: Int,
        text: Spannable,
    ) {
        val urlSpanFactory = DEFAULT_SPAN_FACTORY
        val span = urlSpanFactory(url)
        text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun makeUrl(
        url: String?,
        prefixes: Array<String>,
        matcher: Matcher,
        filter: Linkify.TransformFilter?,
    ): String? {
        if (url == null) return null

        var transformedUrl = filter?.transformUrl(matcher, url) ?: url

        var hasPrefix = false
        for (i in prefixes.indices) {
            if (transformedUrl.regionMatches(0, prefixes[i], 0, prefixes[i].length, ignoreCase = true)) {
                hasPrefix = true

                // Fix capitalization if necessary
                if (!transformedUrl.regionMatches(0, prefixes[i], 0, prefixes[i].length, ignoreCase = false)) {
                    transformedUrl = prefixes[i] + transformedUrl.substring(prefixes[i].length)
                }
                break
            }
        }
        if (!hasPrefix && prefixes.isNotEmpty()) {
            transformedUrl = prefixes[0] + transformedUrl
        }
        return transformedUrl
    }

    @Suppress("LongParameterList")
    private fun gatherLinks(
        links: MutableList<LinkSpec>,
        s: Spannable,
        pattern: Pattern,
        schemes: Array<String>,
        matchFilter: Linkify.MatchFilter?,
        transformFilter: Linkify.TransformFilter?,
    ) {
        val m = pattern.matcher(s)
        while (m.find()) {
            val start = m.start()
            val end = m.end()
            if (matchFilter == null || matchFilter.acceptMatch(s, start, end)) {
                val url: String? = makeUrl(m.group(0), schemes, m, transformFilter)
                val spec = LinkSpec(url = url, start = start, end = end)
                links.add(spec)
            }
        }
    }

    @Suppress("NestedBlockDepth")
    private fun pruneOverlaps(links: MutableList<LinkSpec>, text: Spannable) {
        // Append spans added by Markwon to remove any overlap.
        val urlSpans: Array<URLSpan> = text.getSpans(0, text.length, URLSpan::class.java)
        urlSpans.forEach { span ->
            val spec = LinkSpec(
                markwonAddedSpan = span,
                start = text.getSpanStart(span),
                end = text.getSpanEnd(span)
            )
            links.add(spec)
        }

        links.sortWith(COMPARATOR)

        var len = links.size
        var i = 0
        while (i < len - 1) {
            val a: LinkSpec = links[i]
            val b: LinkSpec = links[i + 1]
            var remove = -1
            if (a.start <= b.start && a.end > b.start) {
                when {
                    b.end <= a.end -> {
                        remove = i + 1
                    }
                    a.end - a.start > b.end - b.start -> {
                        remove = i + 1
                    }
                    a.end - a.start < b.end - b.start -> {
                        remove = i
                    }
                }
                if (remove != -1) {
                    val span: URLSpan? = links[remove].markwonAddedSpan
                    if (span != null) {
                        text.removeSpan(span)
                    }
                    links.removeAt(remove)
                    len--
                    continue
                }
            }
            i++
        }
    }

    private data class LinkSpec(
        val markwonAddedSpan: URLSpan? = null,
        val url: String? = null,
        val start: Int,
        val end: Int,
    )

    private val DEFAULT_SPAN_FACTORY: (string: String?) -> URLSpan = ::URLSpan
}
