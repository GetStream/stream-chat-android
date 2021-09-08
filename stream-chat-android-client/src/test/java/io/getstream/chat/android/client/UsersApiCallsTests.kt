package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.test.TestCoroutineExtension
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
        mock = MockClientBuilder(testCoroutines.scope)
        client = mock.build()
    }

    @Test
    fun banSuccess() {
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
        ).execute()

        verifySuccess(
            result,
            Unit
        )
    }

    @Test
    fun unbanSuccess() {
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
            mock.api.flagUser(targetUserId)
        ).thenReturn(RetroSuccess(flag).toRetrofitCall())

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
            mock.api.flagUser(targetUserId)
        ).thenReturn(RetroSuccess(flag).toRetrofitCall())

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
            mock.api.flagMessage(targetMessageId)
        ).thenReturn(RetroSuccess(flag).toRetrofitCall())

        val result = client.flagMessage(targetMessageId).execute()

        verifySuccess(result, flag)
    }

    @Test
    fun getUsersSuccess() {
        val user = User().apply { id = "a-user" }

        val request = QueryUsersRequest(Filters.eq("id", "1"), 0, 1)

        Mockito.`when`(
            mock.api.queryUsers(request)
        ).thenReturn(RetroSuccess(listOf(user)).toRetrofitCall())

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
            mock.api.removeMembers(
                mock.channelType,
                mock.channelId,
                listOf("a-id", "b-id"),
            )
        ).thenReturn(RetroSuccess(channel).toRetrofitCall())

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
            Date(2),
            null,
        )

        Mockito.`when`(
            mock.api.muteUser(
                targetUser.id, null
            )
        ).thenReturn(RetroSuccess(mute).toRetrofitCall())

        val result = client.muteUser(targetUser.id).execute()

        verifySuccess(result, mute)
    }

    @Test
    fun unmuteUserSuccess() {
        val targetUser = User().apply { id = "target-id" }

        Mockito.`when`(
            mock.api.unmuteUser(targetUser.id)
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.unmuteUser(targetUser.id).execute()

        verifySuccess(result, Unit)
    }
}
