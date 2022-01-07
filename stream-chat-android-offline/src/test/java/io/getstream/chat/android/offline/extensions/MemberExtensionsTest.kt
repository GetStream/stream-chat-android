package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.offline.randomMember
import io.getstream.chat.android.offline.randomUser
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class MemberExtensionsTest {

    @Test
    fun `Should update members correctly`() {
        val user1 = randomUser(id = "userId1").apply { name = "userName1" }
        val member1 = randomMember(user = user1)
        val member2 = randomMember()
        val user1Updated = randomUser(id = "userId1").apply { name = "userName2" }

        val result = listOf(member1, member2).updateUsers(mapOf(user1Updated.id to user1Updated))

        result.any { it.user == user1 } shouldBeEqualTo false
        result.any { it.user == user1Updated } shouldBeEqualTo true
    }
}
