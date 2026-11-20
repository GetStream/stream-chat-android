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
import io.getstream.chat.android.ai.openui.lang.ActionEvent
import io.getstream.chat.android.ai.openui.rendering.openuiChatLibrary
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ActionComponentsTest {

    @get:Rule
    val compose = createComposeRule()

    private val actions = mutableListOf<ActionEvent>()

    @Test
    fun `buttons of every variant send their message to the assistant`() {
        compose.renderProgram(
            """
            root = Stack([Buttons([Button("Book", @ToAssistant("Book a table"), "primary", "normal", "large"), Button("Cancel", @ToAssistant("Cancel it"), "secondary", "destructive", "small")]), Buttons([Button("More", @ToAssistant("Show more"), "tertiary", "normal", "extra-small")], "column")])
            """.trimIndent(),
            onAction = actions::add,
        )

        compose.onNodeWithText("Book").performClick()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("More").performClick()
        compose.waitForIdle()

        assertEquals(listOf("Book a table", "Cancel it", "Show more"), actions.map(ActionEvent::humanFriendlyMessage))
    }

    @Test
    fun `buttons too wide for one row stack and still work`() {
        compose.renderProgram(
            """
            root = Buttons([Button("A very long label that does not fit next to another one", @ToAssistant("first")), Button("Another very long label that also needs the whole row", @ToAssistant("second"))])
            """.trimIndent(),
            onAction = actions::add,
        )

        compose.onNodeWithText("Another very long label that also needs the whole row").performClick()
        compose.waitForIdle()

        assertEquals(listOf("second"), actions.map(ActionEvent::humanFriendlyMessage))
    }

    @Test
    fun `buttons are disabled while the response streams`() {
        compose.renderProgram(
            """
            root = Stack([Button("Book", @ToAssistant("Book a table")), IconButton("Share", Icon("share"), @ToAssistant("share"))])
            """.trimIndent(),
            isStreaming = true,
        )

        compose.onNodeWithText("Book").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Share").assertIsNotEnabled()
    }

    @Test
    fun `icon buttons of every variant fire their action`() {
        compose.renderProgram(
            """
            root = Stack([IconButton("Like", Icon("heart"), @ToAssistant("like"), "primary", "large", "circle"), IconButton("Share", Icon("share"), @ToAssistant("share"), "tertiary", "small"), IconButton("Save", Icon("bookmark"), @ToAssistant("save"), "secondary", "extra-small")])
            """.trimIndent(),
            onAction = actions::add,
        )

        compose.onNodeWithContentDescription("Like").performClick()
        compose.onNodeWithContentDescription("Share").performClick()
        compose.onNodeWithContentDescription("Save").performClick()
        compose.waitForIdle()

        assertEquals(listOf("like", "share", "save"), actions.map(ActionEvent::humanFriendlyMessage))
    }

    @Test
    fun `a button that opens a link sends the link to the host`() {
        val opened = mutableListOf<String>()
        compose.renderProgram(
            """
            root = Button("Website", @OpenUrl("https://example.com"))
            """.trimIndent(),
            onOpenUrl = { opened += it },
        )

        compose.onNodeWithText("Website").performClick()
        compose.waitForIdle()

        assertEquals(listOf("https://example.com"), opened)
    }

    @Test
    fun `follow-up suggestions send their text`() {
        compose.renderProgram(
            """
            root = Card([TextContent("Answer"), FollowUpBlock([FollowUpItem("Tell me more"), FollowUpItem("Show prices")])])
            """.trimIndent(),
            library = openuiChatLibrary,
            onAction = actions::add,
        )

        compose.onNodeWithText("Show prices").performClick()
        compose.waitForIdle()

        assertEquals(listOf("Show prices"), actions.map(ActionEvent::humanFriendlyMessage))
    }

    @Test
    fun `a numbered list shows its items and runs an item action`() {
        compose.renderProgram(
            """
            root = Stack([ListBlock([ListItem("Louvre", "Museum", null, "Open", @ToAssistant("About the Louvre")), ListItem("Orsay")])])
            """.trimIndent(),
            onAction = actions::add,
        )

        compose.onNodeWithText("1").assertExists()
        compose.onNodeWithText("2").assertExists()
        compose.onNodeWithText("Museum").assertExists()
        compose.onNodeWithText("Open").assertExists()

        compose.onNodeWithText("Louvre").performClick()
        compose.waitForIdle()

        assertEquals(listOf("About the Louvre"), actions.map(ActionEvent::humanFriendlyMessage))
    }

    @Test
    fun `a small image list shows the alt text of images that cannot load`() {
        compose.renderProgram(
            """
            root = Stack([ListBlock([ListItem("Louvre", "Museum", {src: "not a link", alt: "Glass pyramid"}), ListItem("Orsay", null, {src: "not a link", alt: "Old station"})], "image", "small")])
            """.trimIndent(),
        )

        compose.onNodeWithText("Glass pyramid").assertExists()
        compose.onNodeWithText("Orsay").assertExists()
    }

    @Test
    fun `tags show their text in every size and variant`() {
        compose.renderProgram(
            """
            root = Stack([TagBlock(["Vegan", "Spicy"], "lg"), Tag("Open", Icon("clock"), "sm", "success"), Tag("Closed", null, "md", "danger"), Tag("Busy", null, null, "warning"), Tag("New", null, null, "info"), Icon("star")])
            """.trimIndent(),
        )

        compose.onNodeWithText("Vegan").assertExists()
        compose.onNodeWithText("Spicy").assertExists()
        compose.onNodeWithText("Open").assertExists()
        compose.onNodeWithText("Closed").assertExists()
        compose.onNodeWithText("Busy").assertExists()
        compose.onNodeWithText("New").assertExists()
    }

    @Test
    fun `an entity list shows its header, rows and footer`() {
        compose.renderProgram(
            """
            root = Stack([EntityList([{left: "Rent", right: "1200", rightVariant: "number"}, {left: "Food", right: "300"}], "default", {left: "Item", right: "Cost"}, {left: "Total", right: "1500", rightVariant: "number"})])
            """.trimIndent(),
        )

        compose.onNodeWithText("Item").assertExists()
        compose.onNodeWithText("Rent").assertExists()
        compose.onNodeWithText("300").assertExists()
        compose.onNodeWithText("Total").assertExists()
    }

    @Test
    fun `a small entity list leaves out its header and footer`() {
        compose.renderProgram(
            """
            root = Stack([EntityList([{left: "Rent", right: "1200"}], "small", {left: "Item", right: "Cost"}, {left: "Total", right: "1500"})])
            """.trimIndent(),
        )

        compose.onNodeWithText("Rent").assertExists()
        compose.onNodeWithText("Item").assertDoesNotExist()
        compose.onNodeWithText("Total").assertDoesNotExist()
    }
}
