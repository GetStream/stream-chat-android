package io.getstream.chat.android.client.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtensionsTests {

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
    fun unreadCountOfChannel() {
        assertThat(Channel().getUnreadMessagesCount()).isEqualTo(0)

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

    private fun getRead(unreadCount: Int, userId: String = ""): ChannelUserRead {
        return ChannelUserRead(User(id = userId), null, unreadCount)
    }
}