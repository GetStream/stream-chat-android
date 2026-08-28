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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultQuotedAttachmentMessageFactoryTest {

    private val factory = DefaultQuotedAttachmentMessageFactory()

    @ParameterizedTest
    @MethodSource("attachmentTypes")
    fun `factory handles the quoted attachment types it renders`(type: String, expected: Boolean) {
        val message = Message(attachments = listOf(Attachment(type = type)))

        assertEquals(expected, factory.canHandle(message))
    }

    @Test
    fun `factory does not handle a message without attachments`() {
        assertFalse(factory.canHandle(Message()))
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun attachmentTypes(): List<Arguments> = listOf(
            Arguments.of(AttachmentType.AUDIO, true),
            Arguments.of(AttachmentType.AUDIO_RECORDING, true),
            Arguments.of(AttachmentType.FILE, true),
            Arguments.of(AttachmentType.IMAGE, true),
            Arguments.of(AttachmentType.VIDEO, true),
            Arguments.of(AttachmentType.GIPHY, true),
            Arguments.of(AttachmentType.UNKNOWN, false),
        )
    }
}
