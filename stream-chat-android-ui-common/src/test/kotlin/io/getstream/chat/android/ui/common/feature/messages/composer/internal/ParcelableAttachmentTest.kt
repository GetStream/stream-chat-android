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

package io.getstream.chat.android.ui.common.feature.messages.composer.internal

import io.getstream.chat.android.models.Attachment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

internal class ParcelableAttachmentTest {

    @Test
    fun `round-trip preserves all fields`() {
        val original = Attachment(
            upload = File("/tmp/test.jpg"),
            type = "image",
            name = "test.jpg",
            fileSize = 12345,
            mimeType = "image/jpeg",
            title = "Test Image",
        )

        val restored = original.toParcelable().toAttachment()

        assertEquals("/tmp/test.jpg", restored.upload?.absolutePath)
        assertEquals("image", restored.type)
        assertEquals("test.jpg", restored.name)
        assertEquals(12345, restored.fileSize)
        assertEquals("image/jpeg", restored.mimeType)
        assertEquals("Test Image", restored.title)
    }

    @Test
    fun `round-trip with null upload`() {
        val original = Attachment(
            type = "file",
            name = "doc.pdf",
            fileSize = 100,
        )

        val restored = original.toParcelable().toAttachment()

        assertNull(restored.upload)
        assertEquals("file", restored.type)
        assertEquals("doc.pdf", restored.name)
    }

    @Test
    fun `round-trip preserves extraData with primitives`() {
        val original = Attachment(
            type = "voicemail",
            extraData = mapOf(
                "duration" to 5.2f,
                "label" to "recording",
                "count" to 42,
                "bigNumber" to 123456789L,
                "ratio" to 3.14,
                "active" to true,
            ),
        )

        val restored = original.toParcelable().toAttachment()

        assertEquals(5.2f, restored.extraData["duration"])
        assertEquals("recording", restored.extraData["label"])
        assertEquals(42, restored.extraData["count"])
        assertEquals(123456789L, restored.extraData["bigNumber"])
        assertEquals(3.14, restored.extraData["ratio"])
        assertEquals(true, restored.extraData["active"])
    }

    @Test
    fun `round-trip preserves extraData with nested list`() {
        val waveform = listOf(0.1f, 0.5f, 0.9f, 0.3f)
        val original = Attachment(
            type = "voicemail",
            extraData = mapOf("waveform_data" to waveform),
        )

        val restored = original.toParcelable().toAttachment()

        assertEquals(waveform, restored.extraData["waveform_data"])
    }

    @Test
    fun `fields not in ParcelableAttachment are null or default on restore`() {
        val original = Attachment(
            upload = File("/tmp/test.jpg"),
            type = "image",
            name = "test.jpg",
            imageUrl = "https://example.com/img.jpg",
            thumbUrl = "https://example.com/thumb.jpg",
            assetUrl = "https://example.com/asset.jpg",
            authorName = "Author",
            originalWidth = 1920,
            originalHeight = 1080,
            uploadState = Attachment.UploadState.InProgress(100, 1000),
        )

        val restored = original.toParcelable().toAttachment()

        assertNull(restored.imageUrl)
        assertNull(restored.thumbUrl)
        assertNull(restored.assetUrl)
        assertNull(restored.authorName)
        assertNull(restored.originalWidth)
        assertNull(restored.originalHeight)
        assertNull(restored.uploadState)
    }

    // region areExtraDataParcelSafe

    @Test
    fun `empty attachments list is parcel safe`() {
        assertTrue(emptyList<Attachment>().areExtraDataParcelSafe())
    }

    @Test
    fun `attachments with empty extraData are parcel safe`() {
        val attachments = listOf(
            Attachment(type = "image"),
            Attachment(type = "file"),
        )
        assertTrue(attachments.areExtraDataParcelSafe())
    }

    @Test
    fun `attachments with primitive extraData are parcel safe`() {
        val attachments = listOf(
            Attachment(
                extraData = mapOf(
                    "string" to "value",
                    "int" to 42,
                    "long" to 123L,
                    "float" to 1.5f,
                    "double" to 3.14,
                    "bool" to true,
                ),
            ),
        )
        assertTrue(attachments.areExtraDataParcelSafe())
    }

    @Test
    fun `attachments with nested list of primitives are parcel safe`() {
        val attachments = listOf(
            Attachment(extraData = mapOf("data" to listOf(1.0f, 2.0f, 3.0f))),
        )
        assertTrue(attachments.areExtraDataParcelSafe())
    }

    @Test
    fun `attachments with nested map of primitives are parcel safe`() {
        val attachments = listOf(
            Attachment(extraData = mapOf("nested" to mapOf("key" to "value"))),
        )
        assertTrue(attachments.areExtraDataParcelSafe())
    }

    @Test
    fun `attachments with non-parcelable extraData are NOT parcel safe`() {
        val attachments = listOf(
            Attachment(extraData = mapOf("custom" to object {})),
        )
        assertFalse(attachments.areExtraDataParcelSafe())
    }

    @Test
    fun `single unsafe attachment makes entire list unsafe`() {
        val attachments = listOf(
            Attachment(extraData = mapOf("safe" to "value")),
            Attachment(extraData = mapOf("unsafe" to object {})),
        )
        assertFalse(attachments.areExtraDataParcelSafe())
    }

    // endregion
}
