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

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.openui.lang.ActionEvent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class FormComponentsTest {

    @get:Rule
    val compose = createComposeRule()

    private val actions = mutableListOf<ActionEvent>()

    @Test
    fun `a primary button checks the required fields before it sends the form`() {
        compose.renderProgram(
            """
            root = Form("signup", Buttons([Button("Send", @ToAssistant("Signed up"), "primary")]), [FormControl("Email", Input("email", "you@example.com", "email", {required: true, email: true}), "We never share it")])
            """.trimIndent(),
            onAction = actions::add,
        )
        compose.onNodeWithText("Send").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("This field is required").assertExists()
        assertEquals(emptyList<ActionEvent>(), actions)

        compose.onNodeWithText("you@example.com").performTextInput("ann@example.com")
        compose.onNodeWithText("Send").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("This field is required").assertDoesNotExist()
        assertEquals(listOf("Signed up"), actions.map(ActionEvent::humanFriendlyMessage))
    }

    @Test
    fun `text inputs write to their bound variables`() {
        compose.renderProgram(
            """
            ${'$'}name = ""
            ${'$'}bio = ""
            root = Stack([Input("name", "Your name", "text", null, ${'$'}name), TextArea("bio", "About you", 3, null, ${'$'}bio), TextContent("Hello " + ${'$'}name + ", " + ${'$'}bio)])
            """.trimIndent(),
        )

        compose.onNodeWithText("Your name").performTextInput("Ann")
        compose.onNodeWithText("About you").performTextInput("Likes maps")
        compose.waitForIdle()

        compose.onNodeWithText("Hello Ann, Likes maps").assertExists()
    }

    @Test
    fun `a password input hides what the user types`() {
        compose.renderProgram(
            """
            root = Input("secret", "Password", "password")
            """.trimIndent(),
        )

        compose.onNodeWithText("Password").performTextInput("hunter2")
        compose.waitForIdle()

        compose.onNodeWithText("\u2022".repeat("hunter2".length)).assertExists()
    }

    @Test
    fun `a select shows its placeholder and stores the picked option`() {
        compose.renderProgram(
            """
            ${'$'}city = ""
            root = Stack([Select("city", [SelectItem("lis", "Lisbon"), SelectItem("por", "Porto")], "Pick a city", {required: true}, ${'$'}city, "small"), TextContent("City: " + ${'$'}city)])
            """.trimIndent(),
        )

        compose.onNodeWithText("Pick a city").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Porto").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("City: por").assertExists()
        compose.onNodeWithText("Porto").assertExists()
    }

    @Test
    fun `a date field stores the day picked in the dialog`() {
        compose.renderProgram(
            """
            root = DatePicker("day", "single")
            """.trimIndent(),
        )

        compose.onNodeWithText("Date").assertExists()
        compose.onNodeWithText("Choose").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("OK").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Choose").assertDoesNotExist()
    }

    @Test
    fun `cancelling the date dialog keeps the range`() {
        compose.renderProgram(
            """
            ${'$'}trip = {from: "2026-05-10", to: "2026-05-12"}
            root = Stack([DatePicker("trip", "range", {required: true}, ${'$'}trip), TextContent("Trip: " + ${'$'}trip.from + " to " + ${'$'}trip.to)])
            """.trimIndent(),
        )

        compose.onNodeWithText("From").assertExists()
        compose.onNodeWithText("To").assertExists()
        compose.onNodeWithText("Trip: 2026-05-10 to 2026-05-12").assertExists()

        compose.onNodeWithText("May 10, 2026").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Cancel").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Trip: 2026-05-10 to 2026-05-12").assertExists()
    }

    @Test
    fun `a slider stores the value it is moved to`() {
        compose.renderProgram(
            """
            ${'$'}budget = [20]
            root = Stack([Slider("budget", "discrete", 0, 100, 10, [20], "Budget", null, ${'$'}budget), TextContent("Budget: " + ${'$'}budget[0])])
            """.trimIndent(),
        )

        compose.onNodeWithText("Budget").assertDoesNotExist()
        compose.onNodeWithContentDescription("Budget").performSemanticsAction(SemanticsActions.SetProgress) { it(60f) }
        compose.waitForIdle()

        compose.onNodeWithText("Budget: 60").assertExists()
    }

    @Test
    fun `a range slider shows both thumbs`() {
        compose.renderProgram(
            """
            root = Slider("price", "continuous", 0, 500, null, [100, 300], "Price")
            """.trimIndent(),
        )

        compose.onNodeWithContentDescription("Price, minimum").assertExists()
        compose.onNodeWithContentDescription("Price, maximum").assertExists()
    }

    @Test
    fun `check boxes and switches toggle their items`() {
        compose.renderProgram(
            """
            root = Stack([CheckBoxGroup("extras", [CheckBoxItem("Wifi", "Fast internet", "wifi", true), CheckBoxItem("Parking", "On site", "parking")], {required: true}), SwitchGroup("alerts", [SwitchItem("Email", "Weekly digest", "email"), SwitchItem("Push", null, "push", true)], "card")])
            """.trimIndent(),
        )

        compose.onNodeWithText("Wifi").assertIsOn()
        compose.onNodeWithText("Parking").assertIsOff()
        compose.onNodeWithText("Parking").performClick()
        compose.onNodeWithText("Parking").assertIsOn()

        compose.onNodeWithText("Email").assertIsOff()
        compose.onNodeWithText("Email").performClick()
        compose.onNodeWithText("Email").assertIsOn()
        compose.onNodeWithText("Push").assertIsOn()
    }

    @Test
    fun `a radio group selects one option`() {
        compose.renderProgram(
            """
            root = RadioGroup("plan", [RadioItem("Basic", "Free", "basic"), RadioItem("Pro", "Paid", "pro")], "basic", {required: true})
            """.trimIndent(),
        )

        compose.onNodeWithText("Basic").assertIsSelected()
        compose.onNodeWithText("Pro").performClick()
        compose.onNodeWithText("Pro").assertIsSelected()
    }

    @Test
    fun `chips select one or many options and skip disabled ones`() {
        compose.renderProgram(
            """
            root = Stack([Chips("diet", "multiple", [ChipItem("veg", "Vegan", Icon("leaf")), ChipItem("gf", "Gluten free"), ChipItem("raw", "Raw", null, true)], {required: true}), Chips("size", "single", [ChipItem("s", "Small"), ChipItem("l", "Large")], null, "s")])
            """.trimIndent(),
        )

        compose.onNodeWithText("Vegan").performClick()
        compose.onNodeWithText("Gluten free").performClick()
        compose.onNodeWithText("Raw").performClick()
        compose.onNodeWithText("Vegan").assertIsOn()
        compose.onNodeWithText("Gluten free").assertIsOn()
        compose.onNodeWithText("Raw").assertIsOff()

        compose.onNodeWithText("Small").assertIsSelected()
        compose.onNodeWithText("Large").performClick()
        compose.onNodeWithText("Large").assertIsSelected()
    }

    @Test
    fun `option cards select a card with an icon or image on top`() {
        compose.renderProgram(
            """
            root = OptionCards("room", "single", [OptionCard("std", "Standard", "One bed", Icon("bed")), OptionCard("suite", "Suite", "Sea view", Image("Suite photo", "not a link")), OptionCard("loft", "Loft", null, null, true)])
            """.trimIndent(),
        )

        compose.onNodeWithText("Standard").performClick()
        compose.onNodeWithText("Standard").assertIsSelected()

        compose.onNodeWithText("Loft").performClick()
        compose.onNodeWithText("Loft").assertIsNotSelected()
        compose.onNodeWithText("Standard").assertIsSelected()
    }
}
