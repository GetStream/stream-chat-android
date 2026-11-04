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

package io.getstream.chat.android.ai.openui.rendering

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OpenUIIconsTest {

    @Test
    fun `icon names read as words`() {
        assertEquals("trash", OpenUIIcons.label("trash-2"))
        assertEquals("arrow up right", OpenUIIcons.label("arrow-up-right"))
        assertEquals("calendar", OpenUIIcons.label("Calendar"))
    }

    @Test
    fun `names without words have no label`() {
        assertNull(OpenUIIcons.label(null))
        assertNull(OpenUIIcons.label(""))
        assertNull(OpenUIIcons.label("2"))
        assertNull(OpenUIIcons.label(" - "))
    }
}
