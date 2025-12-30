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
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class ShuffleGiphyListenerDatabaseTest {

    private val userRepository: UserRepository = mock()
    private val messageRepository: MessageRepository = mock()

    private val shuffleGiphyListenerDatabase = ShuffleGiphyListenerDatabase(
        userRepository = userRepository,
        messageRepository = messageRepository,
    )

    @BeforeEach
    fun setUp() {
        reset(userRepository, messageRepository)
    }

    @Test
    fun `when shuffling giphys and request succeeds, it should be insert in database`() = runTest {
        val testMessage = randomMessage()

        shuffleGiphyListenerDatabase.onShuffleGiphyResult(randomCID(), Result.Success(testMessage))

        verify(messageRepository).insertMessage(testMessage.copy(syncStatus = SyncStatus.COMPLETED))
    }

    @Test
    fun `when shuffling giphys and request fails, it should NOT be insert in database`() = runTest {
        shuffleGiphyListenerDatabase.onShuffleGiphyResult(randomCID(), Result.Failure(Error.GenericError("")))

        verify(messageRepository, never()).insertMessage(any())
    }
}
