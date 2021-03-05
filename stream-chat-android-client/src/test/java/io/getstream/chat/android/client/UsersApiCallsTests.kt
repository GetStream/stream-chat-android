package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.models.BanUserRequest
import io.getstream.chat.android.client.api.models.ChannelResponse
import io.getstream.chat.android.client.api.models.CompletableResponse
import io.getstream.chat.android.client.api.models.FlagResponse
import io.getstream.chat.android.client.api.models.MuteUserRequest
import io.getstream.chat.android.client.api.models.MuteUserResponse
import io.getstream.chat.android.client.api.models.QueryUserListResponse
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.RemoveMembersRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifySuccess
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.Date

internal class UsersApiCallsTests {

    lateinit var client: ChatClient
    lateinit var mock: MockClientBuilder

    @Before
    fun before() {
        mock = MockClientBuilder()
        client = mock.build()
    }

    @Test
    fun banSuccess() {

        val targetUserId = "target-id"
        val timeout = 13
        val reason = "reason"

        Mockito.`when`(
            mock.retrofitApi.banUser(
                mock.apiKey,
                mock.connectionId,
                BanUserRequest(targetUserId, timeout, reason, mock.channelType, mock.channelId, shadow = false)
            )
        ).thenReturn(RetroSuccess(CompletableResponse()).toRetrofitCall())

        val result = client.banUser(
            targetUserId,
            mock.channelType,
            mock.channelId,
            reason,
            timeout
        ).execute()

        verifySuccess(
            result,
            Unit
        )
    }

    @Test
    fun unBanSuccess() {

        val targetUserId = "target-id"

        Mockito.`when`(
            mock.retrofitApi.unBanUser(
                mock.apiKey,
                mock.connectionId,
                targetUserId,
                mock.channelType,
                mock.channelId,
                shadow = false,
            )
        ).thenReturn(RetroSuccess(CompletableResponse()).toRetrofitCall())

        val result = client.unBanUser(
            targetUserId,
            mock.channelType,
            mock.channelId
        ).execute()

        verifySuccess(
            result,
            Unit
        )
    }

    @Test
    fun flagSuccess() {

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
            mock.retrofitApi.flag(
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                mapOf(Pair("target_user_id", targetUserId))
            )
        ).thenReturn(RetroSuccess(FlagResponse(flag)).toRetrofitCall())

        val result = client.flagUser(targetUserId).execute()

        verifySuccess(result, flag)
    }

    @Test
    fun flagUserSuccess() {

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
            mock.retrofitApi.flag(
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                mapOf(Pair("target_user_id", targetUserId))
            )
        ).thenReturn(RetroSuccess(FlagResponse(flag)).toRetrofitCall())

        val result = client.flagUser(targetUserId).execute()

        verifySuccess(result, flag)
    }

    @Test
    fun flagMessageSuccess() {

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
            mock.retrofitApi.flag(
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                mapOf(Pair("target_message_id", targetMessageId))
            )
        ).thenReturn(RetroSuccess(FlagResponse(flag)).toRetrofitCall())

        val result = client.flagMessage(targetMessageId).execute()

        verifySuccess(result, flag)
    }

    @Test
    fun getUsersSuccess() {

        val user = User().apply { id = "a-user" }

        val request = QueryUsersRequest(Filters.eq("id", "1"), 0, 1)

        Mockito.`when`(
            mock.retrofitApi.queryUsers(
                mock.apiKey,
                mock.connectionId,
                request
            )
        ).thenReturn(RetroSuccess(QueryUserListResponse(listOf(user))).toRetrofitCall())

        val result = client.queryUsers(
            request
        ).execute()

        verifySuccess(result, listOf(user))
    }

    @Test
    fun removeMembersSuccess() {

        val channel = Channel()
            .apply { id = "a-channel" }

        Mockito.`when`(
            mock.retrofitApi.removeMembers(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                RemoveMembersRequest(listOf("a-id", "b-id"))
            )
        ).thenReturn(RetroSuccess(ChannelResponse(channel)).toRetrofitCall())

        val result =
            client.removeMembers(mock.channelType, mock.channelId, listOf("a-id", "b-id")).execute()

        verifySuccess(result, channel)
    }

    @Test
    fun muteUserSuccess() {

        val targetUser = User().apply { id = "target-id" }
        val mute = Mute(
            mock.user,
            targetUser,
            Date(1),
            Date(2)
        )

        Mockito.`when`(
            mock.retrofitApi.muteUser(
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                MuteUserRequest(targetUser.id, mock.userId)
            )
        ).thenReturn(RetroSuccess(MuteUserResponse(mute, mock.user)).toRetrofitCall())

        val result = client.muteUser(targetUser.id).execute()

        verifySuccess(result, mute)
    }

    @Test
    fun unMuteUserSuccess() {

        val targetUser = User().apply { id = "target-id" }

        Mockito.`when`(
            mock.retrofitApi.unMuteUser(
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                MuteUserRequest(targetUser.id, mock.userId)
            )
        ).thenReturn(RetroSuccess(CompletableResponse()).toRetrofitCall())

        val result = client.unmuteUser(targetUser.id).execute()

        verifySuccess(result, Unit)
    }
}
