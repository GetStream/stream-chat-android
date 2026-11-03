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

package io.getstream.chat.android.ai.openui.lang

import io.getstream.chat.android.ai.openui.library.DefinedAction
import io.getstream.chat.android.ai.openui.library.DefinedFunction

/** Tells the runtime which component props accept two-way `$variable` bindings. */
internal fun interface ReactivePropResolver {
    /** Whether [prop] of [component] accepts a `$variable` binding. */
    fun isReactiveProp(component: String, prop: String): Boolean
}

/**
 * What the language runtime needs to know about a component library: its JSON Schema, its root
 * component, which props take two-way `$variable` bindings, and its own functions and actions.
 *
 * The renderer's component library provides one; [fromSpec] reads upstream's `LibrarySpec`
 * JSON (`library.toSpec()`), so a library defined with `@openuidev/react-lang` can be used as is.
 */
internal class LibrarySchema(
    /** The library JSON Schema (`library.toJSONSchema()`), components in `$defs`. */
    val jsonSchema: OpenUIValue,
    /** The root component, e.g. `Card` for the chat library. */
    val root: String?,
    /** Which component props accept `$binding`s. */
    val reactiveProps: ReactivePropResolver = ReactivePropResolver { _, _ -> false },
    /** The library's functions, called as `@Name(...)`. */
    val functions: List<DefinedFunction> = emptyList(),
    /** The library's action steps, used as `@Name(...)`. */
    val customActions: List<DefinedAction> = emptyList(),
) {
    /**
     * Positional params per component and the calls programs can make, compiled once. Functions
     * and actions the schema doesn't list are added from [functions] and [customActions].
     */
    val paramMap: ParamMap = Parser.compileSchema(withCalls(jsonSchema, functions, customActions))

    /** Component names in schema order. */
    val componentNames: List<String> get() = paramMap.names

    /** A one-shot parser for this library. */
    fun parser(): Parser = Parser(paramMap, root)

    /** An incremental parser for streamed responses in this library. */
    fun streamParser(): StreamParser = StreamParser(paramMap, root)

    /** Factory for schemas from upstream library specs. */
    companion object {
        private fun withCalls(
            schema: OpenUIValue,
            functions: List<DefinedFunction>,
            actions: List<DefinedAction>,
        ): OpenUIValue {
            if (functions.isEmpty() && actions.isEmpty()) return schema
            fun section(key: String, calls: List<Pair<String, OpenUIValue>>) = OpenUIValue.Obj(
                OpenUIObject.build {
                    calls.forEach { (name, params) -> this[name] = OpenUIValue.obj("params" to params) }
                    schema[key].objectValue?.forEach { (name, call) -> this[name] = call }
                },
            )
            return OpenUIValue.Obj(
                OpenUIObject.build {
                    schema.objectValue?.forEach { (key, value) -> this[key] = value }
                    this["functions"] = section("functions", functions.map { it.name to it.paramsSchema })
                    this["actions"] = section("actions", actions.map { it.name to it.paramsSchema })
                },
            )
        }

        private val bindingPattern = Regex("(\\w+)\\??: \\\$binding<")

        /**
         * Reads a `LibrarySpec` (`{ root, components: { Name: { signature, description } }, schema }`).
         * Reactive props are the ones whose signature type is `$binding<...>`. Pass the
         * implementations of the library's [functions] and its [customActions].
         */
        fun fromSpec(
            spec: OpenUIValue,
            functions: List<DefinedFunction> = emptyList(),
            customActions: List<DefinedAction> = emptyList(),
        ): LibrarySchema {
            val reactive = buildSet {
                for ((name, component) in spec["components"].objectValue.orEmpty()) {
                    val signature = component["signature"].stringValue.orEmpty()
                    bindingPattern.findAll(signature).forEach { add("$name.${it.groupValues[1]}") }
                }
            }
            return LibrarySchema(
                jsonSchema = spec["schema"] ?: OpenUIValue.Obj(OpenUIObject.Empty),
                root = spec["root"].stringValue,
                reactiveProps = { component, prop -> "$component.$prop" in reactive },
                functions = functions,
                customActions = customActions,
            )
        }
    }
}
