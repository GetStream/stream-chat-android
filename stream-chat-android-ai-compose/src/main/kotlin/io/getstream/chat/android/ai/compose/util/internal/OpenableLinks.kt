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

package io.getstream.chat.android.ai.compose.util.internal

import androidx.compose.ui.platform.UriHandler
import java.util.Locale

private const val HTTP = "http"
private const val HTTPS = "https"
private const val MAILTO = "mailto"

/** The kinds of link an agent's reply opens by default: web and email links. */
internal val DefaultLinkSchemes: Set<String> = setOf(HTTP, HTTPS, MAILTO)

/**
 * Whether [url] is a link of one of [schemes], with a host of its own when it is a web link.
 *
 * @param url The link the agent wrote.
 * @param schemes The schemes that may open, in lower case.
 */
internal fun isOpenableLink(url: String, schemes: Set<String>): Boolean {
    val scheme = url.substringBefore(':', missingDelimiterValue = "").lowercase(Locale.ROOT)
    val allowed = scheme.isNotEmpty() && scheme in schemes
    if (!allowed || url.any { it.isWhitespace() || it.isISOControl() }) return false
    return scheme != HTTPS && scheme != HTTP || hasHost(url.substring(scheme.length + 1))
}

// Whether the rest of a web link, after its scheme, names a host.
private fun hasHost(rest: String): Boolean {
    if (!rest.startsWith("//")) return false
    val authority = rest.drop(2).substringBefore('/').substringBefore('?').substringBefore('#')
    val host = authority.substringAfterLast('@')
    // An IPv6 host keeps its colons in brackets; otherwise a colon starts the port.
    val name = if (host.startsWith("[")) host.substringBefore(']') else host.substringBefore(':')
    return name.isNotEmpty()
}

/**
 * A [UriHandler] that passes to [delegate] only the links [isOpenableLink] lets open, and ignores the rest.
 *
 * @param delegate What opens the links that may open.
 * @param schemes The schemes that may open, in lower case.
 */
internal class OpenableLinkUriHandler(
    private val delegate: UriHandler,
    private val schemes: Set<String> = DefaultLinkSchemes,
) : UriHandler {
    override fun openUri(uri: String) {
        if (isOpenableLink(uri, schemes)) delegate.openUri(uri)
    }
}
