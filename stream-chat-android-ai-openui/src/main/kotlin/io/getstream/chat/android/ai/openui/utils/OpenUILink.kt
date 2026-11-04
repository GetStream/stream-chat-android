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

import java.net.Inet6Address
import java.net.InetAddress
import java.util.Locale

/**
 * The links a response may open. The model writes every link, so only the schemes react-markdown
 * keeps by default for web links and email open; a link to another app or a phone number doesn't.
 */
internal object OpenUILink {
    private val schemes = setOf("http", "https", "mailto")

    /** Whether [url] is a web or email link, without spaces or control characters. */
    fun isOpenable(url: String?): Boolean {
        if (url.isNullOrEmpty() || url.any { it.isWhitespace() || it.isISOControl() }) return false
        val scheme = url.substringBefore(':', missingDelimiterValue = "").lowercase(Locale.ROOT)
        return scheme in schemes
    }

    /**
     * Whether every address [host] resolves to is public, so a link the model wrote can't reach
     * the device itself or the local network. Resolves the name, so call it off the main thread.
     */
    fun isPublicHost(host: String?): Boolean {
        if (host.isNullOrEmpty()) return false
        val addresses = runCatching { InetAddress.getAllByName(host) }.getOrNull()
        return !addresses.isNullOrEmpty() && addresses.all(::isPublicAddress)
    }

    /** Whether [address] is neither loopback, private, link-local, multicast nor unspecified. */
    fun isPublicAddress(address: InetAddress): Boolean {
        val local = address.isAnyLocalAddress ||
            address.isLoopbackAddress ||
            address.isLinkLocalAddress ||
            address.isSiteLocalAddress ||
            address.isMulticastAddress
        // IPv6 unique local addresses (fc00::/7) are the private range Java doesn't flag.
        val uniqueLocal = address is Inet6Address && (address.address[0].toInt() and UNIQUE_LOCAL_MASK) == UNIQUE_LOCAL
        return !local && !uniqueLocal
    }

    private const val UNIQUE_LOCAL_MASK = 0xFE
    private const val UNIQUE_LOCAL = 0xFC
}
