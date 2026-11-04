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

package io.getstream.chat.android.ai.a2ui.generativeui

import io.getstream.chat.android.ai.a2ui.core.A2UIJson
import io.getstream.chat.android.ai.a2ui.core.Catalog

/**
 * What an agent is told so that its model writes replies' UI in A2UI: how a reply carries the UI,
 * the protocol, the catalog's guidelines, and the catalog it writes with, the app's components
 * among its own.
 *
 * @param catalog The catalog the model writes with.
 * @param fence The info string of the fenced block a reply's UI is written in.
 * @param preamble What the app tells the model before A2UI's own instructions.
 * @param tools The names of the app's tools the UI can call.
 */
internal class A2UIInstructions(
    private val catalog: Catalog,
    private val fence: String,
    private val preamble: String? = null,
    private val tools: List<String> = emptyList(),
) {

    /** The instructions, in Markdown. */
    val text: String
        get() = buildList {
            preamble?.takeIf { it.isNotEmpty() }?.let(::add)
            add(writing())
            add(PROTOCOL_OVERVIEW)
            catalog.instructions?.takeIf { it.isNotEmpty() }?.let(::add)
            if (tools.isNotEmpty()) add(TOOLS_INTRODUCTION + "\n\n" + tools.joinToString("\n") { "- `$it`" })
            add("## The catalog `${catalog.catalogId}`\n\n```json\n${document()}\n```")
        }.joinToString("\n\n")

    private fun writing(): String = """
        ## Writing UI in A2UI

        Write a reply's UI in one fenced block whose info string is `$fence`, after one short sentence saying what it shows, which is what people read wherever the UI can't show. In the block, write A2UI messages, one JSON object per line, and nothing else:

        1. First a `createSurface` with a `surfaceId` new to this reply, the `catalogId` `${catalog.catalogId}`, and the `dataModel` the UI's inputs start from.
        2. Then `updateComponents` messages for that surface with a few components each, `root` first, so the UI shows as you write it.

        A reply's UI is shown as you wrote it: never write `callRendererFunction` or `agentFunctionResponse`, and never change an earlier reply's UI, but write a new one. What the person does in it comes back as their next message, which says what the button they tapped says. Give every event action a `userMessage` saying what it asks of you, such as "Update the plan", which comes back with it, and a `context` with what you need to know, bound to the data model.
    """.trimIndent()

    // The catalog as one line of JSON, its keys sorted so the same catalog always reads the same.
    // Its guidelines are a section of their own.
    private fun document(): String = A2UIJson.write(catalog.toJson() - "instructions", sortKeys = true)

    private companion object {
        val TOOLS_INTRODUCTION = """
            ## The app's tools

            These run on the person's device. Call one as you call a function the catalog doesn't define, with `{"@call": "<name>", "args": {…}}`, and its output is the call's value.
        """.trimIndent()

        val PROTOCOL_OVERVIEW = """
            # Generating user interfaces with A2UI v1.0

            You render user interfaces by emitting A2UI messages. Every message is a JSON object with a `"version": "1.0"` field plus exactly one of these keys:

            - `createSurface`: creates a surface. Requires `surfaceId`, and accepts `catalogId`, `sendDataModel`, `components` and `dataModel` so a complete UI can be sent in one message.
            - `updateComponents`: adds or replaces components of an existing surface. Requires `surfaceId` and `components`.
            - `updateDataModel`: writes to the surface data model. Requires `surfaceId` and `value`, and accepts a JSON Pointer `path`. Setting `value` to `null` deletes the entry at `path`.
            - `deleteSurface`: removes a surface. Requires `surfaceId`.
            - `callRendererFunction`: asks the renderer to run one of its functions.
            - `agentFunctionResponse`: answers a `callAgentFunction` request from the renderer.

            Rules:

            1. Components are a flat adjacency list. Each component has a unique `id`, a `component` type, and its properties inline. Exactly one component must have the id `root`. An id is an identifier: letters, digits and underscores, not starting with a digit, such as `saturday_plan` (never `saturday-plan`).
            2. Children are referenced by id, never nested inline. Use an array of ids, or `{"componentId": "...", "path": "/items"}` to repeat a template for every item of a data model list.
            3. Any property may be a literal, a data binding `{"@path": "/a/b"}` or a function call `{"@call": "name", "args": {...}}`. Inside a list template, paths without a leading slash resolve relative to the current item, and `{"@call": "@index"}` returns the item's position. A literal object key that starts with `@` is written with `@@`, so `"@@type"` means `"@type"`.
            4. Put content that changes in the data model and bind to it, so you can update values with `updateDataModel` instead of resending components.
            5. Interactive components declare an `action`, which either dispatches an event to you (`{"event": {"name": "...", "context": {...}}}`) or runs a renderer function (`{"functionCall": {"@call": "openUrl", "args": {"url": "..."}}}`).
            6. Surface ids must be unique for the lifetime of the renderer. Creating a surface that already exists is an error: delete it first, or send `updateComponents` and `updateDataModel` to change it.
        """.trimIndent()
    }
}
