package io.getstream.chat.android.client.poc

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.CompletableResponse
import io.getstream.chat.android.client.MockClientBuilder
import io.getstream.chat.android.client.User
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.poc.utils.RetroSuccess
import io.getstream.chat.android.client.poc.utils.verifySuccess
import io.getstream.chat.android.client.requests.QueryUsers
import io.getstream.chat.android.client.rest.BanUserRequest
import io.getstream.chat.android.client.rest.FlagResponse
import io.getstream.chat.android.client.rest.QueryUserListResponse
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class UsersApiCallsTests {

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
                mock.apiKey, mock.connectionId,
                BanUserRequest(targetUserId, timeout, reason, mock.channelType, mock.channelId)
            )
        ).thenReturn(RetroSuccess(CompletableResponse()))

        val result = client.banUser(
            targetUserId,
            mock.channelType,
            mock.channelId,
            reason,
            timeout
        ).execute()

        verifySuccess(result, CompletableResponse())
    }

    @Test
    fun unBanSuccess() {

        val targetUserId = "target-id"

        Mockito.`when`(
            mock.retrofitApi.unBanUser(
                mock.apiKey, mock.connectionId,
                targetUserId, mock.channelType, mock.channelId
            )
        ).thenReturn(RetroSuccess(CompletableResponse()))

        val result = client.unBanUser(
            targetUserId,
            mock.channelType,
            mock.channelId
        ).execute()

        verifySuccess(result, CompletableResponse())
    }

    @Test
    fun flagSuccess() {

        val targetUserId = "target-id"
        val flag = Flag().apply {
            user = User(targetUserId)
        }

        Mockito.`when`(
            mock.retrofitApi.flag(
                mock.apiKey, mock.userId, mock.connectionId,
                mapOf(Pair("target_user_id", targetUserId))
            )
        ).thenReturn(RetroSuccess(FlagResponse(flag)))

        val result = client.flag(
            targetUserId
        ).execute()

        verifySuccess(result, FlagResponse(flag))
    }

    @Test
    fun getUsersSuccess() {

        val user = User("a-user")

        Mockito.`when`(
            mock.retrofitApi.queryUsers(
                mock.apiKey, mock.connectionId,
                QueryUsers()
            )
        ).thenReturn(RetroSuccess(QueryUserListResponse(listOf(user))))

        val result = client.getUsers(
            QueryUsers()
        ).execute()

        verifySuccess(result, listOf(user))
    }
}