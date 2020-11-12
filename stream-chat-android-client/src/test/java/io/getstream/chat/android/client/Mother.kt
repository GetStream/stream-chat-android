package io.getstream.chat.android.client

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User

internal object Mother {
    private val fixture = JFixture()
    private val charPool: CharArray = (('a'..'z') + ('A'..'Z') + ('0'..'9')).toCharArray()

    fun randomAttachment(attachmentBuilder: Attachment.() -> Unit = { }): Attachment {
        return KFixture(fixture) {
            sameInstance(
                Attachment.UploadState::class.java,
                Attachment.UploadState.Success
            )
        } <Attachment>().apply(attachmentBuilder)
    }

    fun randomUser(userBuilder: User.() -> Unit = { }): User {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
        } <User>().apply(userBuilder)
    }

    internal fun randomString(size: Int = 20): String = buildString(capacity = size) {
        repeat(size) {
            append(charPool.random())
        }
    }
}
