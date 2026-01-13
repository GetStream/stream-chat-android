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

package io.getstream.chat.android.ui.common.utils.extensions

import io.getstream.chat.android.core.internal.InternalStreamChatApi

internal fun String.initials(): String {
    return trim()
        .split("\\s+".toRegex())
        .take(2)
        .joinToString(separator = "") { it.take(1).uppercase() }
}

/**
 * Checks if a message contains a link using a
 * regular expression.
 */
internal fun String.containsLinks(): Boolean {
    val regex = """(^|\s)((http|https|ftp)://)?[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)+(/[a-zA-Z0-9- ;,./?%&=]*)?"""
        .toRegex(RegexOption.IGNORE_CASE)
    return this.contains(regex = regex)
}

/**
 * Small extension function designed to add a
 * scheme to URLs that do not have one so that
 * they can be opened using [android.content.Intent.ACTION_VIEW]
 */
@InternalStreamChatApi
public fun String.addSchemeToUrlIfNeeded(): String = when {
    this.startsWith("mailto:") -> this
    this.startsWith("http://") -> this
    this.startsWith("https://") -> this
    else -> "http://$this"
}
