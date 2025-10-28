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

package io.getstream.chat.android.markdown

import java.util.Stack

internal fun String.fixItalicAtEnd(): String = if (this.isNotEmpty() && (this.last() == '*' || this.last() == '_')) {
    // This check is down here to emphasise that this check must be run last,
    // otherwise there's a performance drop when setting text
    if (endsWithItalic(this)) "$this&#x200A;" else this
} else {
    this
}

private fun endsWithItalic(text: String): Boolean {
    val stack = Stack<Char>()
    text.forEach { char ->
        when {
            isItalicMarker(char) && (stack.isEmpty() || stack.peek() != char) -> {
                stack.push(char)
            }

            !stack.isEmpty() && stack.peek() == char -> {
                stack.pop()
            }
        }
    }

    return stack.empty()
}

private fun isItalicMarker(char: Char): Boolean = char == '*' || char == '_'
