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

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChartComponentsTest {

    @get:Rule
    val compose = createComposeRule()

    private val series = """[Series("Sales", [10, 20, 15]), Series("Costs", [5, 8, 12])]"""
    private val months = """["Jan", "Feb", "Mar"]"""
    private val summary = "Sales: Jan 10, Feb 20, Mar 15. Costs: Jan 5, Feb 8, Mar 12"

    @Test
    fun `grouped and stacked bar charts describe their series and show a legend`() {
        compose.renderProgram(
            """
            root = Stack([BarChart($months, $series, "grouped", "Month", "Amount", 200), BarChart($months, $series, "stacked")])
            """.trimIndent(),
        )

        compose.onAllNodesWithContentDescription(summary).assertCountEquals(2)
        compose.onNodeWithText("Month").assertExists()
        compose.onNodeWithText("Amount").assertExists()
    }

    @Test
    fun `line and area charts describe their series in every curve style`() {
        compose.renderProgram(
            """
            root = Stack([LineChart($months, $series, "linear"), LineChart($months, $series, "natural"), LineChart($months, $series, "step"), AreaChart($months, $series, "natural", "Month"), AreaChart($months, $series, "step")])
            """.trimIndent(),
        )

        compose.onAllNodesWithContentDescription(summary).assertCountEquals(5)
        compose.onNodeWithText("Month").assertExists()
    }

    @Test
    fun `horizontal bar charts describe their series`() {
        compose.renderProgram(
            """
            root = Stack([HorizontalBarChart($months, $series, "grouped", "Amount", "Month"), HorizontalBarChart($months, $series, "stacked")])
            """.trimIndent(),
        )

        compose.onAllNodesWithContentDescription(summary).assertCountEquals(2)
    }

    @Test
    fun `a single series chart shows no legend`() {
        compose.renderProgram(
            """
            root = BarChart(["Jan", "Feb"], [Series("Sales", [1, -2])])
            """.trimIndent(),
        )

        compose.onNodeWithContentDescription("Sales: Jan 1, Feb -2").assertExists()
        compose.onNodeWithText("Sales").assertDoesNotExist()
    }

    @Test
    fun `a radar chart draws one shape per series`() {
        compose.renderProgram(
            """
            root = RadarChart(["Speed", "Power", "Range"], [Series("Car A", [3, 4, 5]), Series("Car B", [5, 2, 4])])
            """.trimIndent(),
        )

        compose.onNodeWithText("Car A").assertExists()
        compose.onNodeWithText("Car B").assertExists()
    }

    @Test
    fun `pie and donut charts list each slice with its share`() {
        compose.renderProgram(
            """
            root = Stack([PieChart(["Rent", "Food"], [750, 250]), PieChart(["Rent", "Food"], [750, 250], "donut", "semiCircular")])
            """.trimIndent(),
        )

        compose.onAllNodesWithContentDescription("Rent 750, Food 250").assertCountEquals(2)
        compose.onNodeWithText("Total").assertExists()
        compose.onNodeWithText("1,000").assertExists()
    }

    @Test
    fun `radial and single stacked bar charts list their slices`() {
        compose.renderProgram(
            """
            root = Stack([RadialChart(["Done", "Open"], [70, 30]), SingleStackedBarChart(["Done", "Open"], [70, 30])])
            """.trimIndent(),
        )

        compose.onAllNodesWithContentDescription("Done 70, Open 30").assertCountEquals(2)
    }

    @Test
    fun `a scatter chart describes its points`() {
        compose.renderProgram(
            """
            root = ScatterChart([ScatterSeries("Group A", [Point(1, 2), Point(3, 4, 5)]), ScatterSeries("Group B", [Point(2, 1)])], "Height", "Weight")
            """.trimIndent(),
        )

        compose.onNodeWithContentDescription("Group A: (1, 2), (3, 4). Group B: (2, 1)").assertExists()
        compose.onNodeWithText("Height").assertExists()
        compose.onNodeWithText("Weight").assertExists()
    }

    @Test
    fun `charts drawn while streaming still describe their data`() {
        compose.renderProgram(
            """
            root = Stack([LineChart($months, $series), PieChart(["Rent", "Food"], [750, 250])])
            """.trimIndent(),
            isStreaming = true,
        )

        compose.onNodeWithContentDescription(summary).assertExists()
    }
}
