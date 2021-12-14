package io.getstream.chat.android.client

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.client.models.User
import java.util.UUID

internal object Mother {
    private val fixture: JFixture
        get() = JFixture()

    fun randomAttachment(attachmentBuilder: Attachment.() -> Unit = { }): Attachment {
        return KFixture(fixture) {
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Attachment>().apply(attachmentBuilder)
    }

    fun randomChannel(channelBuilder: Channel.() -> Unit = { }): Channel {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
            sameInstance(Message::class.java, mock())
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Channel>().apply(channelBuilder)
    }

    fun randomUser(userBuilder: User.() -> Unit = { }): User {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
        } <User>().apply(userBuilder)
    }

    fun randomString(): String = UUID.randomUUID().toString()

    fun randomDevice(
        token: String = randomString(),
        pushProvider: PushProvider = PushProvider.values().random(),
    ): Device =
        Device(
            token = token,
            pushProvider = pushProvider,
        )

    fun randomUserPresenceChangedEvent(user: User = randomUser()): UserPresenceChangedEvent {
        return KFixture(fixture) {
            sameInstance(User::class.java, user)
        }()
    }
}
