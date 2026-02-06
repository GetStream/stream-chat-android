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

package io.getstream.chat.android.client.utils.internal

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class MessageUtilsTest {

    @Test
    fun testCommandMessage() {
        val message = Message(text = "/command")
        val updatedMessageType = getMessageType(message)
        updatedMessageType `should be equal to` MessageType.EPHEMERAL
    }

    @Test
    fun testMessageWithAttachmentsInUploadStateIdle() {
        val message = Message(
            text = "Message with attachments",
            type = MessageType.REGULAR,
            attachments = listOf(Attachment(uploadState = Attachment.UploadState.Idle)),
        )
        val updatedMessageType = getMessageType(message)
        updatedMessageType `should be equal to` MessageType.EPHEMERAL
    }

    @Test
    fun testMessageWithAttachmentsInUploadStateInProgress() {
        val message = Message(
            text = "Message with attachments",
            type = MessageType.REGULAR,
            attachments = listOf(Attachment(uploadState = Attachment.UploadState.InProgress(1, 1))),
        )
        val updatedMessageType = getMessageType(message)
        updatedMessageType `should be equal to` MessageType.EPHEMERAL
    }

    @Test
    fun testMessageWithAttachmentsInUploadStateSuccess() {
        val message = Message(
            text = "Message with attachments",
            type = MessageType.REGULAR,
            attachments = listOf(Attachment(uploadState = Attachment.UploadState.Success)),
        )
        val updatedMessageType = getMessageType(message)
        updatedMessageType `should be equal to` MessageType.REGULAR
    }

    @Test
    fun testSystemMessage() {
        val message = Message(text = "System message", type = MessageType.SYSTEM)
        val updatedMessageType = getMessageType(message)
        updatedMessageType `should be equal to` MessageType.SYSTEM
    }

    @Test
    fun testRegularMessage() {
        val message = Message(text = "Regular message")
        val updatedMessageType = getMessageType(message)
        updatedMessageType `should be equal to` MessageType.REGULAR
    }

    @Test
    fun testErrorMessage() {
        val message = Message(text = "Error message", type = MessageType.ERROR)
        val updatedMessageType = getMessageType(message)
        updatedMessageType `should be equal to` MessageType.REGULAR
    }
}
