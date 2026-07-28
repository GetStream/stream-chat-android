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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class ThreadQueryListenerTest {

    private val listener = object : ThreadQueryListener {
        override suspend fun onGetRepliesRequest(parentId: String, limit: Int) = Unit
        override suspend fun onGetRepliesResult(result: Result<List<Message>>, parentId: String, limit: Int) = Unit
        override suspend fun onGetRepliesMoreRequest(parentId: String, firstId: String, limit: Int) = Unit
        override suspend fun onGetRepliesMoreResult(
            result: Result<List<Message>>,
            parentId: String,
            firstId: String,
            limit: Int,
        ) = Unit
        override suspend fun onGetNewerRepliesRequest(parentId: String, limit: Int, lastId: String?) = Unit
        override suspend fun onGetNewerRepliesResult(
            result: Result<List<Message>>,
            parentId: String,
            limit: Int,
            lastId: String?,
        ) = Unit
    }

    @Test
    fun `the replies around hooks have no-op default implementations`() = runTest {
        listener.onGetRepliesAroundRequest(randomString(), randomString(), randomInt())
        listener.onGetRepliesAroundResult(
            Result.Success(listOf(randomMessage())),
            randomString(),
            randomString(),
            randomInt(),
        )
    }
}
