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
    fun `a list item runs its action on tap`() {
        compose.renderProgram(
            """
            root = Stack([ListBlock([ListItem("Louvre", "Museum", null, "Open", @ToAssistant("About the Louvre")), ListItem("Orsay")])])
            """.trimIndent(),
            onAction = actions::add,
        )

        compose.onNodeWithText("Louvre").performClick()
        compose.waitForIdle()

        assertEquals(listOf("About the Louvre"), actions.map(ActionEvent::humanFriendlyMessage))
    }
}
