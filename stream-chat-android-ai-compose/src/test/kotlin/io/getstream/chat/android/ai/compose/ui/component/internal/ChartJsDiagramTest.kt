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

package io.getstream.chat.android.ai.compose.ui.component.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ChartJsDiagramTest {

    @Test
    fun `a JavaScript string literal escapes quotes, backslashes and line breaks`() {
        assertEquals(
            """"{\"a\":\"b\\\\c\"}\n\r\t"""",
            "{\"a\":\"b\\\\c\"}\n\r\t".toJsStringLiteral(),
        )
    }

    @Test
    fun `a JavaScript string literal escapes other control and line separator characters`() {
        assertEquals("\"\\u0000\\u001f\\u2028\\u2029\"", ("\u0000\u001f" + Char(0x2028) + Char(0x2029)).toJsStringLiteral())
    }

    @Test
    fun `a JavaScript string literal keeps markup as it is`() {
        assertEquals("\"</SCRIPT><img>\"", "</SCRIPT><img>".toJsStringLiteral())
    }

    @Test
    fun `the render script passes the configuration as a single string literal`() {
        val config = """{"type":"bar","data":{"labels":["x\")});alert(1);//"]}}"""

        val script = renderChartScript(config)

        assertTrue(script.endsWith("})(${config.toJsStringLiteral()});"))
    }

    @Test
    fun `the render script shows an error as text`() {
        val script = renderChartScript("{}")

        assertTrue(script.contains("message.textContent = 'Error rendering chart: ' + error.message;"))
        assertFalse(script.contains("innerHTML"))
    }
}
