/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.chatclient

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.callFrom
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

internal class WhenMarkReadThread : BaseChatClientTest() {

    @Test
    fun `Given markRead api call successful ChatClient should return success result`() = runTest {
        val apiResult = callFrom { }
        val sut = Fixture().givenMarkReadApiResult(apiResult).get()

        val result = sut.markThreadRead("channelType", "channelId", "threadId").await()

        result shouldBeInstanceOf Result.Success::class
    }

    @Test
    fun `Given markRead api call fails ChatClient should return error result`() = runTest {
        val apiResult = TestCall<Unit>(Result.Failure(Error.GenericError("Error")))
        val sut = Fixture().givenMarkReadApiResult(apiResult).get()

        val result = sut.markThreadRead("channelType", "channelId", "threadId").await()

        result shouldBeInstanceOf Result.Failure::class
    }

    private inner class Fixture {

        fun givenMarkReadApiResult(result: Call<Unit>) = apply {
            whenever(api.markThreadRead(any(), any(), any())) doReturn result
        }

        fun get(): ChatClient = chatClient
    }
}
