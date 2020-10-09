package io.getstream.chat.android.livedata.controller.helper

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.helpers.AttachmentHelper
import io.getstream.chat.android.livedata.randomAttachment
import io.getstream.chat.android.livedata.randomMessage
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

internal class MessageHelperTests {

    private lateinit var sut: MessageHelper
    private lateinit var attachmentHelper: AttachmentHelper

    @BeforeEach
    fun setup() {
        attachmentHelper = mock()
        sut = MessageHelper(attachmentHelper)
    }

    @Test
    fun `Should return list with the same size as new messages with same messages`() {
        val message1 = randomMessage()
        val message2 = randomMessage()

        val result = sut.updateValidAttachmentsUrl(listOf(message1, message2), emptyMap())

        result.size shouldBeEqualTo 2
        result.any { it === message1 } shouldBeEqualTo true
        result.any { it === message2 } shouldBeEqualTo true
    }

    @Test
    fun `If old messages contains the same id and old url is still valid Should return list with new message with old url`() {
        val oldValidUrl = "oldValidUrl"
        val oldAttachment = randomAttachment { url = oldValidUrl }
        val oldMessage = randomMessage(attachments = mutableListOf(oldAttachment), updatedAt = Date(1000L))
        val newMessage = oldMessage.copy(
            attachments = mutableListOf(oldAttachment.copy(url = "SomeNewUrl")),
            updatedAt = Date(2000L)
        )
        When calling attachmentHelper.hasValidUrl(oldAttachment) doReturn true

        val result = sut.updateValidAttachmentsUrl(listOf(newMessage), mapOf(oldMessage.id to oldMessage))

        result.size shouldBeEqualTo 1
        result.any { message ->
            message.cid == message.cid && message.updatedAt!!.time == 2000L && message.attachments.first().url == "oldValidUrl"
        } shouldBeEqualTo true
    }
}