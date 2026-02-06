/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.internal.state.channel

import io.getstream.chat.android.client.helpers.AttachmentHelper
import io.getstream.chat.android.client.internal.state.message.attachments.internal.AttachmentUrlValidator
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomMessage
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class AttachmentUrlValidatorTests {

    private lateinit var sut: AttachmentUrlValidator
    private lateinit var attachmentHelper: AttachmentHelper

    @BeforeEach
    fun setup() {
        attachmentHelper = mock {
            on { it.hasStreamImageUrl(any()) } doReturn true
        }
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
    fun `Given old messages contains the same id and old url is null Should return list with new message`() {
        val oldAttachment = randomAttachment().copy(imageUrl = null)
        val newAttachment = oldAttachment.copy(imageUrl = "imageUrl")
        val oldMessage = randomMessage(attachments = mutableListOf(oldAttachment), updatedAt = Date(1000L))
        val newMessage = oldMessage.copy(
            attachments = mutableListOf(newAttachment),
            updatedAt = Date(2000L),
        )

        val result = sut.updateValidAttachmentsUrl(listOf(newMessage), mapOf(oldMessage.id to oldMessage))

        result.size shouldBeEqualTo 1
        result.any { message ->
            message.updatedAt == Date(2000L) &&
                message.attachments.first() == newAttachment
        } shouldBeEqualTo true
    }

    @Test
    fun `Given old messages contains the same id and old url is equal to new url Should return list with new message`() {
        val oldAttachment = randomAttachment().copy(imageUrl = "imageUrl")
        val newAttachment = oldAttachment.copy(name = "otherName")
        val oldMessage = randomMessage(attachments = mutableListOf(oldAttachment), updatedAt = Date(1000L))
        val newMessage = oldMessage.copy(
            attachments = mutableListOf(newAttachment),
            updatedAt = Date(2000L),
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
        val oldAttachment = randomAttachment().copy(imageUrl = oldNotValidUrl)
        val newAttachment = oldAttachment.copy(imageUrl = "SomeNewUrl")
        val oldMessage = randomMessage(attachments = mutableListOf(oldAttachment), updatedAt = Date(1000L))
        val newMessage = oldMessage.copy(
            attachments = mutableListOf(newAttachment),
            updatedAt = Date(2000L),
        )
        whenever(attachmentHelper.hasValidImageUrl(oldAttachment)) doReturn false

        val result = sut.updateValidAttachmentsUrl(listOf(newMessage), mapOf(oldMessage.id to oldMessage))

        result.size shouldBeEqualTo 1
        result.any { message -> message.attachments.first() === newAttachment } shouldBeEqualTo true
    }

    @Test
    fun `If old messages contains the same id and old url is still valid Should return list with new message with old url`() {
        val oldValidUrl = "oldValidUrl"
        val oldAttachment = randomAttachment().copy(imageUrl = oldValidUrl)
        val oldMessage = randomMessage(attachments = mutableListOf(oldAttachment), updatedAt = Date(1000L))
        val newMessage = oldMessage.copy(
            attachments = listOf(oldAttachment.copy(imageUrl = "SomeNewUrl")),
            updatedAt = Date(2000L),
        )
        whenever(attachmentHelper.hasValidImageUrl(oldAttachment)) doReturn true

        println("test oldAttachment: $oldAttachment")
        println("test newAttachment: ${newMessage.attachments.first()}")

        val result = sut.updateValidAttachmentsUrl(listOf(newMessage), mapOf(oldMessage.id to oldMessage))

        result.size shouldBeEqualTo 1
        result.first().let {
            it.updatedAt shouldBeEqualTo Date(2000L)
            it.attachments.first().imageUrl shouldBeEqualTo oldValidUrl
        }
    }

    @Test
    fun `Given attachments with only imageUrls and they are valid Should not update different attachments`() {
        val url1 = "url1"
        val url2 = "url2"
        val attachment1 = Attachment(imageUrl = url1)
        val attachment2 = Attachment(imageUrl = url2)
        whenever(attachmentHelper.hasValidImageUrl(any())) doReturn true
        val message = randomMessage(attachments = mutableListOf(attachment1, attachment2))

        val result = sut.updateValidAttachmentsUrl(listOf(message.copy(updatedAt = Date())), mapOf(message.id to message))

        result.first().attachments.let { attachments ->
            attachments.any { it.imageUrl == url1 }.shouldBeTrue()
            attachments.any { it.imageUrl == url2 }.shouldBeTrue()
        }
    }

    @Test
    fun `Given attachments with not stream imageUrls and valid old urls Should Not return attachment with old url`() {
        val oldAttachment = randomAttachment(imageUrl = "oldUrl")
        val newAttachment = oldAttachment.copy(imageUrl = "newUrl")
        val oldMessage = randomMessage(attachments = mutableListOf(oldAttachment))
        val newMessage = oldMessage.copy(attachments = mutableListOf(newAttachment))
        whenever(attachmentHelper.hasStreamImageUrl(oldAttachment)) doReturn false
        whenever(attachmentHelper.hasValidImageUrl(any())) doReturn true

        val result = sut.updateValidAttachmentsUrl(listOf(newMessage), mapOf(oldMessage.id to oldMessage))

        result.first().attachments.first() shouldBeEqualTo newAttachment
    }
}
