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
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBannedUser
import io.getstream.chat.android.randomDateOrNull
import io.getstream.chat.android.randomFlag
import io.getstream.chat.android.randomMute
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for the moderation functionalities of the [ChatClient].
 */
internal class ChatClientModerationApiTests : BaseChatClientTest() {

    @Test
    fun muteUserSuccess() = runTest {
        // given
        val userId = randomString()
        val timeout = positiveRandomInt()
        val mute = randomMute()
        whenever(api.muteUser(any(), any()))
            .thenReturn(RetroSuccess(mute).toRetrofitCall())
        // when
        val result = chatClient.muteUser(userId, timeout).await()
        // then
        verifySuccess(result, mute)
    }

    @Test
    fun muteUserError() = runTest {
        // given
        val userId = randomString()
        val timeout = positiveRandomInt()
        val errorCode = positiveRandomInt()
        whenever(api.muteUser(any(), any()))
            .thenReturn(RetroError<Mute>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.muteUser(userId, timeout).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun testUnmuteUserSuccess() = runTest {
        // given
        val userId = randomString()
        whenever(api.unmuteUser(any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.unmuteUser(userId).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun testUnmuteUserError() = runTest {
        // given
        val userId = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.unmuteUser(any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.unmuteUser(userId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun muteCurrentUserSuccess() = runTest {
        // given
        val mute = randomMute()
        whenever(api.muteCurrentUser()).thenReturn(RetroSuccess(mute).toRetrofitCall())
        // when
        val result = chatClient.muteCurrentUser().await()
        // then
        verifySuccess(result, mute)
    }

    @Test
    fun muteCurrentUserError() = runTest {
        // given
        val errorCode = positiveRandomInt()
        whenever(api.muteCurrentUser()).thenReturn(RetroError<Mute>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.muteCurrentUser().await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun unmuteCurrentUserSuccess() = runTest {
        // given
        whenever(api.unmuteCurrentUser()).thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.unmuteCurrentUser().await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun unmuteCurrentUserError() = runTest {
        // given
        val errorCode = positiveRandomInt()
        whenever(api.unmuteCurrentUser()).thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.unmuteCurrentUser().await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun muteChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val expiration = positiveRandomInt()
        whenever(api.muteChannel(any(), any(), any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.muteChannel(channelType, channelId, expiration).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun muteChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val expiration = positiveRandomInt()
        val errorCode = positiveRandomInt()
        whenever(api.muteChannel(any(), any(), any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.muteChannel(channelType, channelId, expiration).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun unmuteChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        whenever(api.unmuteChannel(any(), any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.unmuteChannel(channelType, channelId).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun unmuteChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.unmuteChannel(any(), any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.unmuteChannel(channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun flagUserSuccess() = runTest {
        // given
        val targetUserId = randomString()
        val flag = randomFlag()
        val reason = randomString()
        val customData = emptyMap<String, String>()
        whenever(api.flagUser(any(), any(), any()))
            .thenReturn(RetroSuccess(flag).toRetrofitCall())
        // when
        val result = chatClient.flagUser(targetUserId, reason, customData).await()
        // then
        verifySuccess(result, flag)
    }

    @Test
    fun flagUserError() = runTest {
        // given
        val targetUserId = randomString()
        val errorCode = positiveRandomInt()
        val reason = randomString()
        val customData = emptyMap<String, String>()
        whenever(api.flagUser(any(), any(), any()))
            .thenReturn(RetroError<Flag>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.flagUser(targetUserId, reason, customData).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun unflagUserSuccess() = runTest {
        // given
        val targetUserId = randomString()
        val flag = randomFlag()
        whenever(api.unflagUser(any()))
            .thenReturn(RetroSuccess(flag).toRetrofitCall())
        // when
        val result = chatClient.unflagUser(targetUserId).await()
        // then
        verifySuccess(result, flag)
    }

    @Test
    fun unflagUserError() = runTest {
        // given
        val targetUserId = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.unflagUser(any()))
            .thenReturn(RetroError<Flag>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.unflagUser(targetUserId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun flagMessageSuccess() = runTest {
        // given
        val messageId = randomString()
        val reason = randomString()
        val customData = emptyMap<String, String>()
        val flag = randomFlag()
        whenever(api.flagMessage(any(), any(), any()))
            .thenReturn(RetroSuccess(flag).toRetrofitCall())
        // when
        val result = chatClient.flagMessage(messageId, reason, customData).await()
        // then
        verifySuccess(result, flag)
    }

    @Test
    fun flagMessageError() = runTest {
        // given
        val messageId = randomString()
        val reason = randomString()
        val customData = emptyMap<String, String>()
        val errorCode = positiveRandomInt()
        whenever(api.flagMessage(any(), any(), any()))
            .thenReturn(RetroError<Flag>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.flagMessage(messageId, reason, customData).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun unflagMessageSuccess() = runTest {
        // given
        val messageId = randomString()
        val flag = randomFlag()
        whenever(api.unflagMessage(any()))
            .thenReturn(RetroSuccess(flag).toRetrofitCall())
        // when
        val result = chatClient.unflagMessage(messageId).await()
        // then
        verifySuccess(result, flag)
    }

    @Test
    fun unflagMessageError() = runTest {
        // given
        val messageId = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.unflagMessage(any()))
            .thenReturn(RetroError<Flag>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.unflagMessage(messageId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun banUserSuccess() = runTest {
        // given
        val targetId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val reason = randomString()
        val timeout = positiveRandomInt()
        whenever(api.banUser(any(), any(), any(), any(), any(), any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.banUser(targetId, channelType, channelId, reason, timeout).await()
        // then
        verifySuccess(result, Unit)
        verify(api, times(1)).banUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
            reason = reason,
            timeout = timeout,
            shadow = false,
        )
    }

    @Test
    fun banUserError() = runTest {
        // given
        val targetId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val reason = randomString()
        val timeout = positiveRandomInt()
        val errorCode = positiveRandomInt()
        whenever(api.banUser(any(), any(), any(), any(), any(), any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.banUser(targetId, channelType, channelId, reason, timeout).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api, times(1)).banUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
            reason = reason,
            timeout = timeout,
            shadow = false,
        )
    }

    @Test
    fun unbanUserSuccess() = runTest {
        // given
        val targetId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        whenever(api.unbanUser(any(), any(), any(), any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.unbanUser(targetId, channelType, channelId).await()
        // then
        verifySuccess(result, Unit)
        verify(api, times(1)).unbanUser(targetId, channelType, channelId, shadow = false)
    }

    @Test
    fun unbanUserError() = runTest {
        // given
        val targetId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.unbanUser(any(), any(), any(), any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.unbanUser(targetId, channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api, times(1)).unbanUser(targetId, channelType, channelId, shadow = false)
    }

    @Test
    fun shadowBanUserSuccess() = runTest {
        // given
        val targetId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val reason = randomString()
        val timeout = positiveRandomInt()
        whenever(api.banUser(any(), any(), any(), any(), any(), any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.shadowBanUser(targetId, channelType, channelId, reason, timeout).await()
        // then
        verifySuccess(result, Unit)
        verify(api, times(1)).banUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
            reason = reason,
            timeout = timeout,
            shadow = true,
        )
    }

    @Test
    fun shadowBanUserError() = runTest {
        // given
        val targetId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val reason = randomString()
        val timeout = positiveRandomInt()
        val errorCode = positiveRandomInt()
        whenever(api.banUser(any(), any(), any(), any(), any(), any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.shadowBanUser(targetId, channelType, channelId, reason, timeout).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api, times(1)).banUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
            reason = reason,
            timeout = timeout,
            shadow = true,
        )
    }

    @Test
    fun removeShadowBanSuccess() = runTest {
        // given
        val targetId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        whenever(api.unbanUser(any(), any(), any(), any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.removeShadowBan(targetId, channelType, channelId).await()
        // then
        verifySuccess(result, Unit)
        verify(api, times(1)).unbanUser(targetId, channelType, channelId, shadow = true)
    }

    @Test
    fun removeShadowBanError() = runTest {
        // given
        val targetId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.unbanUser(any(), any(), any(), any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.removeShadowBan(targetId, channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api, times(1)).unbanUser(targetId, channelType, channelId, shadow = true)
    }

    @Test
    fun queryBannedUsersSuccess() = runTest {
        // given
        val filter = Filters.neutral()
        val sort = QuerySortByField.ascByName<BannedUsersSort>("created_at")
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val createdAtAfter = randomDateOrNull()
        val createdAtAfterOrEqual = randomDateOrNull()
        val createdAtBefore = randomDateOrNull()
        val createdAtBeforeOrEqual = randomDateOrNull()
        val bannedUsers = listOf(randomBannedUser())
        whenever(api.queryBannedUsers(any(), any(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(RetroSuccess(bannedUsers).toRetrofitCall())
        // when
        val result = chatClient.queryBannedUsers(
            filter = filter,
            sort = sort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        ).await()
        // then
        verifySuccess(result, bannedUsers)
    }

    @Test
    fun queryBannedUsersError() = runTest {
        // given
        val filter = Filters.neutral()
        val sort = QuerySortByField.ascByName<BannedUsersSort>("created_at")
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val createdAtAfter = randomDateOrNull()
        val createdAtAfterOrEqual = randomDateOrNull()
        val createdAtBefore = randomDateOrNull()
        val createdAtBeforeOrEqual = randomDateOrNull()
        val errorCode = positiveRandomInt()
        whenever(api.queryBannedUsers(any(), any(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(RetroError<List<BannedUser>>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.queryBannedUsers(
            filter = filter,
            sort = sort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        ).await()
        // then
        verifyNetworkError(result, errorCode)
    }
}
