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

package io.getstream.chat.android.internal.offline.plugin.listener.internal

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class ThreadQueryListenerDatabaseTest {

    private val messageRepository: MessageRepository = mock()
    private val userRepository: UserRepository = mock()

    private val threadQueryListenerDatabase: ThreadQueryListenerDatabase = ThreadQueryListenerDatabase(
        messageRepository,
        userRepository,
    )

    @Test
    fun `given the response is successful, database should be updated`() = runTest {
        val message = randomMessage()
        val messageList = listOf(message)

        threadQueryListenerDatabase.onGetRepliesResult(Result.Success(messageList), randomString(), randomInt())

        verify(userRepository).insertUsers(any())
        verify(messageRepository).insertMessages(messageList)
    }

    @Test
    fun `given the response is failure, database should NOT be updated`() = runTest {
        val message = randomMessage()
        val messageList = listOf(message)

        threadQueryListenerDatabase.onGetRepliesResult(Result.Failure(Error.GenericError("")), randomString(), randomInt())

        verify(userRepository, never()).insertUsers(any())
        verify(messageRepository, never()).insertMessages(messageList)
    }
}
