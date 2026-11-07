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

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.a2ui.core.A2UIProtocol
import io.getstream.chat.android.ai.a2ui.core.BasicCatalog
import io.getstream.chat.android.ai.a2ui.core.Harness
import io.getstream.chat.android.ai.a2ui.core.MessageProcessor
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ContentColorTest {

    @get:Rule
    val rule = createComposeRule()

    private val scheme = darkColorScheme()

    private val processor = MessageProcessor(
        catalogs = listOf(BasicCatalog.catalog),
        defaultCatalogId = A2UIProtocol.BASIC_CATALOG_ID,
        services = Harness.testServices(),
    ) {}.also { processor ->
        processor.handle(
            mapOf(
                "version" to "1.0",
                "createSurface" to mapOf(
                    "surfaceId" to "s",
                    "components" to listOf(
                        mapOf("id" to "root", "component" to "Column", "children" to listOf("title", "book")),
                        mapOf("id" to "title", "component" to "Text", "text" to "Day 1"),
                        mapOf(
                            "id" to "book",
                            "component" to "Button",
                            "child" to "label",
                            "variant" to "primary",
                            "action" to mapOf("event" to mapOf("name" to "book")),
                        ),
                        mapOf("id" to "label", "component" to "Text", "text" to "Book"),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `text outside a material surface takes the theme's color, not black`() {
        show()

        assertEquals(scheme.onSurface, color("Day 1"))
    }

    @Test
    fun `text takes the color the app sets around the surface`() {
        show { content -> CompositionLocalProvider(LocalContentColor provides Color.Red) { content() } }

        assertEquals(Color.Red, color("Day 1"))
    }

    @Test
    fun `text on a material surface takes the surface's color`() {
        show { content -> Surface(color = scheme.primaryContainer) { content() } }

        assertEquals(scheme.onPrimaryContainer, color("Day 1"))
    }

    @Test
    fun `a button's label keeps the button's color`() {
        show()

        assertEquals(scheme.onPrimary, color("Book"))
    }

    private fun show(around: @Composable (@Composable () -> Unit) -> Unit = { it() }) {
        rule.setContent {
            MaterialTheme(colorScheme = scheme) {
                around { A2UISurfaceView(surface = processor.surfaces.values.single(), revision = 0) }
            }
        }
    }

    private fun color(text: String): Color {
        val results = mutableListOf<TextLayoutResult>()
        val node = rule.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode()
        node.config[SemanticsActions.GetTextLayoutResult].action!!(results)
        return results.single().layoutInput.style.color
    }
}
