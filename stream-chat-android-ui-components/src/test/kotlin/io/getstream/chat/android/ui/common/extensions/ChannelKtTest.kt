package io.getstream.chat.android.ui.common.extensions

import io.getstream.chat.android.ui.createMember
import io.getstream.chat.android.ui.createUser
import io.getstream.chat.android.ui.randomChannel
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class ChannelKtTest {

    @Test
    fun `given a channel has only one member it should have the name of the member`() {
        val randomUser = createUser()
        val member = createMember(user = randomUser)
        val oneMemberChannel = randomChannel(members = listOf(member))

        randomUser.name `should be equal to` oneMemberChannel.getDisplayName(mock(), randomUser)
    }
}
