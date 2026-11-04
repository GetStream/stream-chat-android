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

import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import kotlin.test.Test
import kotlin.test.assertEquals

internal class EditableTableSupportTest {

    @Test
    fun `column widths stay within what a cell can show`() {
        assertEquals(600.0, EditableTableSupport.columnWidth(100_000.0))
        assertEquals(48.0, EditableTableSupport.columnWidth(-5.0))
        assertEquals(200.0, EditableTableSupport.columnWidth(200.0))
        assertEquals(150.0, EditableTableSupport.columnWidth(null))
        assertEquals(150.0, EditableTableSupport.columnWidth(Double.POSITIVE_INFINITY))
        assertEquals(150.0, EditableTableSupport.columnWidth(Double.NaN))
    }

    @Test
    fun `saved rows restore unchanged`() {
        val rows = listOf(
            mapOf("id" to OpenUIValue.Str("1"), "name" to OpenUIValue.Str("Ada"), "age" to OpenUIValue.Num(36.0)),
            mapOf("id" to OpenUIValue.Str("2"), "name" to OpenUIValue.Str(""), "age" to OpenUIValue.Str("x")),
        )

        assertEquals(rows, EditableTableSupport.decode(EditableTableSupport.encode(rows)))
        assertEquals(emptyList(), EditableTableSupport.decode("not json"))
    }
}
