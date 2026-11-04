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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.a2ui.core.Accessibility
import io.getstream.chat.android.ai.a2ui.core.Node
import io.getstream.chat.android.ai.a2ui.core.NodeContext
import io.getstream.chat.android.ai.a2ui.core.Surface
import io.getstream.chat.android.ai.a2ui.core.rootNode
import io.getstream.chat.android.ai.compose.generativeui.AIUIEntrance
import io.getstream.chat.android.ai.compose.generativeui.AIUIPartEntrance

/**
 * Draws a surface from its root, with [catalog]. [revision] changes whenever the surface's
 * components or data do, so the surface draws again.
 *
 * @param entrances Whether the surface comes in as a part of a reply's generated UI does, and its
 * components with it: a component that shows after the one around it comes in on its own.
 */
@Composable
internal fun A2UISurfaceView(
    surface: Surface,
    revision: Int,
    modifier: Modifier = Modifier,
    catalog: A2UICatalog = A2UICatalog.Basic,
    entrances: Boolean = false,
) {
    val root = surface.rootNode() ?: return
    val environment = SurfaceEnvironment(surface, catalog, entrances, revision)
    Box(modifier = modifier.fillMaxWidth().testTag("a2ui:${surface.surfaceId}")) {
        if (entrances) {
            AIUIEntrance {
                RenderNode(environment, root, Modifier)
            }
        } else {
            RenderNode(environment, root, Modifier)
        }
    }
}

/** Draws one node with its type's renderer, applying its accessibility attributes. */
@Composable
internal fun RenderNode(environment: SurfaceEnvironment, node: Node, modifier: Modifier) {
    val renderer = environment.catalog[node.component.type] ?: return
    val context = NodeContext(environment.surface, node)
    val attributes = context.accessibility()
    fun scope(entrance: Modifier) = A2UIComponentScope(
        context = context,
        layout = modifier.then(entrance).hiddenIf(attributes?.hidden == true).testTag(node.component.id),
        accessibility = Modifier.accessibility(attributes),
        clickLabel = attributes?.description?.takeUnless { attributes.hidden },
        environment = environment,
    )
    if (environment.entrances) {
        AIUIPartEntrance { entrance -> renderer(scope(entrance)) }
    } else {
        renderer(scope(Modifier))
    }
}

// A hidden component and everything in it are left out of the accessibility tree.
private fun Modifier.hiddenIf(hidden: Boolean): Modifier = if (hidden) clearAndSetSemantics { } else this

// The agent's label and live region, for the node a person interacts with. Its description says
// what using the component does, so it is the click label of that node instead.
private fun Modifier.accessibility(attributes: Accessibility?): Modifier {
    if (attributes == null || attributes.hidden) return this
    if (attributes.label == null && attributes.live !in LiveRegions) return this
    return semantics {
        attributes.label?.let { contentDescription = it }
        when (attributes.live) {
            "polite" -> liveRegion = LiveRegionMode.Polite
            "assertive" -> liveRegion = LiveRegionMode.Assertive
        }
    }
}

private val LiveRegions = setOf("polite", "assertive")

/**
 * The node a person interacts with, labelled for accessibility: the agent's label, and its
 * description as what a tap does. [action] is what the tap does, as the node's own action.
 */
internal fun Modifier.interactive(scope: A2UIComponentScope, action: (() -> Unit)? = null): Modifier {
    val label = scope.clickLabel
    val labelled = then(scope.accessibility)
    if (label == null || action == null) return labelled
    return labelled.semantics {
        onClick(label = label) {
            action()
            true
        }
    }
}

/** Opens the modal whose trigger holds the composition, such as a button that opens it. */
internal val LocalModalOpener: ProvidableCompositionLocal<(() -> Unit)?> = staticCompositionLocalOf { null }

/** The space around leaf components. Containers add none, so nesting them doesn't add up space. */
internal val LeafMargin: Dp = 4.dp

/** The messages of a component's checks that fail, under the component. */
@Composable
internal fun ValidationMessages(messages: List<String>, modifier: Modifier = Modifier) {
    if (messages.isEmpty()) return
    Column(modifier = modifier) {
        messages.forEach { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
