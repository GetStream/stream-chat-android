package io.getstream.chat.android.offline.channel

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.helpers.AttachmentHelper
import io.getstream.chat.android.offline.message.attachment.AttachmentUrlValidator
import io.getstream.chat.android.offline.randomAttachment
import io.getstream.chat.android.offline.randomMessage
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

internal class AttachmentUrlValidatorTests {

    private lateinit var sut: AttachmentUrlValidator
    private lateinit var attachmentHelper: AttachmentHelper

    @BeforeEach
    fun setup() {
        attachmentHelper = mock()
        sut = AttachmentUrlValidator(attachmentHelper)
    }

    @Test
    fun `Should return list with same messages`() {
        val message1 = randomMessage()
        val message2 = randomMessage()

        val result = sut.updateValidAttachmentsUrl(listOf(message1, message2), emptyMap())

        result shouldBeEqualTo listOf(message1, message2)
    }

    @Test
    fun `If old messages contains the same id and old url is null to new url Should return list with new message`() {
        val oldAttachment = randomAttachment { imageUrl = null }
        val newAttachment = oldAttachment.copy(imageUrl = "imageUrl")
        val oldMessage = randomMessage(attachments = mutableListOf(oldAttachment), updatedAt = Date(1000L))
        val newMessage = oldMessage.copy(
            attachments = mutableListOf(newAttachment),
            updatedAt = Date(2000L)
        )

        val result = sut.updateValidAttachmentsUrl(listOf(newMessage), mapOf(oldMessage.id to oldMessage))

        result.size shouldBeEqualTo 1
        result.any { message ->
            message.updatedAt == Date(2000L) &&
                message.attachments.first() == newAttachment
        } shouldBeEqualTo true
    }

    @Test
    fun `If old messages contains the same id and old url is equal to new url Should return list with new message`() {
        val oldAttachment = randomAttachment { imageUrl = "imageUrl" }
        val newAttachment = oldAttachment.copy(name = "otherName")
        val oldMessage = randomMessage(attachments = mutableListOf(oldAttachment), updatedAt = Date(1000L))
        val newMessage = oldMessage.copy(
            attachments = mutableListOf(newAttachment),
            updatedAt = Date(2000L)
        )

        val result = sut.updateValidAttachmentsUrl(listOf(newMessage), mapOf(oldMessage.id to oldMessage))

        result.size shouldBeEqualTo 1
        result.any { message ->
            message.updatedAt == Date(2000L) &&
                message.attachments.first() == newAttachment
        } shouldBeEqualTo true
    }

    @Test
    fun `If old messages contains the same id and old url is not valid Should return list with new attachment`() {
        val oldNotValidUrl = "oldNotValidUrl"
        val oldAttachment = randomAttachment { imageUrl = oldNotValidUrl }
        val newAttachment = oldAttachment.copy(imageUrl = "SomeNewUrl")
        val oldMessage = randomMessage(attachments = mutableListOf(oldAttachment), updatedAt = Date(1000L))
        val newMessage = oldMessage.copy(
            attachments = mutableListOf(newAttachment),
            updatedAt = Date(2000L)
        )
        whenever(attachmentHelper.hasValidImageUrl(oldAttachment)) doReturn false

        val result = sut.updateValidAttachmentsUrl(listOf(newMessage), mapOf(oldMessage.id to oldMessage))

        result.size shouldBeEqualTo 1
        result.any { message -> message.attachments.first() === newAttachment } shouldBeEqualTo true
    }

    @Test
    fun `If old messages contains the same id and old url is still valid Should return list with new message with old url`() {
        val oldValidUrl = "oldValidUrl"
        val oldAttachment = randomAttachment { imageUrl = oldValidUrl }
        val oldMessage = randomMessage(attachments = mutableListOf(oldAttachment), updatedAt = Date(1000L))
        val newMessage = oldMessage.copy(
            attachments = mutableListOf(oldAttachment.copy(imageUrl = "SomeNewUrl")),
            updatedAt = Date(2000L)
        )
        whenever(attachmentHelper.hasValidImageUrl(oldAttachment)) doReturn true

        val result = sut.updateValidAttachmentsUrl(listOf(newMessage), mapOf(oldMessage.id to oldMessage))

        result.size shouldBeEqualTo 1
        result.any { message ->
            message.cid == message.cid && message.updatedAt!!.time == 2000L &&
                message.attachments.first().imageUrl == "oldValidUrl"
        } shouldBeEqualTo true
    }
}
