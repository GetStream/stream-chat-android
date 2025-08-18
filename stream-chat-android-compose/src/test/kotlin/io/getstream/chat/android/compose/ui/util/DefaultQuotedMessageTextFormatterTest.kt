/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.util

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomUser
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class DefaultQuotedMessageTextFormatterTest {

    @Test
    fun `shows translated text when auto-translation enabled and translation exists`() {
        val user = randomUser(language = "pt")
        val message = Message(
            text = "Hello",
            user = user,
            i18n = mapOf("pt_text" to "Olá"),
        )
        val sut = Fixture()
            .givenAutoTranslationEnabled(enabled = true)
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = user)

        assertEquals("Olá", result.text)
    }

    @Test
    fun `shows message text when auto-translation enabled but current user is null`() {
        val message = Message(
            text = "Hello",
            i18n = mapOf("pt_text" to "Olá"),
        )
        val sut = Fixture()
            .givenAutoTranslationEnabled(enabled = true)
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = null)

        assertEquals("Hello", result.text)
    }

    @Test
    fun `shows message text when auto-translation enabled but no translation exists`() {
        val user = randomUser(language = "fr")
        val message = Message(
            text = "Hello",
            user = user,
            i18n = mapOf("pt_text" to "Olá"),
        )
        val sut = Fixture()
            .givenAutoTranslationEnabled(enabled = true)
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = user)

        assertEquals("Hello", result.text)
    }

    @Test
    fun `shows message text when auto-translation enabled but user language is empty`() {
        val user = randomUser(language = "")
        val message = Message(
            text = "Hello",
            user = user,
            i18n = mapOf("pt_text" to "Olá"),
        )
        val sut = Fixture()
            .givenAutoTranslationEnabled(enabled = true)
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = user)

        assertEquals("Hello", result.text)
    }

    @Test
    fun `shows message text when auto-translation disabled`() {
        val user = randomUser(language = "pt")
        val message = Message(
            text = "Hello",
            user = user,
            i18n = mapOf("pt_text" to "Olá"),
        )
        val sut = Fixture()
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = user)

        assertEquals("Hello", result.text)
    }

    @Test
    fun `shows deleted message placeholder when message is deleted`() {
        val message = Message(
            text = "Hello",
            deletedAt = Date(),
        )
        val sut = Fixture()
            .givenStringResource(R.string.stream_ui_message_list_message_deleted, "Message deleted")
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = null)

        assertEquals("Message deleted", result.text)
    }

    @Test
    fun `shows poll name when poll exists`() {
        val poll = randomPoll(name = "My Poll")
        val message = Message(poll = poll)
        val sut = Fixture()
            .givenStringResource(R.string.stream_compose_quoted_message_poll, "Poll: My Poll", poll.name)
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = null)

        assertEquals("Poll: My Poll", result.text)
    }

    @Test
    fun `shows static location text when shared location exists with no end date`() {
        val sharedLocation = randomLocation(endAt = null)
        val message = Message(sharedLocation = sharedLocation)
        val sut = Fixture()
            .givenStringResource(R.string.stream_ui_location_static_message_text, "Static Location")
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = null)

        assertEquals("Static Location", result.text)
    }

    @Test
    fun `shows live location text when shared location exists with end date`() {
        val sharedLocation = randomLocation(endAt = randomDate())
        val message = Message(sharedLocation = sharedLocation)
        val sut = Fixture()
            .givenStringResource(R.string.stream_ui_location_live_message_text, "Live Location")
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = null)

        assertEquals("Live Location", result.text)
    }

    @Test
    fun `shows attachment name`() {
        val attachment = randomAttachment(name = "AttachmentName")
        val message = Message(attachments = listOf(attachment))
        val sut = Fixture()
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = null)

        assertEquals("AttachmentName", result.text)
    }

    @Test
    fun `shows attachment text`() {
        val attachment = randomAttachment(name = null, text = "AttachmentText")
        val message = Message(attachments = listOf(attachment))
        val sut = Fixture()
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = null)

        assertEquals("AttachmentText", result.text)
    }

    @Test
    fun `shows attachment title`() {
        val attachment = randomAttachment(name = null, text = null, title = "AttachmentTitle")
        val message = Message(attachments = listOf(attachment))
        val sut = Fixture()
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = null)

        assertEquals("AttachmentTitle", result.text)
    }

    @Test
    fun `shows attachment image tag`() {
        val attachment = randomAttachment(name = null, text = null, title = null, type = AttachmentType.IMAGE)
        val message = Message(attachments = listOf(attachment))
        val sut = Fixture()
            .givenStringResource(R.string.stream_compose_quoted_message_image_tag, "Image Attachment")
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = null)

        assertEquals("Image Attachment", result.text)
    }

    @Test
    fun `shows attachment giphy tag`() {
        val attachment = randomAttachment(name = null, text = null, title = null, type = AttachmentType.GIPHY)
        val message = Message(attachments = listOf(attachment))
        val sut = Fixture()
            .givenStringResource(R.string.stream_compose_quoted_message_giphy_tag, "Giphy Attachment")
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = null)

        assertEquals("Giphy Attachment", result.text)
    }

    @Test
    fun `shows attachment file tag`() {
        val attachment = randomAttachment(name = null, text = null, title = null, type = AttachmentType.FILE)
        val message = Message(attachments = listOf(attachment))
        val sut = Fixture()
            .givenStringResource(R.string.stream_compose_quoted_message_file_tag, "File Attachment")
            .get()

        val result = sut.format(message = message, replyMessage = null, currentUser = null)

        assertEquals("File Attachment", result.text)
    }

    private class Fixture {

        private val context: Context = mock()
        private var autoTranslationEnabled: Boolean = false
        private val typography = StreamTypography.defaultTypography()
        private val textStyle = { _: Boolean -> TextStyle.Default }
        private val linkStyle = { _: Boolean -> TextStyle.Default }
        private val mentionColor = { _: Boolean -> Color.Black }

        fun givenAutoTranslationEnabled(enabled: Boolean) = apply {
            autoTranslationEnabled = enabled
        }

        fun givenStringResource(resId: Int, value: String, vararg formatArgs: Any) = apply {
            if (formatArgs.isEmpty()) {
                whenever(context.getString(resId)) doReturn value
            } else {
                whenever(context.getString(resId, formatArgs)) doReturn value
            }
        }

        fun get() = DefaultQuotedMessageTextFormatter(
            context = context,
            autoTranslationEnabled = autoTranslationEnabled,
            typography = typography,
            textStyle = textStyle,
            linkStyle = linkStyle,
            mentionColor = mentionColor,
            builder = null,
        )
    }
}
