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

package io.getstream.chat.android.ai.a2ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.a2ui.core.A2UIJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class LayoutAndAccessibilityTest {

    @get:Rule
    val rule = createComposeRule()

    private val surfaces = SurfaceRule(rule)

    private fun bound(path: String) = mapOf("@path" to path)

    // How wide a component is, in dp.
    private fun width(id: String): Float =
        rule.onNodeWithTag(id).fetchSemanticsNode().size.width / rule.density.density

    // How tall a component is, in dp.
    private fun height(id: String): Float =
        rule.onNodeWithTag(id).fetchSemanticsNode().size.height / rule.density.density

    @Test
    fun `a stretched row with tabs in it settles`() {
        surfaces.create(
            listOf(
                mapOf("id" to "root", "component" to "Row", "children" to listOf("tabs", "name")),
                mapOf("id" to "tabs", "component" to "Tabs", "tabs" to listOf(mapOf("title" to "One", "child" to "one"))),
                mapOf("id" to "one", "component" to "Text", "text" to "First"),
                mapOf("id" to "name", "component" to "Text", "text" to "Ada"),
            ),
        )
        surfaces.show()

        rule.waitForIdle()

        rule.onNodeWithText("First").assertExists()
        assertEquals(height("tabs"), height("name"), 0.5f)
    }

    @Test
    fun `a stretched row gives its children the tallest one's height`() {
        surfaces.create(
            listOf(
                mapOf("id" to "root", "component" to "Column", "children" to listOf("row", "below")),
                mapOf("id" to "row", "component" to "Row", "children" to listOf("short", "tall")),
                mapOf("id" to "short", "component" to "Text", "text" to "One line"),
                mapOf("id" to "tall", "component" to "Text", "text" to "One\n\nTwo\n\nThree\n\nFour"),
                mapOf("id" to "below", "component" to "Text", "text" to "Below"),
            ),
        )
        surfaces.show()

        val tall = height("tall")
        assertTrue("the tall child is taller than one line", tall > 2 * height("below"))
        assertEquals(tall, height("short"), 0.5f)
        assertEquals(tall, height("row"), 0.5f)
    }

    @Test
    fun `a row that doesn't stretch keeps its children's own heights`() {
        surfaces.create(
            listOf(
                mapOf("id" to "root", "component" to "Row", "children" to listOf("short", "tall"), "align" to "start"),
                mapOf("id" to "short", "component" to "Text", "text" to "One line"),
                mapOf("id" to "tall", "component" to "Text", "text" to "One\n\nTwo\n\nThree\n\nFour"),
            ),
        )
        surfaces.show()

        assertTrue(height("tall") > 2 * height("short"))
    }

    @Test
    fun `entrances keep the weights of a row's children`() {
        surfaces.create(
            listOf(
                mapOf("id" to "root", "component" to "Row", "children" to listOf("query", "search")),
                mapOf("id" to "query", "component" to "TextField", "label" to "Query", "value" to bound("/q")),
                mapOf(
                    "id" to "search",
                    "component" to "Button",
                    "child" to "search_label",
                    "action" to mapOf("event" to mapOf("name" to "search")),
                ),
                mapOf("id" to "search_label", "component" to "Text", "text" to "Search"),
            ),
        )
        var entrances by mutableStateOf(false)
        rule.setContent {
            MaterialTheme {
                A2UISurfaceView(surface = surfaces.surface, revision = 0, entrances = entrances)
            }
        }
        val without = width("query") to width("search")

        entrances = true
        rule.waitForIdle()
        val with = width("query") to width("search")

        assertTrue("the button has a width: $without", without.second > 0f)
        assertEquals(without.first, with.first, 0.5f)
        assertEquals(without.second, with.second, 0.5f)
    }

    @Test
    fun `the agent's label and description go on the button a person taps`() {
        surfaces.create(
            listOf(
                mapOf(
                    "id" to "root",
                    "component" to "Button",
                    "child" to "label",
                    "action" to mapOf("event" to mapOf("name" to "book")),
                    "accessibility" to mapOf("label" to "Book a table", "description" to "Sends the booking"),
                ),
                mapOf("id" to "label", "component" to "Text", "text" to "Book"),
            ),
        ).show()

        rule.onNodeWithContentDescription("Book a table")
            .assert(SemanticsMatcher("is the clickable node") { it.config.contains(SemanticsProperties.Role) })
            .assert(
                SemanticsMatcher("has the description as its click label") {
                    it.config.getOrNull(SemanticsActions.OnClick)?.label == "Sends the booking"
                },
            )
            .assert(
                SemanticsMatcher("has no state description") {
                    !it.config.contains(SemanticsProperties.StateDescription)
                },
            )
            .performClick()
        assertEquals("book", surfaces.actions.single().name)
    }

    @Test
    fun `choice options are one group of full-height rows`() {
        surfaces.create(
            listOf(
                mapOf(
                    "id" to "root",
                    "component" to "ChoicePicker",
                    "variant" to "mutuallyExclusive",
                    "options" to listOf(mapOf("label" to "Red", "value" to "red"), mapOf("label" to "Blue", "value" to "blue")),
                    "value" to bound("/color"),
                ),
            ),
        ).show()

        val row = rule.onNodeWithText("Red").fetchSemanticsNode()
        assertTrue("${row.size.height} px", row.size.height >= with(rule.density) { 48.dp.toPx() } - 0.5f)
        rule.onNodeWithText("Red").onParent()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup))
    }

    @Test
    fun `value resolves bindings nested in a list`() {
        surfaces.create(
            listOf(
                mapOf(
                    "id" to "root",
                    "component" to "Text",
                    "text" to listOf(mapOf("label" to bound("/first")), mapOf("label" to "Plain")),
                ),
            ),
            data = mapOf("first" to "Bound"),
        ).show(
            catalog = A2UICatalog.Basic.with("Text") { scope ->
                val labels = A2UIJson.list(scope.value("text")).orEmpty().map { A2UIJson.map(it)?.get("label") }
                Text(labels.joinToString())
            },
        )

        assertEquals(1, rule.onAllNodesWithText("Bound, Plain").fetchSemanticsNodes().size)
    }
}
