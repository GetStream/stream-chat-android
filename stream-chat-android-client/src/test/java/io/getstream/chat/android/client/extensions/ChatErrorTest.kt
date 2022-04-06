/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
 
package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.errors.ChatNetworkError
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import java.net.UnknownHostException

internal class ChatErrorTest {

    @Test
    fun `error for messages with the same ID should be permanent`() {
        val error = ChatNetworkError.create(4, "a message with ID the same id already exists", 400, null)
        error.isPermanent().shouldBeTrue()
    }

    @Test
    fun `rateLimit error should be temporary`() {
        val error = ChatNetworkError.create(9, "", 429, null)
        error.isPermanent().shouldBeFalse()
    }

    @Test
    fun `request timeout should be a temporary error`() {
        val error = ChatNetworkError.create(23, "", 408, null)
        error.isPermanent().shouldBeFalse()
    }

    @Test
    fun `broken api should be a temporary error`() {
        val error = ChatNetworkError.create(0, "", 500, null)
        error.isPermanent().shouldBeFalse()
    }

    @Test
    fun `cool down period error should be permanent`() {
        val error = ChatNetworkError.create(60, "", 403, null)
        error.isPermanent().shouldBeTrue()
    }

    @Test
    fun `UnknownHost as cause should be a temporary error`() {
        val error = ChatNetworkError.create(0, "", 500, UnknownHostException())
        error.isPermanent().shouldBeFalse()
    }
}
