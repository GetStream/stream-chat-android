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

import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMembers
import io.getstream.chat.android.randomString
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelStateLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

internal class QueryMembersListenerStateTest {
    private val channelType = randomString()
    private val channelId = randomString()
    private val channelStateLogic: ChannelStateLogic = mock()
    private val logicRegistry: LogicRegistry = mock {
        on(
            it.channelState(
                channelType = eq(channelType),
                channelId = eq(channelId),
            ),
        ) doReturn channelStateLogic
    }
    private val queryMembersListenerState = QueryMembersListenerState(logicRegistry)

    @Test
    fun `when querying members, should call channelStateLogic upsertMembers`() = runTest {
        val members = randomMembers()

        queryMembersListenerState.onQueryMembersResult(
            result = Result.Success(members),
            channelType = channelType,
            channelId = channelId,
            offset = randomInt(),
            limit = randomInt(),
            filter = mock(),
            sort = mock(),
            members = randomMembers(),
        )

        verify(channelStateLogic).upsertMembers(members)
    }

    @Test
    fun `when querying members fails, should not call channelStateLogic upsertMembers`() = runTest {
        queryMembersListenerState.onQueryMembersResult(
            result = Result.Failure(mock()),
            channelType = channelType,
            channelId = channelId,
            offset = randomInt(),
            limit = randomInt(),
            filter = mock(),
            sort = mock(),
            members = randomMembers(),
        )

        verify(channelStateLogic, never()).upsertMembers(any())
    }
}
