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

package io.getstream.chat.android.ai.a2ui.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Every example published with the A2UI 1.0 basic catalog (`catalogs/basic/v1/examples`)
 * processes without an error, and its tree is whole and resolves.
 */
@RunWith(Parameterized::class)
internal class BasicCatalogExamplesTest(private val file: String) {

    @Test
    fun `example processes cleanly`() {
        val example = A2UIJson.map(Fixtures.json("examples/$file"))!!
        val harness = Harness(listOf(BasicCatalog.catalog))
        harness.process(A2UIJson.list(example["messages"])!!)
        // An incremental example replaces placeholders, which then stay behind unreachable.
        harness.processor.validateTopology(TopologyValidator(allowOrphans = true))
        assertEquals("Errors in $file", emptyList<RendererError>(), harness.errors)
        assertTrue(harness.processor.surfaces.isNotEmpty())
        harness.processor.surfaces.values.forEach { surface ->
            val root = surface.rootNode()
            assertNotNull("No root in ${surface.surfaceId}", root)
            renderAll(surface, root!!)
            assertEquals("Errors while resolving $file", emptyList<RendererError>(), harness.errors)
        }
    }

    // Reads every property of every node of the tree, as rendering does.
    private fun renderAll(surface: Surface, node: Node) {
        val context = NodeContext(surface, node)
        node.component.properties.keys.forEach { context.value(it) }
        context.failingChecks()
        val definition = surface.definition(node.component)
        definition?.childReferences(node.component).orEmpty()
            .map { it.first.removePrefix("/").substringBefore('/') }.toSet()
            .forEach { property ->
                when (ChildList.of(node.component.properties[property])) {
                    ChildList.None -> context.child(property)?.let { renderAll(surface, it) }
                    else -> context.children(property).forEach { renderAll(surface, it) }
                }
            }
        A2UIJson.list(node.component.properties["tabs"]).orEmpty().forEachIndexed { index, tab ->
            (A2UIJson.map(tab)?.get("child") as? String)
                ?.let { context.childNode(it, "tab$index") }
                ?.let { renderAll(surface, it) }
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun examples(): List<String> = Fixtures.text("examples/index.txt").lines().filter { it.endsWith(".json") }
    }
}
