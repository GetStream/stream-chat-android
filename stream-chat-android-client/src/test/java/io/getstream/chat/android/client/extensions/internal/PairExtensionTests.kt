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

package io.getstream.chat.android.client.extensions.internal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class PairExtensionTests {

    @Test
    fun `toCid should return valid cid when given valid channel type and id`() {
        // given
        val channelType = "messaging"
        val channelId = "123"
        val expectedCid = "messaging:123"

        // when
        val result = Pair(channelType, channelId).toCid()

        // then
        assertEquals(expectedCid, result)
    }

    @Test
    fun `toCid should return valid cid when given valid channel type and id with special characters`() {
        // given
        val channelType = "messaging"
        val channelId = "123-456_789"
        val expectedCid = "messaging:123-456_789"

        // when
        val result = Pair(channelType, channelId).toCid()

        // then
        assertEquals(expectedCid, result)
    }

    @Test
    fun `toCid should throw IllegalArgumentException when channel type is empty`() {
        // given
        val channelType = ""
        val channelId = "123"

        // when/then
        assertThrows(IllegalArgumentException::class.java) {
            Pair(channelType, channelId).toCid()
        }
    }

    @Test
    fun `toCid should throw IllegalArgumentException when channel id is empty`() {
        // given
        val channelType = "messaging"
        val channelId = ""

        // when/then
        assertThrows(IllegalArgumentException::class.java) {
            Pair(channelType, channelId).toCid()
        }
    }

    @Test
    fun `toCid should throw IllegalArgumentException when channel type is blank`() {
        // given
        val channelType = "   "
        val channelId = "123"

        // when/then
        assertThrows(IllegalArgumentException::class.java) {
            Pair(channelType, channelId).toCid()
        }
    }

    @Test
    fun `toCid should throw IllegalArgumentException when channel id is blank`() {
        // given
        val channelType = "messaging"
        val channelId = "   "

        // when/then
        assertThrows(IllegalArgumentException::class.java) {
            Pair(channelType, channelId).toCid()
        }
    }

    @Test
    fun `toCid should throw IllegalArgumentException when channel type contains invalid characters`() {
        // given
        val channelType = "messaging:invalid"
        val channelId = "123"

        // when/then
        assertThrows(IllegalArgumentException::class.java) {
            Pair(channelType, channelId).toCid()
        }
    }

    @Test
    fun `toCid should throw IllegalArgumentException when channel id contains invalid characters`() {
        // given
        val channelType = "messaging"
        val channelId = "123:456"

        // when/then
        assertThrows(IllegalArgumentException::class.java) {
            Pair(channelType, channelId).toCid()
        }
    }
}
