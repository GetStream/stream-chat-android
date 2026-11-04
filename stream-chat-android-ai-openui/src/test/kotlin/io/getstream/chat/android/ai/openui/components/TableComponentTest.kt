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

package io.getstream.chat.android.ai.openui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.openui.rendering.Renderer
import io.getstream.chat.android.ai.openui.rendering.openuiLibrary
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class TableComponentTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `a cell that scrolls horizontally renders inside the table`() {
        compose.setContent {
            Renderer(
                response = "root = Table([Col(\"Snippet\", [CodeBlock(\"kotlin\", \"val x = 1\")])])",
                library = openuiLibrary,
            )
        }

        compose.onNodeWithText("SNIPPET").assertExists()
        compose.onNodeWithText("val x = 1", substring = true).assertExists()
    }

    @Test
    fun `an editable table that finishes streaming shows no unsaved changes`() {
        val head = "root = EditableTable(\"roster\", [colName, colRole], [row1])\n" +
            "colName = {type: \"text\", key: \"name\", header: \"Name\"}\n" +
            "colRole = {type: \"text\", key: \"role\", header: \"Role\"}\n"
        var response by mutableStateOf(head + "row1 = {id: \"1\", values: [\"Ann\"]}")
        var isStreaming by mutableStateOf(true)
        compose.setContent {
            Renderer(response = response, library = openuiLibrary, isStreaming = isStreaming)
        }
        compose.waitForIdle()

        response = head + "row1 = {id: \"1\", values: [\"Ann\", \"Dev\"]}"
        compose.waitForIdle()
        isStreaming = false
        compose.waitForIdle()

        compose.onNodeWithText("Dev", substring = true).assertExists()
        compose.onNodeWithText("Save Changes").assertDoesNotExist()
    }
}
