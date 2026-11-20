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
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.openui.lang.ActionEvent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CardBlockComponentsTest {

    @get:Rule
    val compose = createComposeRule()

    private val actions = mutableListOf<ActionEvent>()

    @Test
    fun `snippet cards show their label and value and send a tap`() {
        compose.renderProgram(
            """
            root = Stack([SnippetCardBlock([SnippetCardItem("wifi", IconText(Icon("wifi"), "info", "m", "Wifi", "In every room"), BoldText("number", "100", "+5", "metric", "lg")), SnippetCardItem(null, ImageText("not a link", "Pool photo", "Pool", "Heated", true, "vertical", 48), Text("text", "Open", "until 10", "text", "sm"))], "grid", true, @ToAssistant("Tell me about it"), 12)])
            """.trimIndent(),
            onAction = actions::add,
        )

        compose.onNodeWithText("Wifi").assertExists()
        compose.onNodeWithText("In every room").assertExists()
        compose.onNodeWithText("100").assertExists()
        compose.onNodeWithText("+5").assertExists()
        compose.onNodeWithText("Pool photo").assertExists()
        compose.onNodeWithText("until 10").assertExists()

        compose.onNodeWithText("Wifi").performClick()
        compose.waitForIdle()

        assertEquals(listOf("Tell me about it"), actions.map(ActionEvent::humanFriendlyMessage))
    }

    @Test
    fun `overview cards show their metric and trend in a carousel`() {
        compose.renderProgram(
            """
            root = Stack([OverviewCardBlock([OverviewCardItem("rev", IconText(Icon("dollar-sign"), "success", "s", "Revenue", null, true, "horizontal"), MetricIndicatorInline("12.4k", "this month", {direction: "up", value: 12})), OverviewCardItem(null, Text(null, "Users"), MetricIndicatorInline("830", null, {direction: "down", value: 3}))], "carousel", false, @ToAssistant("Open overview"), "l")])
            """.trimIndent(),
            onAction = actions::add,
        )

        compose.onNodeWithText("Revenue").assertExists()
        compose.onNodeWithText("12.4k").assertExists()
        compose.onNodeWithText("this month").assertExists()
        compose.onNodeWithContentDescription("Up 12 percent").assertExists()
        compose.onNodeWithContentDescription("Down 3 percent").assertExists()

        compose.onNodeWithText("Revenue").performClick()
        compose.waitForIdle()

        assertEquals(listOf("Open overview"), actions.map(ActionEvent::humanFriendlyMessage))
    }

    @Test
    fun `context cards show a plain or tag title and their body`() {
        compose.renderProgram(
            """
            root = Stack([ContextCardBlock([ContextCardItem("a", "Best time", "**Spring** is mild", "gray"), ContextCardItem(null, Tag("Tip", null, "sm", "info"), "Book early", null, "not a link", "Beach")], "grid", true, @ToAssistant("More context"))])
            """.trimIndent(),
            onAction = actions::add,
        )

        compose.onNodeWithText("Best time").assertExists()
        compose.onNodeWithText("Spring is mild", substring = true).assertExists()
        compose.onNodeWithText("Tip").assertExists()
        compose.onNodeWithText("Book early").assertExists()

        compose.onNodeWithText("Best time").performClick()
        compose.waitForIdle()

        assertEquals(listOf("More context"), actions.map(ActionEvent::humanFriendlyMessage))
    }

    @Test
    fun `composite cards show their header, body and footer`() {
        compose.renderProgram(
            """
            root = Stack([CompositeCardBlock([CompositeCardItem("plan", ImageTextLarge("not a link", "Hotel front", "Grand Hotel", "City center", true), [Text(null, "Sea view"), BoldText(null, "Free breakfast"), MetricIndicatorInline("4.8", "rating"), IconText(Icon("wifi"), null, null, "Wifi"), TagBlock(["Pool", "Spa"]), EntityList([{left: "Check-in", right: "14:00"}]), ListBlock([ListItem("Room service")])], {price: MetricIndicatorWithStrikethrough("${'$'}120", "per night", "${'$'}150", {direction: "down", value: 20}), button: Button("Reserve", @ToAssistant("Reserve the Grand Hotel"))}), CompositeCardItem(null, Text(null, "Hostel"), [Text(null, "Shared rooms")], {price: BoldText(null, "${'$'}30")})], "grid")])
            """.trimIndent(),
            onAction = actions::add,
        )

        compose.onNodeWithText("Grand Hotel").assertExists()
        compose.onNodeWithText("Hotel front").assertExists()
        compose.onNodeWithText("Sea view").assertExists()
        compose.onNodeWithText("Free breakfast").assertExists()
        compose.onNodeWithText("Pool").assertExists()
        compose.onNodeWithText("Check-in").assertExists()
        compose.onNodeWithText("Room service").assertExists()
        compose.onNodeWithText("$150").assertExists()
        compose.onNodeWithText("Hostel").assertExists()
        compose.onNodeWithText("$30").assertExists()

        compose.onNodeWithText("Reserve").performClick()
        compose.waitForIdle()

        assertEquals(listOf("Reserve the Grand Hotel"), actions.map(ActionEvent::humanFriendlyMessage))
    }

    @Test
    fun `visual cards show their tag and body over the image`() {
        compose.renderProgram(
            """
            root = Stack([VisualCardBlock([VisualCardItem(BoldText(null, "Lisbon", "Portugal"), "lis", "not a link", Tag("Trending"), "Tram on a hill"), VisualCardItem(BoldText(null, "Porto"))], "carousel", false, @ToAssistant("Plan a trip"))])
            """.trimIndent(),
            onAction = actions::add,
        )

        compose.onNodeWithText("Lisbon").assertExists()
        compose.onNodeWithText("Portugal").assertExists()
        compose.onNodeWithText("Trending").assertExists()

        compose.onNodeWithText("Lisbon").performClick()
        compose.waitForIdle()

        assertEquals(listOf("Plan a trip"), actions.map(ActionEvent::humanFriendlyMessage))
    }

    @Test
    fun `cards without an action are not tappable`() {
        compose.renderProgram(
            """
            root = Stack([SnippetCardBlock([SnippetCardItem(null, IconText(Icon("wifi"), null, null, "Wifi"))])])
            """.trimIndent(),
            onAction = actions::add,
        )

        compose.onNodeWithText("Wifi").performClick()
        compose.waitForIdle()

        assertEquals(emptyList<ActionEvent>(), actions)
    }
}
