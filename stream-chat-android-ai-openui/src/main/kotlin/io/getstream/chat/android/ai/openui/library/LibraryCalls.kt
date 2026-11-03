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

package io.getstream.chat.android.ai.openui.library

import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue

/**
 * Defines a function of the app's that programs call as `@Name(...)`, with positional arguments
 * in the order of [params] (port of `defineFunction`).
 *
 * ```kotlin
 * val percent = defineFunction(
 *     name = "Percent",
 *     description = "Share of a total, in percent",
 *     params = listOf(PropField.required("part", PropType.number), PropField.required("total", PropType.number)),
 *     returns = PropType.number,
 * ) { args -> OpenUIValue.Num(args["part"].numberValue!! / args["total"].numberValue!! * 100) }
 * ```
 *
 * Arguments are checked against [params], and the result against [returns]. A call with invalid
 * arguments, or whose [fn] throws, evaluates to null and reports a runtime error.
 *
 * @param fn Runs the function with its arguments by name, schema defaults applied.
 */
public fun defineFunction(
    name: String,
    description: String,
    params: List<PropField>,
    returns: PropType? = null,
    fn: (OpenUIObject) -> OpenUIValue,
): DefinedFunction = DefinedFunction(name, description, params, returns, fn)

/**
 * A function of the app's, called as `@Name(...)` in programs (port of `DefinedFunction`); create
 * one with [defineFunction].
 */
public class DefinedFunction internal constructor(
    /** The name programs call it by. */
    public val name: String,
    /** What it does, for the prompt. */
    public val description: String,
    /** Its positional parameters. */
    public val params: List<PropField>,
    /** Its result type, checked at runtime and shown in the prompt. */
    public val returns: PropType? = null,
    /** Runs the function with its arguments by name. */
    public val fn: (OpenUIObject) -> OpenUIValue,
) {
    internal val paramsSchema: OpenUIValue get() = PropField.objectSchema(params, description = null)

    /** The prompt signature: `Percent(part: number, total: number) → number`. */
    internal val signature: String
        get() {
            val base = params.joinToString(", ", "$name(", ")") { it.signature }
            return returns?.let { "$base → ${it.signature}" } ?: base
        }
}

/**
 * Defines an action step of the app's, used like the built-in steps:
 * `Button("Copy", @CopyToClipboard(id))` or inside `Action([...])` (port of `defineAction`).
 *
 * On click it reaches the action handler as an action event whose `type` is the action's name and
 * whose `params` are its arguments. A step with invalid arguments does nothing.
 */
public fun defineAction(name: String, description: String, params: List<PropField>): DefinedAction =
    DefinedAction(name, description, params)

/** An action step of the app's (port of `DefinedAction`); create one with [defineAction]. */
public data class DefinedAction internal constructor(
    /** The name programs use it by, and the `type` of its action events. */
    val name: String,
    /** What it does, for the prompt. */
    val description: String,
    /** Its positional parameters, the `params` of its action events. */
    val params: List<PropField>,
) {
    internal val paramsSchema: OpenUIValue get() = PropField.objectSchema(params, description = null)

    /** The prompt signature: `CopyToClipboard(text: string)`. */
    internal val signature: String get() = params.joinToString(", ", "$name(", ")") { it.signature }

    /**
     * The JSON shape of a step of this action, the `$defs` entry a slot that takes only some
     * actions refers to: `{type: "CopyToClipboard", params: {...}}`.
     */
    internal val stepSchema: OpenUIValue
        get() = PropField.objectSchema(
            listOf(
                PropField.required("type", PropType.literal(name)),
                PropField.required("params", PropType.objectOf(params)),
            ),
            description = null,
        )
}
