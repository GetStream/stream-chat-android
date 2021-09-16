package io.getstream.chat.android.client.models

import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Test

internal class ExtensionsTests {

    val nameValue = "bob"
    val nameField = "name"
    val imageValue = "http://image"
    val imageField = "image"
    val nonStringValue = 13

    @Test
    fun userPropertyFields() {
        val user = User()
        user.name.shouldBeEmpty()
        user.extraData[nameField].shouldBeNull()

        user.image.shouldBeEmpty()
        user.extraData[imageField].shouldBeNull()

        user.name = nameValue
        user.image = imageValue

        user.name shouldBeEqualTo nameValue
        user.extraData[nameField] shouldBeEqualTo nameValue

        user.image shouldBeEqualTo imageValue
        user.extraData[imageField] shouldBeEqualTo imageValue
    }

    @Test
    fun channelPropertyFields() {
        val channel = Channel()
        channel.name.shouldBeEmpty()
        channel.extraData[nameField].shouldBeNull()

        channel.image.shouldBeEmpty()
        channel.extraData[imageField].shouldBeNull()

        channel.name = nameValue
        channel.image = imageValue

        channel.name shouldBeEqualTo nameValue
        channel.extraData[nameField] shouldBeEqualTo nameValue

        channel.image shouldBeEqualTo imageValue
        channel.extraData[imageField] shouldBeEqualTo imageValue
    }

    @Test
    fun getExternalFieldTest() {
        val user = User()
        getExternalField(user, nameField).shouldBeEmpty()

        user.name = nameValue
        getExternalField(user, nameField) shouldBeEqualTo nameValue

        user.extraData[nameField] = nonStringValue
        getExternalField(user, nameField).shouldBeEmpty()
    }

    @Test
    fun noUnread() {
        Channel().getUnreadMessagesCount() shouldBeEqualTo 0
    }

    @Test
    fun totalUnread() {
        Channel(read = listOf(getRead(10))).getUnreadMessagesCount() shouldBeEqualTo 10

        Channel(
            read = listOf(
                getRead(10),
                getRead(10)
            )
        ).getUnreadMessagesCount() shouldBeEqualTo 20
    }

    @Test
    fun unreadByUsers() {

        val userA = "user-a"
        val userB = "user-b"
        val unreadUserA = 10
        val unreadUserB = 5

        val channel = Channel(
            read = listOf(
                getRead(unreadUserA, userA),
                getRead(unreadUserB, userB)
            )
        )

        channel.getUnreadMessagesCount() shouldBeEqualTo unreadUserA + unreadUserB
        channel.getUnreadMessagesCount(userA) shouldBeEqualTo unreadUserA
        channel.getUnreadMessagesCount(userB) shouldBeEqualTo unreadUserB
    }

    private fun getRead(unreadCount: Int, userId: String = ""): ChannelUserRead {
        return ChannelUserRead(User(id = userId), null, unreadCount)
    }
}
