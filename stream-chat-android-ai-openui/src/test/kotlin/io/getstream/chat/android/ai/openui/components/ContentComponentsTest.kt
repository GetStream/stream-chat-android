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
    fun `callout icons describe their variant`() {
        compose.renderProgram(
            """
            root = Stack([Callout("info", "Note", "Read this"), Callout("success", "Saved", "All good"), Callout("warning", "Careful", "Check it"), Callout("error", "Failed", "Try again"), Callout("neutral", "Plain", "Nothing special")])
            """.trimIndent(),
        )

        compose.onNodeWithContentDescription("Information").assertExists()
        compose.onNodeWithContentDescription("Success").assertExists()
        compose.onNodeWithContentDescription("Warning").assertExists()
        compose.onNodeWithContentDescription("Error").assertExists()
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
    fun `a code block confirms that it copied its code`() {
        compose.renderProgram(
            """
            root = Stack([CodeBlock("kotlin", "val answer = 42"), CodeBlock("", "plain")])
            """.trimIndent(),
        )

        compose.onAllNodesWithText("Copy")[0].performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Copied").assertExists()
    }

    @Test
    fun `a chat card opens a source on tap`() {
        val opened = mutableListOf<String>()
        compose.renderProgram(
            """
            root = Card([TextContent("Paris is big [1]")], [{title: "Paris guide", url: "https://example.com/paris", sourceName: "Example"}])
            """.trimIndent(),
            library = openuiChatLibrary,
            onOpenUrl = { opened += it },
        )

        compose.onNodeWithText("Paris guide").performClick()
        compose.waitForIdle()

        assertEquals(listOf("https://example.com/paris"), opened)
    }
}

// Longer than the 3s a bound callout shows, plus its exit animation.
private const val CalloutDismissWaitMillis = 4_000L
