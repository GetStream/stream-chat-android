/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.logger

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ChatLogLevelTest {

    @Test
    fun `ALL should be more or equal than all levels`() {
        assertTrue(ChatLogLevel.ALL.isMoreOrEqualsThan(ChatLogLevel.ALL))
        assertTrue(ChatLogLevel.ALL.isMoreOrEqualsThan(ChatLogLevel.DEBUG))
        assertTrue(ChatLogLevel.ALL.isMoreOrEqualsThan(ChatLogLevel.WARN))
        assertTrue(ChatLogLevel.ALL.isMoreOrEqualsThan(ChatLogLevel.ERROR))
        assertTrue(ChatLogLevel.ALL.isMoreOrEqualsThan(ChatLogLevel.NOTHING))
    }

    @Test
    fun `DEBUG should be more or equal than DEBUG, WARN, ERROR, NOTHING`() {
        assertFalse(ChatLogLevel.DEBUG.isMoreOrEqualsThan(ChatLogLevel.ALL))
        assertTrue(ChatLogLevel.DEBUG.isMoreOrEqualsThan(ChatLogLevel.DEBUG))
        assertTrue(ChatLogLevel.DEBUG.isMoreOrEqualsThan(ChatLogLevel.WARN))
        assertTrue(ChatLogLevel.DEBUG.isMoreOrEqualsThan(ChatLogLevel.ERROR))
        assertTrue(ChatLogLevel.DEBUG.isMoreOrEqualsThan(ChatLogLevel.NOTHING))
    }

    @Test
    fun `WARN should be more or equal than WARN, ERROR, NOTHING`() {
        assertFalse(ChatLogLevel.WARN.isMoreOrEqualsThan(ChatLogLevel.ALL))
        assertFalse(ChatLogLevel.WARN.isMoreOrEqualsThan(ChatLogLevel.DEBUG))
        assertTrue(ChatLogLevel.WARN.isMoreOrEqualsThan(ChatLogLevel.WARN))
        assertTrue(ChatLogLevel.WARN.isMoreOrEqualsThan(ChatLogLevel.ERROR))
        assertTrue(ChatLogLevel.WARN.isMoreOrEqualsThan(ChatLogLevel.NOTHING))
    }

    @Test
    fun `ERROR should be more or equal than ERROR, NOTHING`() {
        assertFalse(ChatLogLevel.ERROR.isMoreOrEqualsThan(ChatLogLevel.ALL))
        assertFalse(ChatLogLevel.ERROR.isMoreOrEqualsThan(ChatLogLevel.DEBUG))
        assertFalse(ChatLogLevel.ERROR.isMoreOrEqualsThan(ChatLogLevel.WARN))
        assertTrue(ChatLogLevel.ERROR.isMoreOrEqualsThan(ChatLogLevel.ERROR))
        assertTrue(ChatLogLevel.ERROR.isMoreOrEqualsThan(ChatLogLevel.NOTHING))
    }

    @Test
    fun `NOTHING should be more or equal than NOTHING`() {
        assertFalse(ChatLogLevel.NOTHING.isMoreOrEqualsThan(ChatLogLevel.ALL))
        assertFalse(ChatLogLevel.NOTHING.isMoreOrEqualsThan(ChatLogLevel.DEBUG))
        assertFalse(ChatLogLevel.NOTHING.isMoreOrEqualsThan(ChatLogLevel.WARN))
        assertFalse(ChatLogLevel.NOTHING.isMoreOrEqualsThan(ChatLogLevel.ERROR))
        assertTrue(ChatLogLevel.NOTHING.isMoreOrEqualsThan(ChatLogLevel.NOTHING))
    }
}
