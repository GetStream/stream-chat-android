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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import io.getstream.chat.android.ai.a2ui.core.A2UIJson
import io.getstream.chat.android.ai.a2ui.core.A2UIProtocol
import io.getstream.chat.android.ai.a2ui.core.BasicCatalog
import io.getstream.chat.android.ai.a2ui.core.DataPath
import io.getstream.chat.android.ai.a2ui.core.Fixtures
import io.getstream.chat.android.ai.a2ui.core.Harness
import io.getstream.chat.android.ai.a2ui.core.MessageProcessor
import io.getstream.chat.android.ai.a2ui.core.RendererMessage
import io.getstream.chat.android.ai.a2ui.core.Surface
import io.getstream.chat.android.ai.compose.generativeui.AIUIDocument
import io.getstream.chat.android.ai.compose.generativeui.AIUIEngine
import io.getstream.chat.android.ai.compose.generativeui.AIUIFormat
import io.getstream.chat.android.ai.compose.generativeui.AIUIHost
import io.getstream.chat.android.ai.compose.generativeui.AIUIRenderer
import io.getstream.chat.android.ai.compose.generativeui.AIUIView

/** One renderer's surfaces drawn by a compose test, with what they sent to the agent. */
internal class SurfaceRule(private val rule: ComposeContentTestRule) {
    val sent = mutableListOf<RendererMessage>()
    private val revision = mutableIntStateOf(0)
    val processor = MessageProcessor(
        catalogs = listOf(BasicCatalog.catalog),
        defaultCatalogId = A2UIProtocol.BASIC_CATALOG_ID,
        services = Harness.testServices(),
    ) { sent.add(it) }.also { processor -> processor.addChangeListener { revision.intValue++ } }

    val actions: List<RendererMessage.Action> get() = sent.filterIsInstance<RendererMessage.Action>()

    val surface: Surface get() = processor.surfaces.values.single()

    /** Applies the messages of a fixture, as `{"messages": [...]}`. */
    fun load(path: String): SurfaceRule = apply {
        val messages = A2UIJson.list(A2UIJson.map(Fixtures.json(path))!!["messages"])!!
        messages.forEach { processor.handle(A2UIJson.map(it)!!) }
    }

    /** Creates a surface with [components] and [data]. */
    fun create(components: List<Map<String, Any?>>, data: Map<String, Any?> = emptyMap()): SurfaceRule = apply {
        processor.handle(
            mapOf(
                "version" to "1.0",
                "createSurface" to mapOf("surfaceId" to "s", "components" to components, "dataModel" to data),
            ),
        )
    }

    /** The data model value at [path]. */
    fun data(path: String): Any? = surface.dataModel.get(DataPath.parse(path))

    /**
     * Draws every surface with [catalog]. With [entrances], they show as a reply's generated UI
     * does, the agent still writing the reply while [isStreaming].
     */
    fun show(catalog: A2UICatalog = A2UICatalog.Basic, entrances: Boolean = false, isStreaming: Boolean = false) {
        rule.setContent {
            MaterialTheme {
                if (entrances) {
                    val host = remember {
                        AIUIHost(renderers = listOf(SurfacesRenderer { Surfaces(catalog, entrances = true) })) {}
                    }
                    val document = AIUIDocument(AIUIFormat.A2UI, isStreaming = isStreaming, content = null)
                    AIUIView(host = host, document = document, messageId = "m")
                } else {
                    Surfaces(catalog, entrances = false)
                }
            }
        }
    }

    @Composable
    private fun Surfaces(catalog: A2UICatalog, entrances: Boolean) {
        val current by revision
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            processor.surfaces.values.forEach { surface ->
                A2UISurfaceView(
                    surface = surface,
                    revision = current,
                    catalog = catalog,
                    entrances = entrances,
                )
            }
        }
    }
}

// Shows the surfaces as a reply's generated UI.
private class SurfacesRenderer(private val content: @Composable () -> Unit) : AIUIRenderer {
    override val format: AIUIFormat = AIUIFormat.A2UI

    override fun makeEngine(host: AIUIHost): AIUIEngine = object : AIUIEngine {
        override fun content(document: AIUIDocument, messageId: String): @Composable () -> Unit = content
    }
}
