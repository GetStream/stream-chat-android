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

import io.getstream.chat.android.models.GroupedChannels
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class GroupedQueryChannelsListenerStateTest {

    private val globalState: MutableGlobalState = mock()
    private val listener = GroupedQueryChannelsListenerState(globalState)

    @Test
    fun `when result is successful, grouped unread channels should be set on global state`() = runTest {
        // given
        doNothing().`when`(globalState).setGroupedUnreadChannels(any())
        val result = Result.Success(
            value = GroupedChannels(
                groups = mapOf(
                    "direct" to GroupedChannelsGroup(channels = emptyList(), unreadChannels = 3),
                    "support" to GroupedChannelsGroup(channels = emptyList(), unreadChannels = 1),
                ),
            ),
        )
        // when
        listener.onGroupedQueryChannelsResult(result, limit = null, watch = false, presence = false)
        // then
        verify(globalState, times(1)).setGroupedUnreadChannels(mapOf("direct" to 3, "support" to 1))
    }

    @Test
    fun `when result is successful with null unread channels, defaults to zero`() = runTest {
        // given
        doNothing().`when`(globalState).setGroupedUnreadChannels(any())
        val result = Result.Success(
            value = GroupedChannels(
                groups = mapOf(
                    "expired" to GroupedChannelsGroup(channels = emptyList(), unreadChannels = null),
                ),
            ),
        )
        // when
        listener.onGroupedQueryChannelsResult(result, limit = 10, watch = true, presence = false)
        // then
        verify(globalState, times(1)).setGroupedUnreadChannels(mapOf("expired" to 0))
    }

    @Test
    fun `when result is failure, global state should not be updated`() = runTest {
        // given
        val result = Result.Failure(Error.GenericError("Network error"))
        // when
        listener.onGroupedQueryChannelsResult(result, limit = null, watch = false, presence = false)
        // then
        verify(globalState, never()).setGroupedUnreadChannels(any())
    }
}
