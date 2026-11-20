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

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.openui.rendering.openuiChatLibrary
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ContentComponentsTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `a standard card shows its headers, text and labels`() {
        compose.renderProgram(
            """
            root = Card([CardHeader("Paris", "France"), InlineHeader("Facts", "At a glance"), TextContent("Big city", "large-heavy"), TextContent("Small print", "small"), Label("Population"), Separator(), Separator("vertical")], "sunk", "row", "s", "center", "between", true)
            """.trimIndent(),
        )

        compose.onNodeWithText("Paris").assertExists()
        compose.onNodeWithText("France").assertExists()
        compose.onNodeWithText("Facts").assertExists()
        compose.onNodeWithText("At a glance").assertExists()
        compose.onNodeWithText("Big city").assertExists()
        compose.onNodeWithText("Small print").assertExists()
        compose.onNodeWithText("Population").assertExists()
    }

    @Test
    fun `a stack lays out its children in a row`() {
        compose.renderProgram(
            """
            root = Stack([Card([TextContent("Left")], "clear"), Card([TextContent("Right")])], "row", "l", "start", "between", true)
            """.trimIndent(),
        )

        compose.onNodeWithText("Left").assertExists()
        compose.onNodeWithText("Right").assertExists()
    }

    @Test
    fun `markdown text renders in each container variant`() {
        compose.renderProgram(
            """
            root = Stack([MarkDownRenderer("**Bold** words", "card"), MarkDownRenderer("Sunk words", "sunk"), MarkDownRenderer("Clear words")])
            """.trimIndent(),
        )

        compose.onNodeWithText("Bold words", substring = true).assertExists()
        compose.onNodeWithText("Sunk words").assertExists()
        compose.onNodeWithText("Clear words").assertExists()
    }

    @Test
    fun `callouts show their title, description and a meaningful icon`() {
        compose.renderProgram(
            """
            root = Stack([Callout("info", "Note", "Read this"), Callout("success", "Saved", "All good"), Callout("warning", "Careful", "Check it"), Callout("error", "Failed", "Try again"), Callout("neutral", "Plain", "Nothing special")])
            """.trimIndent(),
        )

        compose.onNodeWithText("Note").assertExists()
        compose.onNodeWithText("Try again").assertExists()
        compose.onNodeWithContentDescription("Information").assertExists()
        compose.onNodeWithContentDescription("Success").assertExists()
        compose.onNodeWithContentDescription("Warning").assertExists()
        compose.onNodeWithContentDescription("Error").assertExists()
        compose.onNodeWithText("Plain").assertExists()
    }

    @Test
    fun `a callout bound to a variable dismisses itself`() {
        compose.mainClock.autoAdvance = false
        compose.renderProgram(
            """
            ${'$'}visible = true
            root = Stack([Callout("success", "Saved", "Done.", ${'$'}visible)])
            """.trimIndent(),
        )
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Saved").assertExists()

        compose.mainClock.advanceTimeBy(CalloutDismissWaitMillis)

        compose.onNodeWithText("Saved").assertDoesNotExist()
    }

    @Test
    fun `text callouts show their title and description`() {
        compose.renderProgram(
            """
            root = Stack([TextCallout("warning", "Heads up", "Prices change"), TextCallout()])
            """.trimIndent(),
        )

        compose.onNodeWithText("Heads up").assertExists()
        compose.onNodeWithText("Prices change").assertExists()
    }

    @Test
    fun `images that cannot load show their alt text`() {
        compose.renderProgram(
            """
            root = Stack([Image("A bridge", "not a link"), ImageBlock("not a link", "A tower")])
            """.trimIndent(),
        )

        compose.onNodeWithText("A bridge").assertExists()
        compose.onNodeWithText("A tower").assertExists()
    }

    @Test
    fun `a gallery opens a preview with details and closes it`() {
        compose.renderProgram(
            """
            root = Stack([ImageGallery([{src: "not a link", alt: "Sunset", details: "Taken at dusk"}, {src: "not a link", alt: "Harbor"}])])
            """.trimIndent(),
        )

        compose.onNodeWithText("Sunset").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Taken at dusk").assertExists()

        compose.onNodeWithText("Done").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Taken at dusk").assertDoesNotExist()
    }

    @Test
    fun `a code block shows its language, code and copy action`() {
        compose.renderProgram(
            """
            root = Stack([CodeBlock("kotlin", "val answer = 42"), CodeBlock("", "plain")])
            """.trimIndent(),
        )

        compose.onNodeWithText("kotlin").assertExists()
        compose.onNodeWithText("val answer = 42").assertExists()
        compose.onNodeWithText("code").assertExists()

        compose.onAllNodesWithText("Copy")[0].performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Copied").assertExists()
    }

    @Test
    fun `a chat card lists its sources and opens one on tap`() {
        val opened = mutableListOf<String>()
        compose.renderProgram(
            """
            root = Card([TextContent("Paris is big [1]")], [{title: "Paris guide", url: "https://example.com/paris", sourceName: "Example"}])
            """.trimIndent(),
            library = openuiChatLibrary,
            onOpenUrl = { opened += it },
        )

        compose.onNodeWithText("Sources").assertExists()
        compose.onNodeWithText("Example").assertExists()

        compose.onNodeWithText("Paris guide").performClick()
        compose.waitForIdle()

        assertEquals(listOf("https://example.com/paris"), opened)
    }
}

// Longer than the 3s a bound callout shows, plus its exit animation.
private const val CalloutDismissWaitMillis = 4_000L
