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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito
import java.util.Date

internal class UsersApiCallsTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    lateinit var client: ChatClient
    lateinit var mock: MockClientBuilder

    @BeforeEach
    fun before() {
        mock = MockClientBuilder(testCoroutines)
        client = mock.build()
    }

    @Test
    fun banSuccess() = runTest {
        val targetUserId = "target-id"
        val timeout = 13
        val reason = "reason"

        Mockito.`when`(
            mock.api.banUser(
                targetId = targetUserId,
                timeout = timeout,
                reason = reason,
                channelType = mock.channelType,
                channelId = mock.channelId,
                shadow = false,
            )
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.banUser(
            targetUserId,
            mock.channelType,
            mock.channelId,
            reason,
            timeout
        ).await()

        verifySuccess(
            result,
            Unit
        )
    }

    @Test
    fun unbanSuccess() = runTest {
        val targetUserId = "target-id"

        Mockito.`when`(
            mock.api.unbanUser(
                targetUserId,
                mock.channelType,
                mock.channelId,
                shadow = false,
            )
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.unbanUser(
            targetUserId,
            mock.channelType,
            mock.channelId
        ).await()

        verifySuccess(
            result,
            Unit
        )
    }

    @Test
    fun flagSuccess() = runTest {
        val targetUserId = "target-id"
        val user = User("user-id")
        val targetUser = User(targetUserId)
        val date = Date()
        val flag = Flag(
            user,
            targetUser,
            "",
            "",
            false,
            date,
            date,
            date,
            date,
            date
        )

        Mockito.`when`(
            mock.api.flagUser(targetUserId)
        ).thenReturn(RetroSuccess(flag).toRetrofitCall())

        val result = client.flagUser(targetUserId).await()

        verifySuccess(result, flag)
    }

    @Test
    fun flagUserSuccess() = runTest {
        val targetUserId = "target-id"
        val user = User("user-id")
        val targetUser = User(targetUserId)
        val date = Date()
        val flag = Flag(
            user,
            targetUser,
            "",
            "",
            false,
            date,
            date,
            date,
            date,
            date
        )

        Mockito.`when`(
            mock.api.flagUser(targetUserId)
        ).thenReturn(RetroSuccess(flag).toRetrofitCall())

        val result = client.flagUser(targetUserId).await()

        verifySuccess(result, flag)
    }

    @Test
    fun flagMessageSuccess() = runTest {
        val targetMessageId = "message-id"
        val user = User("user-id")
        val date = Date()
        val flag = Flag(
            user,
            null,
            targetMessageId,
            "",
            false,
            date,
            date,
            date,
            date,
            date
        )

        Mockito.`when`(
            mock.api.flagMessage(targetMessageId)
        ).thenReturn(RetroSuccess(flag).toRetrofitCall())

        val result = client.flagMessage(targetMessageId).await()

        verifySuccess(result, flag)
    }

    @Test
    fun getUsersSuccess() = runTest {
        val user = User(id = "a-user")

        val request = QueryUsersRequest(Filters.eq("id", "1"), 0, 1)

        Mockito.`when`(
            mock.api.queryUsers(request)
        ).thenReturn(RetroSuccess(listOf(user)).toRetrofitCall())

        val result = client.queryUsers(
            request
        ).await()

        verifySuccess(result, listOf(user))
    }

    @Test
    fun removeMembersSuccess() = runTest {
        val channel = Channel(id = "a-channel")

        Mockito.`when`(
            mock.api.removeMembers(
                mock.channelType,
                mock.channelId,
                listOf("a-id", "b-id"),
                null
            )
        ).thenReturn(RetroSuccess(channel).toRetrofitCall())

        val result =
            client.removeMembers(mock.channelType, mock.channelId, listOf("a-id", "b-id")).await()

        verifySuccess(result, channel)
    }

    @Test
    fun muteUserSuccess() = runTest {
        val targetUser = User(id = "target-id")
        val mute = Mute(
            mock.user,
            targetUser,
            Date(1),
            Date(2),
            null,
        )

        Mockito.`when`(
            mock.api.muteUser(
                targetUser.id, null
            )
        ).thenReturn(RetroSuccess(mute).toRetrofitCall())

        val result = client.muteUser(targetUser.id).await()

        verifySuccess(result, mute)
    }

    @Test
    fun unmuteUserSuccess() = runTest {
        val targetUser = User(id = "target-id")

        Mockito.`when`(
            mock.api.unmuteUser(targetUser.id)
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.unmuteUser(targetUser.id).await()

        verifySuccess(result, Unit)
    }
}
