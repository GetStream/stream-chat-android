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

package io.getstream.chat.android.ai.openui.components

import io.getstream.chat.android.ai.openui.lang.ActionStepType
import io.getstream.chat.android.ai.openui.lang.BuiltinActionType
import io.getstream.chat.android.ai.openui.lang.JS
import io.getstream.chat.android.ai.openui.lang.OpenUIJson
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.arrayValue
import io.getstream.chat.android.ai.openui.lang.get
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.lang.stringValue

/** Merges a clicked item's details into a card block's shared action (port of `withItemContext`). */
internal object ItemActionContext {

    fun merge(action: OpenUIValue?, item: OpenUIObject): OpenUIValue {
        if (action == null || action is OpenUIValue.Null) {
            return OpenUIValue.obj(
                "type" to OpenUIValue.Str(BuiltinActionType.CONTINUE_CONVERSATION),
                "params" to OpenUIValue.Obj(item),
            )
        }
        val steps = action["steps"].arrayValue
        if (steps != null) {
            val plan = action.objectValue
            if (item.isEmpty() || plan == null) return action
            val suffix = "Selected item: " + OpenUIJson.stringify(OpenUIValue.Obj(item))
            val merged = steps.map { step ->
                val fields = step.objectValue
                if (step["type"] != OpenUIValue.Str(ActionStepType.CONTINUE_CONVERSATION) || fields == null) {
                    step
                } else {
                    val existing = fields["context"].stringValue
                    OpenUIValue.Obj(
                        fields.with(
                            "context",
                            OpenUIValue.Str(if (existing.isNullOrEmpty()) suffix else "$existing\n$suffix"),
                        ),
                    )
                }
            }
            return OpenUIValue.Obj(plan.with("steps", OpenUIValue.Arr(merged)))
        }
        val params = OpenUIObject.build {
            action["params"].objectValue?.forEach { (key, value) -> this[key] = value }
            action["url"]?.let { this["url"] = it }
            action["context"]?.takeIf(JS::isTruthy)?.let { this["context"] = it }
            item.forEach { (key, value) -> this[key] = value }
        }
        val type = action["type"]?.let(JS::toString) ?: BuiltinActionType.CONTINUE_CONVERSATION
        return OpenUIValue.obj("type" to OpenUIValue.Str(type), "params" to OpenUIValue.Obj(params))
    }

    /** An item context object without the absent values. */
    fun obj(vararg pairs: Pair<String, OpenUIValue?>): OpenUIObject = OpenUIObject.of(*pairs)
}
