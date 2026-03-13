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

package io.getstream.chat.android.ui.common.feature.messages.composer

import androidx.lifecycle.SavedStateHandle
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper.Companion.EXTRA_SOURCE_URI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ComposerSessionRepositoryTest {

    @Test
    fun `Given nothing saved When restoreSelectedAttachments Then returns empty list`() {
        val repo = ComposerSessionRepository(SavedStateHandle())

        assertTrue(repo.restoreSelectedAttachments().isEmpty())
    }

    @Test
    fun `Given picker attachment saved When restoreSelectedAttachments Then all fields are restored`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        val attachment = Attachment(
            type = randomString(),
            name = randomString(),
            fileSize = 1024,
            mimeType = randomString(),
            extraData = mapOf(EXTRA_SOURCE_URI to randomString()),
        )

        repo.save(listOf(attachment), editMode = null)
        val restored = repo.restoreSelectedAttachments().first()

        assertEquals(attachment.type, restored.type)
        assertEquals(attachment.name, restored.name)
        assertEquals(attachment.fileSize, restored.fileSize)
        assertEquals(attachment.mimeType, restored.mimeType)
        assertEquals(attachment.extraData[EXTRA_SOURCE_URI], restored.extraData[EXTRA_SOURCE_URI])
    }

    @Test
    fun `Given multiple picker attachments saved When restoreSelectedAttachments Then all are restored`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        val uri1 = randomString()
        val uri2 = randomString()
        val attachments = listOf(
            Attachment(name = randomString(), extraData = mapOf(EXTRA_SOURCE_URI to uri1)),
            Attachment(name = randomString(), extraData = mapOf(EXTRA_SOURCE_URI to uri2)),
        )

        repo.save(attachments, editMode = null)
        val result = repo.restoreSelectedAttachments()

        assertEquals(2, result.size)
        assertEquals(uri1, result[0].extraData[EXTRA_SOURCE_URI])
        assertEquals(uri2, result[1].extraData[EXTRA_SOURCE_URI])
    }

    @Test
    fun `Given picker attachment with extra extraData When saved Then EXTRA_SOURCE_URI and other keys are both present after restore`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        val uri = randomString()
        val customValue = randomString()
        val attachment = Attachment(
            name = randomString(),
            extraData = mapOf(EXTRA_SOURCE_URI to uri, "customKey" to customValue),
        )

        repo.save(listOf(attachment), editMode = null)
        val restored = repo.restoreSelectedAttachments().first()

        assertEquals(uri, restored.extraData[EXTRA_SOURCE_URI])
        assertEquals(customValue, restored.extraData["customKey"])
    }

    @Test
    fun `Given non-empty state When save called with empty Then restoreSelectedAttachments returns empty`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        repo.save(
            listOf(Attachment(extraData = mapOf(EXTRA_SOURCE_URI to randomString()))),
            editMode = null,
        )

        repo.save(emptyList(), editMode = null)

        assertTrue(repo.restoreSelectedAttachments().isEmpty())
    }

    @Test
    fun `Given nothing saved When restoreEditMode Then returns null`() {
        val repo = ComposerSessionRepository(SavedStateHandle())

        assertNull(repo.restoreEditMode())
    }

    @Test
    fun `Given only selected attachments saved When restoreEditMode Then returns null`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        repo.save(
            listOf(Attachment(extraData = mapOf(EXTRA_SOURCE_URI to randomString()))),
            editMode = null,
        )

        assertNull(repo.restoreEditMode())
    }

    @Test
    fun `Given edit mode saved When restoreEditMode Then message fields are restored correctly`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        val message = Message(
            id = randomString(),
            cid = randomCID(),
            text = randomString(),
            parentId = randomString(),
            type = randomString(),
        )

        repo.save(emptyList(), editMode = ComposerSessionRepository.EditMode(message, emptyList()))
        val result = repo.restoreEditMode()

        assertEquals(message.id, result?.message?.id)
        assertEquals(message.cid, result?.message?.cid)
        assertEquals(message.text, result?.message?.text)
        assertEquals(message.parentId, result?.message?.parentId)
        assertEquals(message.type, result?.message?.type)
    }

    @Test
    fun `Given message without parentId saved When restoreEditMode Then parentId is null`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        val message = randomMessage(parentId = null)

        repo.save(emptyList(), editMode = ComposerSessionRepository.EditMode(message, emptyList()))

        assertNull(repo.restoreEditMode()?.message?.parentId)
    }

    @Test
    fun `Given edit mode with remote attachment saved When restoreEditMode Then attachment fields are restored`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        val attachment = randomAttachment(upload = null)

        repo.save(
            emptyList(),
            editMode = ComposerSessionRepository.EditMode(
                randomMessage(),
                listOf(attachment),
            ),
        )
        val restored = repo.restoreEditMode()!!.attachments.first()

        assertEquals(attachment.type, restored.type)
        assertEquals(attachment.name, restored.name)
        assertEquals(attachment.fileSize, restored.fileSize)
        assertEquals(attachment.mimeType, restored.mimeType)
        assertEquals(attachment.assetUrl, restored.assetUrl)
        assertEquals(attachment.imageUrl, restored.imageUrl)
        assertEquals(attachment.thumbUrl, restored.thumbUrl)
        assertEquals(attachment.title, restored.title)
    }

    @Test
    fun `Given edit mode saved When save called with empty Then restoreEditMode returns null`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        repo.save(
            emptyList(),
            editMode = ComposerSessionRepository.EditMode(randomMessage(), emptyList()),
        )

        repo.save(emptyList(), editMode = null)

        assertNull(repo.restoreEditMode())
    }

    @Test
    fun `Given attachment with string extraData When saved in edit mode Then string value is preserved after restore`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        val payload = randomString()
        val attachment = randomAttachment(
            upload = null,
            extraData = mapOf("payload" to payload),
        )

        repo.save(
            emptyList(),
            editMode = ComposerSessionRepository.EditMode(randomMessage(), listOf(attachment)),
        )

        assertEquals(payload, repo.restoreEditMode()!!.attachments.first().extraData["payload"])
    }

    @Test
    fun `Given attachment with all supported primitive types in extraData When saved Then all are preserved after restore`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        val attachment = randomAttachment(
            upload = null,
            extraData = mapOf(
                "strKey" to "text",
                "boolKey" to true,
                "intKey" to 42,
                "longKey" to 9_000_000_000L,
                "doubleKey" to 3.14,
                "floatKey" to 1.5f,
            ),
        )

        repo.save(
            emptyList(),
            editMode = ComposerSessionRepository.EditMode(randomMessage(), listOf(attachment)),
        )
        val restored = repo.restoreEditMode()!!.attachments.first().extraData

        assertEquals("text", restored["strKey"])
        assertEquals(true, restored["boolKey"])
        assertEquals(42, restored["intKey"])
        assertEquals(9_000_000_000L, restored["longKey"])
        assertEquals(3.14, (restored["doubleKey"] as Number).toDouble(), 1e-10)
        assertEquals(1.5, (restored["floatKey"] as Number).toDouble(), 1e-10)
    }

    @Test
    fun `Given attachment with non-serializable value in extraData When saved Then non-serializable entry is dropped without error`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        val validValue = randomString()
        val attachment = randomAttachment(
            upload = null,
            extraData = mapOf("validKey" to validValue, "objectKey" to object {}),
        )

        repo.save(
            emptyList(),
            editMode = ComposerSessionRepository.EditMode(randomMessage(), listOf(attachment)),
        )
        val restored = repo.restoreEditMode()!!.attachments.first().extraData

        assertEquals(validValue, restored["validKey"])
        assertFalse(restored.containsKey("objectKey"))
    }

    @Test
    fun `Given both selected attachments and edit mode saved When restored Then both are returned independently`() {
        val repo = ComposerSessionRepository(SavedStateHandle())
        val pickerUri = randomString()
        val pickerAttachment = Attachment(extraData = mapOf(EXTRA_SOURCE_URI to pickerUri))
        val editAttachment = randomAttachment(upload = null)
        val editMessage = randomMessage()

        repo.save(
            listOf(pickerAttachment),
            editMode = ComposerSessionRepository.EditMode(editMessage, listOf(editAttachment)),
        )

        val selectedAttachments = repo.restoreSelectedAttachments()
        val editMode = repo.restoreEditMode()

        assertEquals(1, selectedAttachments.size)
        assertEquals(pickerUri, selectedAttachments.first().extraData[EXTRA_SOURCE_URI])
        assertEquals(editMessage.id, editMode?.message?.id)
        assertEquals(1, editMode?.attachments?.size)
        assertEquals(editAttachment.assetUrl, editMode?.attachments?.first()?.assetUrl)
    }
}
