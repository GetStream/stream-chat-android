package com.getstream.sdk.chat.utils.extension

import com.getstream.sdk.chat.createMember
import com.getstream.sdk.chat.createUser
import com.getstream.sdk.chat.utils.extensions.getLastActive
import com.getstream.sdk.chat.utils.extensions.getOtherUsers
import io.getstream.chat.android.client.models.Member
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.util.Date

internal class MemberTest {
    private val date1 = Date()
    private val otherUser1 = createUser(lastActive = date1)

    private val date2 = Date(date1.time + 1000)
    private val otherUser2 = createUser(lastActive = date2)

    private val date3 = Date(date1.time + 2000)
    private val currentUser = createUser(lastActive = date3)

    private val members = listOf(
        createMember(user = otherUser1),
        createMember(user = otherUser2),
        createMember(user = currentUser)
    )

    @Test
    fun `should return last active date of members other that the current user`() {
        val result = members.getLastActive(currentUser)

        result `should be equal to` date2
    }

    @Test
    fun `should return users not related to the current user`() {
        val result = members.getOtherUsers(currentUser)

        result `should be equal to` listOf(otherUser1, otherUser2)
    }

    @Test
    fun `should return empty user list when members list is null`() {
        val result = listOf<Member>().getOtherUsers(currentUser)

        result `should be equal to` emptyList()
    }
}
