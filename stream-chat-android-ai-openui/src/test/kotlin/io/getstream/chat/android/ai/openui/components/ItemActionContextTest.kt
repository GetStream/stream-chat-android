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
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.get
import io.getstream.chat.android.ai.openui.lang.stringValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ItemActionContextTest {

    private fun s(value: String) = OpenUIValue.Str(value)
    private fun plan(vararg steps: OpenUIValue) = OpenUIValue.obj("steps" to OpenUIValue.Arr(steps.toList()))
    private fun step(type: String, vararg extra: Pair<String, OpenUIValue>) = OpenUIValue.obj("type" to s(type), *extra)

    @Test
    fun `merge without action continues the conversation with the item`() {
        val item = OpenUIObject.of("title" to s("Lisbon"))
        val merged = ItemActionContext.merge(null, item)
        assertEquals(s(BuiltinActionType.CONTINUE_CONVERSATION), merged["type"])
        assertEquals(s("Lisbon"), merged["params"]["title"])
    }

    @Test
    fun `merge into a plan appends the item to conversation steps`() {
        val item = OpenUIObject.of("title" to s("Lisbon"))
        val action = plan(
            step(ActionStepType.CONTINUE_CONVERSATION, "context" to s("Tell me more")),
            step(ActionStepType.CONTINUE_CONVERSATION),
            step(ActionStepType.OPEN_URL, "url" to s("https://x.y")),
        )
        val steps = ItemActionContext.merge(action, item)["steps"]
        assertEquals("Tell me more\nSelected item: {\"title\":\"Lisbon\"}", steps[0]["context"].stringValue)
        assertEquals("Selected item: {\"title\":\"Lisbon\"}", steps[1]["context"].stringValue)
        assertNull(steps[2]["context"])
        assertEquals(action, ItemActionContext.merge(action, OpenUIObject.Empty))
    }

    @Test
    fun `merge into a legacy action flattens its params`() {
        val action = OpenUIValue.obj(
            "type" to s("open_url"),
            "url" to s("https://x.y"),
            "params" to OpenUIValue.obj("a" to s("1")),
        )
        val merged = ItemActionContext.merge(action, OpenUIObject.of("id" to s("7")))
        assertEquals(s("open_url"), merged["type"])
        assertEquals(s("https://x.y"), merged["params"]["url"])
        assertEquals(s("1"), merged["params"]["a"])
        assertEquals(s("7"), merged["params"]["id"])
    }
}
