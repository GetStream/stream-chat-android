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

package io.getstream.chat.android.ai.openui.generativeui

import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.plainJson

// OpenUI values as the plain Kotlin values of StreamChatAI's generative UI (maps, lists, strings,
// numbers, booleans and `null`), and back.

/**
 * The value as plain Kotlin values, as `JSON.parse(JSON.stringify(value))` reads it: components
 * and expressions turn into their plain JSON shapes, and numbers that aren't finite into `null`.
 */
internal val OpenUIValue?.plainValue: Any?
    get() = this?.plainJson?.let(::plain)

/** The object as a plain Kotlin map, the way [plainValue] reads its values. */
internal val OpenUIObject.plainValue: Map<String, Any?>
    get() = buildMap { this@plainValue.forEach { (key, value) -> put(key, value.plainValue) } }

private fun plain(value: OpenUIValue): Any? = when (value) {
    is OpenUIValue.Bool -> value.value
    is OpenUIValue.Num -> plainNumber(value.value)
    is OpenUIValue.Str -> value.value
    is OpenUIValue.Arr -> value.items.map(::plain)
    is OpenUIValue.Obj -> buildMap { value.fields.forEach { (key, field) -> put(key, plain(field)) } }
    else -> null
}

/** Plain Kotlin values as an OpenUI value: maps, lists and arrays, strings, numbers and booleans. */
internal fun openUIValue(value: Any?): OpenUIValue = when (value) {
    null -> OpenUIValue.Null
    is OpenUIValue -> value
    is Boolean -> OpenUIValue.of(value)
    is Number -> OpenUIValue.Num(value.toDouble())
    is String -> OpenUIValue.Str(value)
    is Map<*, *> -> OpenUIValue.Obj(openUIObject(value))
    is Iterable<*> -> OpenUIValue.Arr(value.map(::openUIValue))
    is Array<*> -> OpenUIValue.Arr(value.map(::openUIValue))
    else -> OpenUIValue.Str(value.toString())
}

/** A plain Kotlin map as an OpenUI object. */
internal fun openUIObject(map: Map<*, *>): OpenUIObject =
    OpenUIObject.build { map.forEach { (key, value) -> this[key.toString()] = openUIValue(value) } }

// A whole number as the `Int` or `Long` JSON parsers give, any other as a `Double`.
private fun plainNumber(value: Double): Number = when {
    value % 1.0 != 0.0 || !value.isFinite() -> value
    value >= Int.MIN_VALUE && value <= Int.MAX_VALUE -> value.toInt()
    value >= -MaxSafeInteger && value <= MaxSafeInteger -> value.toLong()
    else -> value
}

// The largest whole number a JavaScript number holds exactly.
private const val MaxSafeInteger = 9_007_199_254_740_991.0
