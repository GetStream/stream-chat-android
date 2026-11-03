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

/** Tells the runtime which component props accept two-way `$variable` bindings. */
internal fun interface ReactivePropResolver {
    /** Whether [prop] of [component] accepts a `$variable` binding. */
    fun isReactiveProp(component: String, prop: String): Boolean
}

/**
 * What the language runtime needs to know about a component library: its JSON Schema, its root
 * component and which props take two-way `$variable` bindings.
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
) {
    /** Positional params per component and the calls programs can make, compiled once. */
    val paramMap: ParamMap = Parser.compileSchema(jsonSchema)

    /** Component names in schema order. */
    val componentNames: List<String> get() = paramMap.names

    /** A one-shot parser for this library. */
    fun parser(): Parser = Parser(paramMap, root)

    /** An incremental parser for streamed responses in this library. */
    fun streamParser(): StreamParser = StreamParser(paramMap, root)

    /** Factory for schemas from upstream library specs. */
    companion object {
        private val bindingPattern = Regex("(\\w+)\\??: \\\$binding<")

        /**
         * Reads a `LibrarySpec` (`{ root, components: { Name: { signature, description } }, schema }`).
         * Reactive props are the ones whose signature type is `$binding<...>`.
         */
        fun fromSpec(spec: OpenUIValue): LibrarySchema {
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
            )
        }
    }
}
