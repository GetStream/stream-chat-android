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

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The components of a surface come in as they show after the one around it, while the agent
 * writes the reply. A component on its way in sits lower than its place, so where it is tells
 * whether it is coming in.
 */
@RunWith(AndroidJUnit4::class)
internal class EntranceTest {

    @get:Rule
    val rule = createComposeRule()

    private val surfaces = SurfaceRule(rule)

    private fun text(id: String, text: String) = mapOf("id" to id, "component" to "Text", "text" to text)

    private fun column(id: String, vararg children: String) =
        mapOf("id" to id, "component" to "Column", "children" to children.toList())

    private fun add(vararg components: Map<String, Any?>) = rule.runOnUiThread {
        surfaces.processor.handle(
            mapOf(
                "version" to "1.0",
                "updateComponents" to mapOf("surfaceId" to "s", "components" to components.toList()),
            ),
        )
        // The clock is held, so the change goes to the composition by hand.
        Snapshot.sendApplyNotifications()
    }

    // Where the top of a component is, in dp.
    private fun top(id: String): Float {
        val node = rule.onNodeWithTag(id).fetchSemanticsNode()
        return node.positionInRoot.y / rule.density.density
    }

    private fun frames(count: Int = 3) = repeat(count) { rule.mainClock.advanceTimeByFrame() }

    private fun settle() = rule.mainClock.advanceTimeBy(2_000)

    private fun showStreaming() {
        surfaces.create(listOf(column("root", "first"), text("first", "First")))
        rule.mainClock.autoAdvance = false
        surfaces.show(entrances = true, isStreaming = true)
        // The reply's UI comes in first, with what shows with it.
        settle()
    }

    @Test
    fun `a component that shows after the one around it comes in on its own, and what was there stays`() {
        showStreaming()
        val first = top("first")

        add(column("root", "first", "second"), text("second", "Second"))
        frames()
        val coming = top("second")
        settle()

        assertEquals("what showed with the surface doesn't move", first, top("first"), 0.5f)
        assertTrue("the new component rises into place", coming - top("second") > 8f)
    }

    @Test
    fun `a component that shows with a new one around it comes in with it`() {
        showStreaming()

        add(column("root", "first", "group"), column("group", "inner"), text("inner", "Inner"))
        frames()
        val groupComing = top("group")
        val innerInGroup = top("inner") - groupComing
        settle()

        assertTrue("the new group rises into place", groupComing - top("group") > 8f)
        assertEquals("its child moves with it", innerInGroup, top("inner") - top("group"), 0.5f)
    }
}
