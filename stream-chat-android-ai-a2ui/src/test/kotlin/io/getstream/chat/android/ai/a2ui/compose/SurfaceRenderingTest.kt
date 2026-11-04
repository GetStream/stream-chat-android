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

import android.media.MediaPlayer
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.a2ui.compose.icons.A2UIIcons
import io.getstream.chat.android.ai.a2ui.core.A2UIJson
import io.getstream.chat.android.ai.a2ui.core.A2UIProtocol
import io.getstream.chat.android.ai.a2ui.core.BasicCatalog
import io.getstream.chat.android.ai.a2ui.core.RendererError
import io.getstream.chat.android.ai.a2ui.core.RendererMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource

@RunWith(AndroidJUnit4::class)
internal class SurfaceRenderingTest {

    @get:Rule
    val rule = createComposeRule()

    private val surfaces = SurfaceRule(rule)

    private fun bound(path: String) = mapOf("@path" to path)

    private fun option(label: String, value: String) = mapOf("label" to label, "value" to value)

    @Test
    fun `the restaurant list shows a card for every restaurant`() {
        surfaces.load("fixtures/restaurant-list.json").show()

        rule.onNodeWithText("Top 3 restaurants in New York").assertExists()
        listOf("Xi'an Famous Foods", "Han Dynasty", "RedFarm").forEach { rule.onNodeWithText(it).assertExists() }
        assertEquals(3, rule.onAllNodesWithText("Book Now").fetchSemanticsNodes().size)
    }

    @Test
    fun `a button sends its event with the context of its list item`() {
        surfaces.load("fixtures/restaurant-list.json").show()

        rule.onAllNodesWithText("Book Now")[1].performScrollTo().performClick()

        val action = surfaces.actions.single()
        assertEquals("book_restaurant", action.name)
        assertEquals("Han Dynasty", action.context["restaurantName"])
    }

    @Test
    fun `a text field writes to the data model and the form sends what the person entered`() {
        surfaces.load("fixtures/booking-form.json").show()

        rule.onNodeWithTag("booking_dietary").performScrollTo().performTextReplacement("Vegan")
        rule.onNodeWithText("Confirm reservation").performScrollTo().performClick()

        assertEquals("Vegan", surfaces.data("/dietary"))
        assertEquals("Vegan", surfaces.actions.single().context["dietary"])
    }

    @Test
    fun `a button is disabled while its check fails`() {
        surfaces.create(
            listOf(
                mapOf("id" to "root", "component" to "Column", "children" to listOf("email", "send")),
                mapOf("id" to "email", "component" to "TextField", "label" to "Email", "value" to bound("/email")),
                mapOf(
                    "id" to "send",
                    "component" to "Button",
                    "child" to "send_label",
                    "action" to mapOf("event" to mapOf("name" to "send")),
                    "checks" to listOf(
                        mapOf(
                            "condition" to mapOf("@call" to "email", "args" to mapOf("value" to bound("/email"))),
                            "message" to "Enter your email.",
                        ),
                    ),
                ),
                mapOf("id" to "send_label", "component" to "Text", "text" to "Send"),
            ),
            data = mapOf("email" to "nope"),
        ).show()

        rule.onNodeWithText("Enter your email.").assertExists()
        rule.onNodeWithText("Send").assertIsNotEnabled()

        rule.onNodeWithTag("email").performTextReplacement("ada@example.com")

        rule.onNodeWithText("Send").assertIsEnabled().performClick()
        assertEquals("send", surfaces.actions.single().name)
    }

    @Test
    fun `inputs write their values`() {
        surfaces.create(
            listOf(
                mapOf("id" to "root", "component" to "Column", "children" to listOf("agree", "size", "extras")),
                mapOf("id" to "agree", "component" to "CheckBox", "label" to "I agree", "value" to bound("/agree")),
                mapOf(
                    "id" to "size",
                    "component" to "ChoicePicker",
                    "options" to listOf(option("Small", "s"), option("Large", "l")),
                    "value" to mapOf("@path" to "/size"),
                ),
                mapOf(
                    "id" to "extras",
                    "component" to "ChoicePicker",
                    "variant" to "multipleSelection",
                    "displayStyle" to "chips",
                    "options" to listOf(option("Milk", "milk"), option("Sugar", "sugar")),
                    "value" to mapOf("@path" to "/extras"),
                ),
            ),
            data = mapOf("size" to listOf("s"), "extras" to emptyList<String>()),
        ).show()

        rule.onNodeWithText("I agree").performClick()
        rule.onNodeWithText("Large").performClick()
        rule.onNodeWithText("Milk").performClick()
        rule.onNodeWithText("Sugar").performClick()

        assertEquals(true, surfaces.data("/agree"))
        assertEquals(listOf("l"), surfaces.data("/size"))
        assertEquals(listOf("milk", "sugar"), surfaces.data("/extras"))
    }

