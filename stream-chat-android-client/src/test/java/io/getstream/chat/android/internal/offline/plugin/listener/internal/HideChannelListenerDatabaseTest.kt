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

import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
internal class HideChannelListenerDatabaseTest {

    private val channelRepository: ChannelRepository = mock()
    private val messageRepository: MessageRepository = mock()

    private val hideChannelListenerDatabase = HideChannelListenerDatabase(channelRepository, messageRepository)

    @Test
    fun `when result of call is successful, the repository should be updated correctly`() = runTest {
        val (type, id) = randomCID().cidToTypeAndId()
        val cid = Pair(type, id).toCid()

        hideChannelListenerDatabase.onHideChannelResult(
            result = Result.Success(Unit),
            channelType = type,
            channelId = id,
            clearHistory = true,
        )

        verify(channelRepository).setHiddenForChannel(eq(cid), eq(true), any())
    }

    @Test
    fun `when result of call is successful and clear history flag is true, the history should be cleared`() = runTest {
        val (type, id) = randomCID().cidToTypeAndId()
        val cid = Pair(type, id).toCid()

        hideChannelListenerDatabase.onHideChannelResult(
            result = Result.Success(Unit),
            channelType = type,
            channelId = id,
            clearHistory = true,
        )

        verify(messageRepository).deleteChannelMessagesBefore(eq(cid), any())
    }

    @Test
    fun `when hide channel fails, database should not be updated`() = runTest {
        reset(messageRepository, channelRepository)

        val (type, id) = randomCID().cidToTypeAndId()
        hideChannelListenerDatabase.onHideChannelResult(
            result = Result.Failure(Error.GenericError("")),
            channelType = type,
            channelId = id,
            clearHistory = randomBoolean(),
        )

        verifyNoInteractions(messageRepository, channelRepository)
    }
}
