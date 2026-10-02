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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class QueryChannelListenerStateTest {

    private val logicRegistry: LogicRegistry = mock {
        on { channel(any(), any()) } doReturn mock<ChannelLogic>()
    }
    private val mutableGlobalState = MutableGlobalState(randomString())
    private val listener = QueryChannelListenerState(logicRegistry, mutableGlobalState)

    @Test
    fun `given the server returns the channel draft, it should reach the global state`() = runTest {
        val draftMessage = randomDraftMessage(parentId = null)
        val channel = randomChannel(draftMessage = draftMessage)

        listener.onQueryChannelResult(Result.Success(channel), channel.type, channel.id, QueryChannelRequest())

        assertEquals(mapOf(draftMessage.cid to draftMessage), mutableGlobalState.channelDraftMessages.value)
    }

    @Test
    fun `given the query fails, it should leave the global state drafts untouched`() = runTest {
        val draftMessage = randomDraftMessage(parentId = null)
        mutableGlobalState.updateDraftMessage(draftMessage)

        listener.onQueryChannelResult(
            Result.Failure(Error.GenericError(randomString())),
            randomString(),
            randomString(),
            QueryChannelRequest(),
        )

        assertEquals(mapOf(draftMessage.cid to draftMessage), mutableGlobalState.channelDraftMessages.value)
    }
}
