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
}
