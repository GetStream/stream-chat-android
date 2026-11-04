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

/**
 * A dynamically typed value flowing through the OpenUI pipeline.
 *
 * OpenUI Lang is evaluated with JavaScript semantics in the reference implementation, so the
 * runtime works on JSON-like values. On top of plain JSON, two kinds of values can appear in
 * component props:
 * - [Element]: a parsed component invocation ([ElementNode]).
 * - [Expression]: a runtime expression ([ASTNode]) preserved by the parser and resolved by the
 *   evaluator (state references, query results, operators, action plans, ...).
 *
 * JavaScript's `undefined` is modelled as a Kotlin `null` (`OpenUIValue?`), so `null` in the
 * language is [Null] and an absent value is `null`.
 */
internal sealed interface OpenUIValue {

    /** JavaScript `null`. */
    data object Null : OpenUIValue {
        override fun toString(): String = "null"
    }

    /** A boolean. */
    data class Bool(val value: Boolean) : OpenUIValue {
        override fun toString(): String = value.toString()
    }

    /** A JavaScript number. Equality follows `Object.is` (`NaN` equals itself, `-0` differs from `0`). */
    data class Num(val value: Double) : OpenUIValue {
        override fun toString(): String = JS.numberToString(value)
    }

    /** A string. */
    data class Str(val value: String) : OpenUIValue {
        override fun toString(): String = JS.jsonStringify(this)!!
    }

    /** An array. */
    data class Arr(val items: List<OpenUIValue>) : OpenUIValue {
        override fun toString(): String = JS.jsonStringify(this)!!
    }

    /** An object, keeping key order. */
    data class Obj(val fields: OpenUIObject) : OpenUIValue {
        override fun toString(): String = JS.jsonStringify(this)!!
    }

    /** A component invocation. */
    data class Element(val node: ElementNode) : OpenUIValue {
        override fun toString(): String = JS.jsonStringify(this)!!
    }

    /** A runtime expression the evaluator resolves; opaque outside the runtime. */
    data class Expression internal constructor(internal val node: ASTNode) : OpenUIValue {
        override fun toString(): String = JS.jsonStringify(this)!!
    }

    /** Constants and factories. */
    companion object {
        /** `true`. */
        val True: OpenUIValue = Bool(true)

        /** `false`. */
        val False: OpenUIValue = Bool(false)

        /** `[]`. */
        val EmptyArray: OpenUIValue = Arr(emptyList())

        /** A boolean value ([True] or [False]). */
        fun of(value: Boolean): OpenUIValue = if (value) True else False

        /** A number value. */
        fun of(value: Double): OpenUIValue = Num(value)

        /** A number value. */
        fun of(value: Int): OpenUIValue = Num(value.toDouble())

        /** A string value. */
        fun of(value: String): OpenUIValue = Str(value)

        /** An array value. */
        fun of(items: List<OpenUIValue>): OpenUIValue = Arr(items)

        /** An object value. */
        fun of(fields: OpenUIObject): OpenUIValue = Obj(fields)

        /** Builds an object value from pairs, keeping their order (`null` values are skipped). */
        fun obj(vararg pairs: Pair<String, OpenUIValue?>): OpenUIValue = Obj(OpenUIObject.of(*pairs))

        /** Builds an array value. */
        fun arr(vararg items: OpenUIValue): OpenUIValue = Arr(items.toList())
    }
}

// Accessors -----------------------------------------------------------------------------------

/** Whether this is JavaScript `null` (not `undefined`). */
internal val OpenUIValue?.isNull: Boolean get() = this is OpenUIValue.Null

/** The string, or `null` when this is not a [OpenUIValue.Str]. */
internal val OpenUIValue?.stringValue: String? get() = (this as? OpenUIValue.Str)?.value

/** The number, or `null` when this is not a [OpenUIValue.Num]. */
internal val OpenUIValue?.numberValue: Double? get() = (this as? OpenUIValue.Num)?.value

/** The boolean, or `null` when this is not a [OpenUIValue.Bool]. */
internal val OpenUIValue?.boolValue: Boolean? get() = (this as? OpenUIValue.Bool)?.value

/** The items, or `null` when this is not a [OpenUIValue.Arr]. */
internal val OpenUIValue?.arrayValue: List<OpenUIValue>? get() = (this as? OpenUIValue.Arr)?.items

/** The fields, or `null` when this is not a [OpenUIValue.Obj]. */
internal val OpenUIValue?.objectValue: OpenUIObject? get() = (this as? OpenUIValue.Obj)?.fields

/** The element, or `null` when this is not a [OpenUIValue.Element]. */
internal val OpenUIValue?.elementValue: ElementNode? get() = (this as? OpenUIValue.Element)?.node

internal val OpenUIValue?.expressionValue: ASTNode? get() = (this as? OpenUIValue.Expression)?.node

/** Reads a property the way JavaScript's `value[key]` would (own properties only). */
internal operator fun OpenUIValue?.get(key: String): OpenUIValue? = JS.property(this, key)

/** Reads an array item (`value[index]`). */
internal operator fun OpenUIValue?.get(index: Int): OpenUIValue? = JS.property(this, index.toString())

/**
 * This value with elements and expressions converted (deeply) to their plain JSON object shapes,
 * and non-finite numbers turned into `null`, matching `JSON.parse(JSON.stringify(value))`.
 *
 * In an expression, a call's named prop that is one of the call's own arguments is written once,
 * in `args`, and referenced from `mappedProps` as `{"k": "Arg", "i": <index>}`, so the output
 * stays proportional to the expression's size however deeply calls nest.
 */
internal val OpenUIValue.plainJson: OpenUIValue
    get() = when (this) {
        is OpenUIValue.Num -> if (value.isFinite()) this else OpenUIValue.Null
        OpenUIValue.Null, is OpenUIValue.Bool, is OpenUIValue.Str -> this
        is OpenUIValue.Arr -> OpenUIValue.Arr(items.map { it.plainJson })
        // The token that marks custom steps real calls made never leaves the runtime.
        is OpenUIValue.Obj -> OpenUIValue.Obj(
            OpenUIObject.build {
                for ((key, value) in fields) if (!ActionPlan.isCallToken(key, value)) this[key] = value.plainJson
            },
        )
        is OpenUIValue.Element -> node.jsonValue
        is OpenUIValue.Expression -> node.jsonValue
    }

// Conversions from Kotlin values, for building values in code and tests.

/** Converts a Kotlin value (`String`, `Number`, `Boolean`, `List`, `Map`, `OpenUIValue`, `null`) to a value. */
internal fun openUIValueOf(value: Any?): OpenUIValue = when (value) {
    null -> OpenUIValue.Null
    is OpenUIValue -> value
    is String -> OpenUIValue.Str(value)
    is Boolean -> OpenUIValue.of(value)
    is Number -> OpenUIValue.Num(value.toDouble())
    is List<*> -> OpenUIValue.Arr(value.map(::openUIValueOf))
    is Map<*, *> -> OpenUIValue.Obj(
        OpenUIObject(value.entries.associate { (key, item) -> key.toString() to openUIValueOf(item) }),
    )
    is ElementNode -> OpenUIValue.Element(value)
    is ASTNode -> OpenUIValue.Expression(value)
    else -> error("Unsupported value: $value")
}
