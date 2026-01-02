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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyGenericError
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.randomUserBlock
import io.getstream.result.call.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for the users functionalities of the [ChatClient].
 */
internal class ChatClientUserApiTests : BaseChatClientTest() {

    @Test
    fun updateUsersSuccess() = runTest {
        // given
        val user = randomUser()
        val sut = Fixture()
            .givenUpdateUsersResult(RetroSuccess(listOf(user)).toRetrofitCall())
            .get()
        // when
        val result = sut.updateUsers(listOf(user)).await()
        // then
        verifySuccess(result, listOf(user))
    }

    @Test
    fun updateUsersError() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenUpdateUsersResult(RetroError<List<User>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.updateUsers(emptyList()).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun updateUserSuccess() = runTest {
        // given
        val user = randomUser()
        val sut = Fixture()
            .givenUpdateUsersResult(RetroSuccess(listOf(user)).toRetrofitCall())
            .get()
        // when
        val result = sut.updateUser(user).await()
        // then
        verifySuccess(result, user)
    }

    @Test
    fun updateUserError() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenUpdateUsersResult(RetroError<List<User>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.updateUser(randomUser()).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun blockUserSuccess() = runTest {
        // given
        val blockUserId = randomString()
        val currentUserId = randomString()
        val currentUser = randomUser(id = currentUserId)
        val userBlock = randomUserBlock(userId = blockUserId)
        val plugin = mock<Plugin>().apply {
            doNothing().whenever(this).onBlockUserResult(any())
        }
        val sut = Fixture()
            .givenBlockUserResult(RetroSuccess(userBlock).toRetrofitCall())
            .givenPlugin(plugin)
            .givenUserState(currentUser)
            .givenClientState(currentUser)
            .get()
        // when
        val result = sut.blockUser(blockUserId).await()
        // then
        verifySuccess(result, userBlock)
        verify(plugin).onBlockUserResult(result)
        val expectedUser = currentUser.copy(blockedUserIds = listOf(blockUserId))
        verify(userStateService).onUserUpdated(expectedUser)
        verify(mutableClientState).setUser(expectedUser)
    }

    @Test
    fun blockUserError() = runTest {
        // given
        val blockUserId = randomString()
        val currentUserId = randomString()
        val currentUser = randomUser(id = currentUserId)
        val errorCode = positiveRandomInt()
        val plugin = mock<Plugin>().apply {
            doNothing().whenever(this).onBlockUserResult(any())
        }
        val sut = Fixture()
            .givenBlockUserResult(RetroError<UserBlock>(errorCode).toRetrofitCall())
            .givenPlugin(plugin)
            .givenUserState(currentUser)
            .givenClientState(currentUser)
            .get()
        // when
        val result = sut.blockUser(blockUserId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(plugin).onBlockUserResult(result)
        verify(userStateService, never()).onUserUpdated(any())
        verify(mutableClientState, never()).setUser(any())
    }

    @Test
    fun unblockUserSuccess() = runTest {
        // given
        val blockedUserId = randomString()
        val currentUserId = randomString()
        val currentUser = randomUser(id = currentUserId, blockedUserIds = listOf(blockedUserId))
        val plugin = mock<Plugin>().apply {
            doNothing().whenever(this).onUnblockUserResult(any(), any())
        }
        val sut = Fixture()
            .givenUnblockUserApiResult(RetroSuccess(Unit).toRetrofitCall())
            .givenPlugin(plugin)
            .givenUserState(currentUser)
            .givenClientState(currentUser)
            .get()
        // when
        val result = sut.unblockUser(blockedUserId).await()
        // then
        verifySuccess(result, Unit)
        verify(plugin).onUnblockUserResult(blockedUserId, result)
        val expectedUser = currentUser.copy(blockedUserIds = emptyList())
        verify(userStateService).onUserUpdated(expectedUser)
        verify(mutableClientState).setUser(expectedUser)
    }

    @Test
    fun unblockUserError() = runTest {
        // given
        val blockedUserId = randomString()
        val currentUserId = randomString()
        val currentUser = randomUser(id = currentUserId, blockedUserIds = listOf(blockedUserId))
        val errorCode = positiveRandomInt()
        val plugin = mock<Plugin>().apply {
            doNothing().whenever(this).onUnblockUserResult(any(), any())
        }
        val sut = Fixture()
            .givenUnblockUserApiResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .givenPlugin(plugin)
            .givenUserState(currentUser)
            .givenClientState(currentUser)
            .get()
        // when
        val result = sut.unblockUser(blockedUserId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(plugin).onUnblockUserResult(blockedUserId, result)
        verify(userStateService, never()).onUserUpdated(any())
        verify(mutableClientState, never()).setUser(any())
    }

    @Test
    fun queryBlockedUsersSuccess() = runTest {
        // given
        val userBlocks = listOf(randomUserBlock())
        val plugin = org.mockito.kotlin.mock<Plugin>().apply {
            doNothing().whenever(this).onQueryBlockedUsersResult(any())
        }
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenQueryBlockedUsersUserApiResult(RetroSuccess(userBlocks).toRetrofitCall())
            .get()
        // when
        val result = sut.queryBlockedUsers().await()
        // then
        verifySuccess(result, userBlocks)
        verify(plugin).onQueryBlockedUsersResult(result)
    }

    @Test
    fun queryBlockedUsersError() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val plugin = org.mockito.kotlin.mock<Plugin>().apply {
            doNothing().whenever(this).onQueryBlockedUsersResult(any())
        }
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenQueryBlockedUsersUserApiResult(RetroError<List<UserBlock>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.queryBlockedUsers().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(plugin).onQueryBlockedUsersResult(result)
    }

    @Test
    fun partialUpdateUserSuccess() = runTest {
        // given
        val id = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val user = randomUser(id = id)
        val sut = Fixture()
            .givenPartialUpdateUserResult(RetroSuccess(listOf(user)).toRetrofitCall())
            .givenUserState(user)
            .get()
        // when
        val result = sut.partialUpdateUser(id, set, unset).await()
        // then
        verifySuccess(result, user)
    }

    @Test
    fun partialUpdateUserNotCurrentUserError() = runTest {
        // given
        val id = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val user = randomUser(id = id)
        val currentUser = randomUser() // Different from the one updated
        val sut = Fixture()
            .givenPartialUpdateUserResult(RetroSuccess(listOf(user)).toRetrofitCall())
            .givenUserState(currentUser)
            .get()
        // when
        val result = sut.partialUpdateUser(id, set, unset).await()
        // then
        verifyGenericError(
            result = result,
            message = "The client-side partial update allows you to update only the current user. " +
                "Make sure the user is set before updating it.",
        )
    }

    @Test
    fun partialUpdateUserError() = runTest {
        // given
        val id = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val errorCode = positiveRandomInt()
        val currentUser = randomUser(id = id)
        val sut = Fixture()
            .givenPartialUpdateUserResult(RetroError<List<User>>(errorCode).toRetrofitCall())
            .givenUserState(currentUser)
            .get()
        // when
        val result = sut.partialUpdateUser(id, set, unset).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun queryUsersSuccess() = runTest {
        // when
        val query = Mother.randomQueryUsersRequest()
        val user = randomUser()
        val sut = Fixture()
            .givenQueryUsersResult(RetroSuccess(listOf(user)).toRetrofitCall())
            .get()
        // when
        val result = sut.queryUsers(query).await()
        // then
        verifySuccess(result, listOf(user))
    }

    @Test
    fun queryUsersError() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenQueryUsersResult(RetroError<List<User>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.queryUsers(Mother.randomQueryUsersRequest()).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    internal inner class Fixture {

        fun givenUpdateUsersResult(result: Call<List<User>>) = apply {
            whenever(api.updateUsers(any())) doReturn result
        }

        fun givenBlockUserResult(result: Call<UserBlock>) = apply {
            whenever(api.blockUser(any())) doReturn result
        }

        fun givenUnblockUserApiResult(result: Call<Unit>) = apply {
            whenever(api.unblockUser(any())) doReturn result
        }

        fun givenQueryBlockedUsersUserApiResult(result: Call<List<UserBlock>>) = apply {
            whenever(api.queryBlockedUsers()) doReturn result
        }

        fun givenPartialUpdateUserResult(result: Call<List<User>>) = apply {
            whenever(api.partialUpdateUser(any(), any(), any())) doReturn result
        }

        fun givenQueryUsersResult(result: Call<List<User>>) = apply {
            whenever(api.queryUsers(any())) doReturn result
        }

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun givenUserState(user: User) = apply {
            whenever(userStateService.state) doReturn UserState.UserSet(user)
        }

        fun givenClientState(user: User) = apply {
            whenever(mutableClientState.user) doReturn MutableStateFlow(user)
        }

        fun get(): ChatClient = chatClient
    }
}
