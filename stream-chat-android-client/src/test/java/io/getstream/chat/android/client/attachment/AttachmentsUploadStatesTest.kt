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

package io.getstream.chat.android.client.attachment

import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class AttachmentsUploadStatesTest {

    @Test
    fun `updateMessageAttachments should update the attachments state`() = runTest {
        val message = randomMessage(attachments = listOf(randomAttachment(type = AttachmentType.IMAGE)))
        AttachmentsUploadStates.updateMessageAttachments(message)
        val attachments = AttachmentsUploadStates.observeAttachments(message.id).first()
        attachments shouldBeEqualTo message.attachments
    }

    @Test
    fun `observeAttachments should return empty list for unknown message id`() = runTest {
        val attachments = AttachmentsUploadStates.observeAttachments("unknown_id").first()
        attachments shouldBeEqualTo emptyList()
    }

    @Test
    fun `removeMessageAttachmentsState should remove the state for the given message id`() = runTest {
        val message = randomMessage(attachments = listOf(randomAttachment(type = AttachmentType.IMAGE)))
        AttachmentsUploadStates.updateMessageAttachments(message)
        AttachmentsUploadStates.removeMessageAttachmentsState(message.id)
        val attachments = AttachmentsUploadStates.observeAttachments(message.id).first()
        attachments shouldBeEqualTo emptyList()
    }

    @Test
    fun `clearStates should clear all states`() = runTest {
        val message1 = randomMessage(attachments = listOf(randomAttachment(type = AttachmentType.IMGUR)))
        val message2 = randomMessage(attachments = listOf(randomAttachment(type = AttachmentType.VIDEO)))
        AttachmentsUploadStates.updateMessageAttachments(message1)
        AttachmentsUploadStates.updateMessageAttachments(message2)
        AttachmentsUploadStates.clearStates()
        val attachments1 = AttachmentsUploadStates.observeAttachments(message1.id).first()
        val attachments2 = AttachmentsUploadStates.observeAttachments(message2.id).first()
        attachments1 shouldBeEqualTo emptyList()
        attachments2 shouldBeEqualTo emptyList()
    }
}
