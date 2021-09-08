package io.getstream.chat.android.client.models

import com.google.common.truth.Truth.assertThat
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
        assertThat(user.name).isEmpty()
        assertThat(user.extraData[nameField]).isNull()

        assertThat(user.image).isEmpty()
        assertThat(user.extraData[imageField]).isNull()

        user.name = nameValue
        user.image = imageValue

        assertThat(user.name).isEqualTo(nameValue)
        assertThat(user.extraData[nameField]).isEqualTo(nameValue)

        assertThat(user.image).isEqualTo(imageValue)
        assertThat(user.extraData[imageField]).isEqualTo(imageValue)
    }

    @Test
    fun channelPropertyFields() {
        val channel = Channel()
        assertThat(channel.name).isEmpty()
        assertThat(channel.extraData[nameField]).isNull()

        assertThat(channel.image).isEmpty()
        assertThat(channel.extraData[imageField]).isNull()

        channel.name = nameValue
        channel.image = imageValue

        assertThat(channel.name).isEqualTo(nameValue)
        assertThat(channel.extraData[nameField]).isEqualTo(nameValue)

        assertThat(channel.image).isEqualTo(imageValue)
        assertThat(channel.extraData[imageField]).isEqualTo(imageValue)
    }

    @Test
    fun getExternalFieldTest() {
        val user = User()
        assertThat(getExternalField(user, nameField)).isEmpty()

        user.name = nameValue
        assertThat(getExternalField(user, nameField)).isEqualTo(nameValue)

        user.extraData[nameField] = nonStringValue
        assertThat(getExternalField(user, nameField)).isEmpty()
    }

    @Test
    fun noUnread() {
        assertThat(Channel().getUnreadMessagesCount()).isEqualTo(0)
    }

    @Test
    fun totalUnread() {
        assertThat(Channel(read = listOf(getRead(10))).getUnreadMessagesCount()).isEqualTo(10)

        assertThat(
            Channel(
                read = listOf(
                    getRead(10),
                    getRead(10)
                )
            ).getUnreadMessagesCount()
        ).isEqualTo(20)
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

        assertThat(channel.getUnreadMessagesCount()).isEqualTo(unreadUserA + unreadUserB)
        assertThat(channel.getUnreadMessagesCount(userA)).isEqualTo(unreadUserA)
        assertThat(channel.getUnreadMessagesCount(userB)).isEqualTo(unreadUserB)
    }

    private fun getRead(unreadCount: Int, userId: String = ""): ChannelUserRead {
        return ChannelUserRead(User(id = userId), null, unreadCount)
    }
}
