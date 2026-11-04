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

package io.getstream.chat.android.ai.openui.lang

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Edge cases of the message protocol's attributes and syntax detection. */
internal class MessageEnvelopeTest {
    @Test
    fun `attributes round-trip, skip empty keys and read malformed escapes as is`() {
        val message = buildMessage("x", attributes = mapOf("library" to "chat app", "v" to "a=b&c"), end = true)
        val parsed = parseMessage(message)
        assertEquals(mapOf("library" to "chat app", "v" to "a=b&c"), parsed.attributes)
        assertTrue(parsed.isEnded)

        val raw = parseMessage("]]>openui:content?=x&flag&bad=%zz+y&eq=a=b\nbody")
        assertEquals(mapOf("flag" to "", "bad" to "%zz y", "eq" to "a=b"), raw.attributes)
        assertEquals("body", raw.content)
        assertFalse(raw.isEnded)
    }

    @Test
    fun `OpenUI Lang is recognized in a fenced block or a root statement`() {
        assertTrue(MessageEnvelope.hasLangSyntax("text\n```openui-lang\nx = 1\n```"))
        assertFalse(MessageEnvelope.hasLangSyntax(""))
        assertFalse(MessageEnvelope.hasLangSyntax(null))
    }
}
