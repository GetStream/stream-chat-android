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

import io.getstream.chat.android.randomString
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Test

internal class MessageEllipsizeTest {

    @Test
    fun longMessagesShouldBeEllipsized() {
        val randomString = randomString(size = 6000)
        val textLimit = 100

        val expected = "${randomString.substring(0..textLimit)}..."
        val result = ellipsizeText(randomString, textLimit)

        assertEquals(expected, result)
    }

    @Test
    fun tallMessagesShouldBeEllipsized() {
        val maxLineBreaks = 2
        val tallTextList = buildList {
            repeat(4) {
                add("${randomString(4)}\n")
            }
        }

        val text = buildString {
            tallTextList.forEach(::append)
            appendLine("...")
        }
        val expected = buildString {
            tallTextList.take(maxLineBreaks).forEach(::append)
            appendLine("...")
        }

        val textLimit = 100
        val result = ellipsizeText(text, textLimit, maxLineBreaks)

        assertEquals(expected, result)
    }
}
