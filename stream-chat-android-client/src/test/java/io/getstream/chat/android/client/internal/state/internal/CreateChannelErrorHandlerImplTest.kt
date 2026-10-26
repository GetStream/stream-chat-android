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

package io.getstream.chat.android.client.internal.state.internal

import io.getstream.chat.android.client.internal.state.errorhandler.internal.CreateChannelErrorHandlerImpl
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.IOException

internal class CreateChannelErrorHandlerImplTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val placeholder = randomChannel(id = "!members-a, b", type = "messaging")
    private val channelRepository: ChannelRepository = mock {
        onBlocking { it.selectChannels(any()) } doReturn listOf(placeholder)
    }
    private val clientState: ClientState = mock {
        on { it.isOnline } doReturn false
    }

    @Test
    fun `offline with a connection failure returns the cached placeholder`() = runTest {
        val error = Error.NetworkError(message = "", serverErrorCode = 1000, statusCode = -1, cause = IOException())

        createChannel(error) shouldBeEqualTo Result.Success(placeholder)
    }

    @Test
    fun `offline with a server rejection returns the rejection`() = runTest {
        val error = Error.NetworkError(message = "not allowed", serverErrorCode = 17, statusCode = 403)

        createChannel(error) shouldBeEqualTo Result.Failure(error)
    }

    private suspend fun createChannel(error: Error): Result<Channel> =
        CreateChannelErrorHandlerImpl(testCoroutines.scope, channelRepository, clientState)
            .onCreateChannelError(TestCall(Result.Failure(error)), "messaging", "", listOf("a", "b"), emptyMap())
            .await()
}
