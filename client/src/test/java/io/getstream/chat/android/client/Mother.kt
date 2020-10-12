package io.getstream.chat.android.client

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import io.getstream.chat.android.client.models.Attachment

internal object Mother {
    private val fixture = JFixture()

    fun randomAttachment(attachmentBuilder: Attachment.() -> Unit): Attachment {
        return KFixture(fixture) {
            sameInstance(
                Attachment.UploadState::class.java,
                Attachment.UploadState.Success
            )
        } <Attachment>().apply(attachmentBuilder)
    }
}
