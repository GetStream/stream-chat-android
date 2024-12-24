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

import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever

internal class UnblockUserListenerStateTest {

    private val globalState: MutableGlobalState = mock()
    private val listener = UnblockUserListenerState(globalState)

    @Test
    fun `when unblockUser is successful and user is blocked, the blocked user should be removed from the global state`() {
        // given
        val blockedUsers = listOf("user1", "user2")
        whenever(globalState.blockedUserIds) doReturn MutableStateFlow(blockedUsers)
        doNothing().whenever(globalState).setBlockedUserIds(any())
        val result = Result.Success(value = Unit)
        // when
        listener.onUnblockUserResult("user2", result)
        // then
        verify(globalState, times(1)).setBlockedUserIds(listOf("user1"))
    }

    @Test
    fun `when unblockUser is successful and user is not blocked, the blocked user should not be removed from the global state`() {
        // given
        val blockedUsers = listOf("user1")
        whenever(globalState.blockedUserIds) doReturn MutableStateFlow(blockedUsers)
        doNothing().whenever(globalState).setBlockedUserIds(any())
        val result = Result.Success(Unit)
        // when
        listener.onUnblockUserResult("user2", result)
        // then
        verify(globalState, never()).setBlockedUserIds(any())
    }

    @Test
    fun `when unblockUser fails, the blocked user should not be removed from the global state`() {
        // given
        val blockedUsers = listOf("user1", "user2")
        whenever(globalState.blockedUserIds) doReturn MutableStateFlow(blockedUsers)
        doNothing().whenever(globalState).setBlockedUserIds(any())
        val result = Result.Failure(Error.GenericError("Generic error"))
        // when
        listener.onUnblockUserResult("user2", result)
        // then
        verify(globalState, never()).setBlockedUserIds(any())
    }
}
