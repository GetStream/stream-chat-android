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

/** Loads the JSON fixtures in `src/test/resources/fixtures`. */
internal object Fixtures {
    fun text(name: String): String =
        requireNotNull(Fixtures::class.java.getResource("/fixtures/$name")) { "Missing fixture $name" }.readText()

    fun json(name: String): OpenUIValue = OpenUIJson.parse(text(name))

    /**
     * The upstream libraries the fixtures were generated with: chat, playground, the parser tests'
     * mini library and the 1.0 spec library.
     */
    val libraries: Map<String, Pair<ParamMap, String?>> by lazy {
        buildMap {
            for ((name, file) in listOf(
                "chat" to "upstream-chat-library.spec.json",
                "playground" to "upstream-playground-library.spec.json",
            )) {
                val spec = json(file)
                put(name, Parser.compileSchema(spec["schema"]!!) to spec["root"].stringValue)
            }
            val mini = OpenUIJson.parse(
                """{"${'$'}defs": {"Stack": {"properties": {"children": {}}, "required": ["children"]},
                   "Title": {"properties": {"text": {"type": "string"}}, "required": ["text"]}}}""",
            )
            put("mini", Parser.compileSchema(mini) to null)
            val spec = json("spec-library.json")
            put("spec", Parser.compileSchema(spec["schema"]!!) to spec["root"].stringValue)
        }
    }
}

/**
 * [value] in the reference implementation's shape: a call's mapped props that refer to its args
 * (`{k: "Arg", i}`) are replaced by copies of those args, as upstream writes them. Fixtures only;
 * the copies double at every nesting level.
 */
internal fun upstreamShape(value: OpenUIValue?): OpenUIValue? = when (value) {
    is OpenUIValue.Arr -> OpenUIValue.Arr(value.items.map { upstreamShape(it)!! })
    is OpenUIValue.Obj -> {
        val fields = value.fields.mapValues { upstreamShape(it)!! }
        val args = fields["args"].arrayValue
        val props = fields["mappedProps"].objectValue
        if (fields["k"].stringValue == "Comp" && args != null && props != null) {
            val expanded = props.mapValues { prop ->
                if (prop["k"].stringValue == ASTNode.ARG_KIND) args[prop["i"].numberValue!!.toInt()] else prop
            }
            OpenUIValue.Obj(fields.with("mappedProps", OpenUIValue.Obj(expanded)))
        } else {
            OpenUIValue.Obj(fields)
        }
    }
    else -> value
}

/**
 * Describes the first structural difference between two values (`null` when equal). Objects are
 * compared without regard to key order, numbers by value (`-0 == 0`), like JSON round-trips.
 */
internal fun firstDifference(actual: OpenUIValue?, expected: OpenUIValue?, path: String = "$"): String? {
    if (actual is OpenUIValue.Obj && expected is OpenUIValue.Obj) {
        expected.fields.keys
            .firstOrNull {
                it !in actual.fields
            }?.let { return "$path.$it: missing (expected ${expected.fields[it]})" }
        actual.fields.keys
            .firstOrNull { it !in expected.fields }
            ?.let { return "$path.$it: unexpected ${actual.fields[it]}" }
        for ((key, value) in expected.fields) {
            firstDifference(actual.fields[key], value, "$path.$key")?.let { return it }
        }
        return null
    }
    if (actual is OpenUIValue.Arr && expected is OpenUIValue.Arr) {
        for (index in 0 until minOf(actual.items.size, expected.items.size)) {
            firstDifference(actual.items[index], expected.items[index], "$path[$index]")?.let { return it }
        }
        if (actual.items.size != expected.items.size) {
            return "$path: count ${actual.items.size} != expected ${expected.items.size}"
        }
        return null
    }
    if (actual is OpenUIValue.Num && expected is OpenUIValue.Num && actual.value == expected.value) return null
    return if (actual == expected) null else "$path: $actual != expected $expected"
}

/** Shorthand for building values in tests: `v("a")`, `v(1)`, `v(listOf(...))`, `v(mapOf(...))`. */
internal fun v(value: Any?): OpenUIValue = openUIValueOf(value)

/** Shorthand for an object value with ordered keys. */
internal fun obj(vararg pairs: Pair<String, Any?>): OpenUIValue =
    OpenUIValue.Obj(OpenUIObject(linkedMapOf(*pairs).mapValues { v(it.value) }))
