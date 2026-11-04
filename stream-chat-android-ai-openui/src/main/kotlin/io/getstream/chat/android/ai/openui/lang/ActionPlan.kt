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

import java.util.UUID

/**
 * Evaluated actions are plans: `{steps: [...]}`. A step call is a one-step plan and an invalid
 * step an empty one.
 */
internal object ActionPlan {
    /**
     * Upstream keeps the custom steps real calls made in a WeakSet, so data (an object literal,
     * `$state`, an `@Each` item) can't pose as a custom action. Values have no identity here, so
     * those steps carry a token data can't know. Expressions can't read it (see [isCallToken]), so
     * they can't copy it into a step of their own.
     */
    const val CALL_TOKEN_KEY: String = "__openuiCall"
    private val callToken: OpenUIValue = OpenUIValue.Str(UUID.randomUUID().toString())

    /** Whether [key] holds the token, which only the runtime sees; data under that key is ordinary. */
    fun isCallToken(key: String, value: OpenUIValue?): Boolean = key == CALL_TOKEN_KEY && value == callToken

    val empty: OpenUIValue get() = plan(emptyList())

    fun plan(steps: List<OpenUIValue>): OpenUIValue = OpenUIValue.obj("steps" to OpenUIValue.Arr(steps))

    fun customStep(name: String, params: OpenUIValue): OpenUIValue = OpenUIValue.obj(
        "type" to OpenUIValue.Str(ActionStepType.CUSTOM_ACTION),
        "name" to OpenUIValue.Str(name),
        "params" to params,
        CALL_TOKEN_KEY to callToken,
    )

    fun isPlan(value: OpenUIValue?): Boolean = value["steps"] is OpenUIValue.Arr

    fun steps(value: OpenUIValue?): List<OpenUIValue> =
        if (isPlan(value)) value["steps"].arrayValue.orEmpty() else emptyList()

    /** Flattens plans (and stray step objects) into one plan; other values are dropped. */
    fun flatten(values: List<OpenUIValue>): OpenUIValue {
        val flattened = values.flatMap { value ->
            when {
                isPlan(value) -> steps(value)
                value is OpenUIValue.Obj && "type" in value.fields -> listOf(value)
                value is OpenUIValue.Element -> listOf(value)
                else -> emptyList()
            }
        }
        return ownSteps(plan(flattened))
    }

    /** Drops the custom steps no real call made. */
    fun ownSteps(plan: OpenUIValue): OpenUIValue {
        val steps = plan["steps"].arrayValue ?: return plan
        val own = steps.filter { step ->
            step["type"].stringValue != ActionStepType.CUSTOM_ACTION ||
                (step as? OpenUIValue.Obj)?.fields?.get(CALL_TOKEN_KEY) == callToken
        }
        if (own.size == steps.size) return plan
        return OpenUIValue.Obj(
            OpenUIObject.build {
                plan.objectValue?.forEach { (key, value) -> this[key] = value }
                this["steps"] = OpenUIValue.Arr(own)
            },
        )
    }
}