    @Test
    fun `a slider snaps to its divisions`() {
        surfaces.create(
            listOf(
                mapOf(
                    "id" to "root",
                    "component" to "Slider",
                    "label" to "Guests",
                    "min" to 0,
                    "max" to 10,
                    "steps" to 10,
                    "value" to mapOf("@path" to "/guests"),
                ),
            ),
            data = mapOf("guests" to 2),
        ).show()

        rule.onNodeWithText("Guests: 2").assertExists()
        rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(6.4f) }

        assertEquals("6", A2UIJson.stringify(surfaces.data("/guests")))
    }

    @Test
    fun `tabs show the selected tab's content`() {
        surfaces.create(
            listOf(
                mapOf(
                    "id" to "root",
                    "component" to "Tabs",
                    "tabs" to listOf(
                        mapOf("title" to "Day 1", "child" to "one"),
                        mapOf("title" to "Day 2", "child" to "two"),
                    ),
                ),
                mapOf("id" to "one", "component" to "Text", "text" to "Ohrid old town"),
                mapOf("id" to "two", "component" to "Text", "text" to "St. Naum"),
            ),
        ).show()

        rule.onNodeWithText("Ohrid old town").assertExists()
        rule.onNodeWithText("St. Naum").assertDoesNotExist()

        rule.onNodeWithText("Day 2").performClick()

        rule.onNodeWithText("St. Naum").assertExists()
    }

    @Test
    fun `a modal's button trigger opens its content and still sends its event`() {
        surfaces.create(
            listOf(
                mapOf("id" to "root", "component" to "Modal", "trigger" to "open", "content" to "details"),
                mapOf(
                    "id" to "open",
                    "component" to "Button",
                    "child" to "open_label",
                    "action" to mapOf("event" to mapOf("name" to "opened")),
                ),
                mapOf("id" to "open_label", "component" to "Text", "text" to "Details"),
                mapOf("id" to "details", "component" to "Text", "text" to "Open daily from 9"),
            ),
        ).show()

        rule.onNodeWithText("Open daily from 9").assertDoesNotExist()
        rule.onNodeWithText("Details").performClick()

        rule.onNodeWithText("Open daily from 9").assertExists()
        assertEquals("opened", surfaces.actions.single().name)
    }

    @Test
    fun `markdown text shows without its markers and caption text is plain`() {
        surfaces.create(
            listOf(
                mapOf("id" to "root", "component" to "Column", "children" to listOf("title", "note")),
                mapOf("id" to "title", "component" to "Text", "text" to "## Weekend in **Ohrid**"),
                mapOf("id" to "note", "component" to "Text", "text" to "Prices may change", "variant" to "caption"),
            ),
        ).show()

        rule.onNodeWithText("Weekend in Ohrid", substring = true).assertExists()
        rule.onNodeWithText("Prices may change").assertIsDisplayed()
    }

    @Test
    fun `accessibility attributes label a component and hide another`() {
        surfaces.create(
            listOf(
                mapOf("id" to "root", "component" to "Column", "children" to listOf("star", "decoration")),
                mapOf(
                    "id" to "star",
                    "component" to "Icon",
                    "name" to "star",
                    "accessibility" to mapOf("label" to bound("/rating")),
                ),
                mapOf(
                    "id" to "decoration",
                    "component" to "Text",
                    "text" to "~~~",
                    "accessibility" to mapOf("hidden" to true),
                ),
            ),
            data = mapOf("rating" to "Five stars"),
        ).show()

        rule.onNodeWithContentDescription("Five stars").assertExists()
        rule.onNodeWithText("~~~").assertDoesNotExist()
    }

    @Test
    fun `a catalog draws a type its own way`() {
        surfaces.create(listOf(mapOf("id" to "root", "component" to "Text", "text" to "Hello"))).show(
            catalog = A2UICatalog.Basic.with("Text") { scope -> Text("Custom: ${scope.string("text")}") },
        )

        rule.onNodeWithText("Custom: Hello").assertExists()
    }

    @Test
    fun `a tree whose shared children multiply past the limit shows nothing and is reported`() {
        // Each level lists the next one twice: 2^30 components from 31 definitions.
        val levels = (0 until 30).map { level ->
            mapOf(
                "id" to if (level == 0) "root" else "c$level",
                "component" to "Column",
                "children" to listOf("c${level + 1}", "c${level + 1}"),
            )
        } + mapOf("id" to "c30", "component" to "Text", "text" to "leaf")
        surfaces.create(levels)

        assertTrue(surfaces.surface.expandedSize(A2UIProtocol.MAX_EXPANDED_COMPONENTS) > A2UIProtocol.MAX_EXPANDED_COMPONENTS)
        surfaces.show()

        rule.onNodeWithTag("root").assertDoesNotExist()
        val errors = surfaces.sent.filterIsInstance<RendererMessage.Error>().map { it.error.code }
        assertEquals(listOf(RendererError.RECURSION_LIMIT_EXCEEDED), errors)
    }

    @Test
    fun `an ordinary tree expands to its own size`() {
        surfaces.create(
            listOf(
                mapOf("id" to "root", "component" to "Column", "children" to listOf("a", "b")),
                mapOf("id" to "a", "component" to "Text", "text" to "a"),
                mapOf("id" to "b", "component" to "Row", "children" to listOf("c")),
                mapOf("id" to "c", "component" to "Text", "text" to "c"),
            ),
        ).show()

        assertEquals(4, surfaces.surface.expandedSize(100))
        rule.onNodeWithText("c").assertExists()
    }

    @Test
    fun `a change of the data model shows at once`() {
        surfaces.create(
            listOf(mapOf("id" to "root", "component" to "Text", "text" to mapOf("@path" to "/status"))),
            data = mapOf("status" to "Booking…"),
        ).show()

        rule.runOnIdle {
            surfaces.processor.handle(
                mapOf(
                    "version" to "1.0",
                    "updateDataModel" to mapOf("surfaceId" to "s", "path" to "/status", "value" to "Booked"),
                ),
            )
        }

        rule.onNodeWithText("Booked").assertExists()
    }

    @Test
    fun `every icon name of the basic catalog has an icon`() {
        val icon = BasicCatalog.catalog.component("Icon")!!.properties["name"]!!
        val names = A2UIJson.list(icon["oneOf"])!!
            .mapNotNull { A2UIJson.map(it)?.get("enum") }
            .flatMap { A2UIJson.list(it)!! }

        assertTrue(names.isNotEmpty())
        assertEquals(emptyList<Any?>(), names.filterNot { it in A2UIIcons.names })
        assertTrue(A2UIIcons.fromSvgPath("M6 6h12v12H6z") != null)
        assertEquals(null, A2UIIcons.fromSvgPath("not a path ("))
    }

    @Test
    fun `the date and time input writes ISO 8601 values`() {
        val utc = java.util.TimeZone.getTimeZone("UTC")
        val berlin = java.util.TimeZone.getTimeZone("GMT+02:00")
        val day = 1_790_294_400_000L // 2026-09-25, midnight UTC

        assertEquals("2026-09-25", DateTimeValues.format(day, 0, 0, utc, DateTimeKind.Date))
        assertEquals("19:30:00", DateTimeValues.format(null, 19, 30, utc, DateTimeKind.Time))
        assertEquals("2026-09-25T19:30:00+02:00", DateTimeValues.format(day, 19, 30, berlin, DateTimeKind.DateTime))
        assertEquals("2026-09-25T19:30:00Z", DateTimeValues.format(day, 19, 30, utc, DateTimeKind.DateTime))
    }

    @Test
    fun `an audio player that failed to prepare starts again on the next tap`() {
        val url = "https://example.com/song.mp3"
        // The audio never finishes preparing on its own, so the test decides when it fails.
        ShadowMediaPlayer.addMediaInfo(DataSource.toDataSource(url), ShadowMediaPlayer.MediaInfo(1_000, -1))
        val players = mutableListOf<ShadowMediaPlayer>()
        ShadowMediaPlayer.setCreateListener { _, shadow -> players.add(shadow) }
        surfaces.create(listOf(mapOf("id" to "root", "component" to "AudioPlayer", "url" to url))).show()

        rule.onNodeWithContentDescription("Play").performClick()
        rule.runOnIdle { players.single().invokeErrorListener(MediaPlayer.MEDIA_ERROR_UNKNOWN, 0) }
        rule.onNodeWithContentDescription("Play").performClick()

        rule.runOnIdle {
            assertEquals(2, players.size)
            assertEquals(ShadowMediaPlayer.State.END, players[0].state)
            assertEquals(ShadowMediaPlayer.State.PREPARING, players[1].state)
        }
    }
}
