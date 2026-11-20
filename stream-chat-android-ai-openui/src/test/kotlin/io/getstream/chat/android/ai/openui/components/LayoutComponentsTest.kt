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

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.openui.rendering.openuiChatLibrary
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class LayoutComponentsTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `tabs show the first tab and switch on tap`() {
        compose.renderProgram(
            """
            root = Tabs([TabItem("a", "First", [TextContent("Alpha")]), TabItem("b", "Second", [TextContent("Beta")])])
            """.trimIndent(),
        )

        compose.onNodeWithText("Alpha").assertExists()
        compose.onNodeWithText("Beta").assertDoesNotExist()

        compose.onNodeWithText("Second").performClick()

        compose.onNodeWithText("Beta").assertExists()
        compose.onNodeWithText("Alpha").assertDoesNotExist()
    }

    @Test
    fun `streaming tabs follow the tab whose content grows`() {
        compose.renderProgram(
            """
            root = Tabs([TabItem("a", "First", [TextContent("Alpha")]), TabItem("b", "Second", [TextContent("Beta")])])
            """.trimIndent(),
            isStreaming = true,
        )

        compose.onNodeWithText("Beta").assertExists()
    }

    @Test
    fun `an accordion opens and closes a section on tap`() {
        compose.renderProgram(
            """
            root = Accordion([AccordionItem("a", "Shipping", [TextContent("Two days")]), AccordionItem("b", "Returns", [TextContent("Thirty days")])])
            """.trimIndent(),
        )

        compose.onNodeWithText("Two days").assertDoesNotExist()

        compose.onNodeWithText("Shipping").performClick()
        compose.onNodeWithText("Two days").assertExists()

        compose.onNodeWithText("Shipping").performClick()
        compose.onNodeWithText("Two days").assertDoesNotExist()
    }

    @Test
    fun `a streaming accordion opens its newest section`() {
        compose.renderProgram(
            """
            root = Accordion([AccordionItem("a", "Shipping", [TextContent("Two days")]), AccordionItem("b", "Returns", [TextContent("Thirty days")])])
            """.trimIndent(),
            isStreaming = true,
        )

        compose.onNodeWithText("Thirty days").assertExists()
        compose.onNodeWithText("Two days").assertDoesNotExist()
    }

    @Test
    fun `a carousel moves between slides with its arrows`() {
        compose.renderProgram(
            """
            root = Carousel([[TextContent("Slide one")], [TextContent("Slide two")]], "sunk")
            """.trimIndent(),
        )

        compose.onNodeWithText("Slide one").assertExists()
        compose.onNodeWithContentDescription("Slide 1 of 2").assertExists()
        compose.onNodeWithContentDescription("Previous slide").assertIsNotEnabled()

        compose.onNodeWithContentDescription("Next slide").performClick()
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Slide 2 of 2").assertExists()
        compose.onNodeWithContentDescription("Next slide").assertIsNotEnabled()

        compose.onNodeWithContentDescription("Previous slide").performClick()
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Slide 1 of 2").assertExists()
    }

    @Test
    fun `an open modal shows its title, content and close button`() {
        compose.renderProgram(
            """
            ${'$'}show = true
            root = Stack([Modal("Details", ${'$'}show, [TextContent("Inside the modal")])])
            """.trimIndent(),
        )

        compose.onNodeWithText("Details").assertExists()
        compose.onNodeWithText("Inside the modal").assertExists()
        compose.onNodeWithContentDescription("Close").assertExists()
    }

    @Test
    fun `a closed modal shows nothing`() {
        compose.renderProgram(
            """
            ${'$'}show = false
            root = Stack([TextContent("Page"), Modal("Details", ${'$'}show, [TextContent("Inside the modal")], "sm")])
            """.trimIndent(),
        )

        compose.onNodeWithText("Page").assertExists()
        compose.onNodeWithText("Inside the modal").assertDoesNotExist()
    }

    @Test
    fun `foldable sections open the first section and toggle on tap`() {
        compose.renderProgram(
            """
            root = Card([SectionBlock([SectionItem("a", "Overview", [TextContent("First body")]), SectionItem("b", "Details", [TextContent("Second body")])])])
            """.trimIndent(),
            library = openuiChatLibrary,
        )

        compose.onNodeWithText("First body").assertExists()
        compose.onNodeWithText("Second body").assertDoesNotExist()

        compose.onNodeWithText("Details").performClick()
        compose.onNodeWithText("Second body").assertExists()

        compose.onNodeWithText("Overview").performClick()
        compose.onNodeWithText("First body").assertDoesNotExist()
    }
}
