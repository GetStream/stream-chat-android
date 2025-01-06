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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.result.Error
import io.getstream.result.Result
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class QueryBlockedUsersListenerStateTest {

    private val globalState: MutableGlobalState = mock()
    private val listener = QueryBlockedUsersListenerState(globalState)

    @Test
    fun `when queryBlockedUsers is successful, the blocked user ids should be set in the global state`() {
        // given
        doNothing().whenever(globalState).setBlockedUserIds(any())
        val blockedUsers = listOf(
            UserBlock("user1", "user2", Date()),
        )
        val result = Result.Success(blockedUsers)
        // when
        listener.onQueryBlockedUsersResult(result)
        // then
        verify(globalState, times(1)).setBlockedUserIds(listOf("user2"))
    }

    @Test
    fun `when queryBlockedUsers is unsuccessful, the blocked user ids should not be set in the global state`() {
        // given
        val result = Result.Failure(Error.GenericError("Generic error"))
        // when
        listener.onQueryBlockedUsersResult(result)
        // then
        verify(globalState, times(0)).setBlockedUserIds(any())
    }
}
