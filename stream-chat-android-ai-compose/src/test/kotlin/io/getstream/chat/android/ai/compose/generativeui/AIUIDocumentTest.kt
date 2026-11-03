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

package io.getstream.chat.android.ai.compose.generativeui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AIUIDocumentTest {

    @Test
    fun `a document reads its format, version, status and content`() {
        val document = AIUIDocument.from(
            mapOf(
                "ai_ui" to mapOf(
                    "v" to 1,
                    "format" to "openui",
                    "version" to "1.0",
                    "status" to "streaming",
                    "content" to "root = Card([title])",
                ),
                "generating" to true,
            ),
        )!!

        assertEquals(AIUIFormat.OpenUI, document.format)
        assertEquals("1.0", document.version)
        assertTrue(document.isStreaming)
        assertEquals("root = Card([title])", document.text)
    }

    @Test
    fun `a finished document in JSON has no text`() {
        val content = listOf(mapOf("deleteSurface" to mapOf("surfaceId" to "s1")))
        val document = AIUIDocument.from(
            mapOf("ai_ui" to mapOf("format" to "a2ui", "status" to "complete", "content" to content)),
        )!!

        assertEquals(AIUIFormat.A2UI, document.format)
        assertNull(document.version)
        assertFalse(document.isStreaming)
        assertNull(document.text)
        assertEquals(content, document.content)
    }

    @Test
    fun `a message without a document it can read has none`() {
        assertNull(AIUIDocument.from(emptyMap()))
        assertNull(AIUIDocument.from(mapOf("ai_ui" to "root = Card([])")))
        assertNull(AIUIDocument.from(mapOf("ai_ui" to mapOf("content" to "root = Card([])"))))
        assertNull(AIUIDocument.from(mapOf("ai_ui" to mapOf("format" to "", "content" to "x"))))
        assertNull(AIUIDocument.from(mapOf("ai_ui" to mapOf("format" to "openui", "content" to null))))
        // A newer field may mean something else.
        assertNull(AIUIDocument.from(mapOf("ai_ui" to mapOf("v" to 2, "format" to "openui", "content" to "x"))))
    }

    @Test
    fun `a format is an open set`() {
        val document = AIUIDocument.from(
            mapOf("ai_ui" to mapOf("format" to "acme-cards", "version" to "", "content" to emptyMap<String, Any?>())),
        )!!

        assertEquals(AIUIFormat("acme-cards"), document.format)
        assertEquals("acme-cards", document.format.toString())
        assertEquals(listOf("openui", "a2ui"), listOf(AIUIFormat.OpenUI, AIUIFormat.A2UI).map { it.rawValue })
        // An empty version is none, and a document without a status is finished.
        assertNull(document.version)
        assertFalse(document.isStreaming)
    }

    @Test
    fun `an action is the JSON an agent reads`() {
        val action = AIUIAction(
            messageId = "m1",
            format = AIUIFormat.OpenUI,
            name = "ask_about",
            label = "Tell me more about St. Naum",
            payload = mapOf("title" to "Monastery of Saint Naum"),
        )

        assertEquals(
            mapOf(
                "v" to 1,
                "message_id" to "m1",
                "format" to "openui",
                "name" to "ask_about",
                "payload" to mapOf("title" to "Monastery of Saint Naum"),
            ),
            action.json,
        )
        assertEquals(mapOf("ai_ui_action" to action.json), action.customData)
    }
}
