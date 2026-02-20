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

package io.getstream.chat.android.ui.common.helper.internal

import android.content.Context
import android.net.Uri
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper.Companion.EXTRA_SOURCE_URI
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File

internal class AttachmentStorageHelperTest {

    private val context: Context = mock()
    private val storageHelper: StorageHelper = mock()
    private val attachmentFilter: AttachmentFilter = mock {
        on { filterAttachments(any()) } doAnswer { it.getArgument(0) }
    }
    private val sut = AttachmentStorageHelper(context, storageHelper, attachmentFilter)

    @Test
    fun `getFileMetadata returns filtered file metadata`() {
        val rawMetadata = listOf(
            AttachmentMetaData(type = "file", mimeType = "application/pdf", title = "doc.pdf"),
        )
        whenever(storageHelper.getFileAttachments(context)) doReturn rawMetadata

        val result = sut.getFileMetadata()

        assertEquals(rawMetadata, result)
    }

    @Test
    fun `getFileMetadata returns empty list when no files found`() {
        whenever(storageHelper.getFileAttachments(context)) doReturn emptyList()

        val result = sut.getFileMetadata()

        assertEquals(emptyList<AttachmentMetaData>(), result)
    }

    @Test
    fun `getMediaMetadata returns filtered media metadata`() {
        val rawMetadata = listOf(
            AttachmentMetaData(type = "image", mimeType = "image/jpeg", title = "photo.jpg"),
        )
        whenever(storageHelper.getMediaAttachments(context)) doReturn rawMetadata

        val result = sut.getMediaMetadata()

        assertEquals(rawMetadata, result)
    }

    @Test
    fun `toAttachments populates all attachment fields from metadata`() {
        val uri = mock<Uri> { on { toString() } doReturn "content://media/external/images/1" }
        val meta = AttachmentMetaData(
            uri = uri,
            type = "image",
            mimeType = "image/jpeg",
            title = "photo.jpg",
        ).apply { size = 1024L }

        val result = sut.toAttachments(listOf(meta))

        assertEquals(1, result.size)
        val attachment = result.first()
        assertEquals("image", attachment.type)
        assertEquals("photo.jpg", attachment.name)
        assertEquals(1024, attachment.fileSize)
        assertEquals("image/jpeg", attachment.mimeType)
        assertNull(attachment.upload)
    }

    @Test
    fun `toAttachments stores source URI in extraData`() {
        val uriString = "content://media/external/images/1"
        val uri = mock<Uri> { on { toString() } doReturn uriString }
        val meta = AttachmentMetaData(uri = uri, type = "image", mimeType = "image/png", title = "img.png")

        val result = sut.toAttachments(listOf(meta))

        assertEquals(uriString, result.first().extraData[EXTRA_SOURCE_URI])
    }

    @Test
    fun `toAttachments preserves existing extraData alongside source URI`() {
        val uriString = "content://media/external/images/1"
        val uri = mock<Uri> { on { toString() } doReturn uriString }
        val meta = AttachmentMetaData(
            uri = uri,
            type = "image",
            mimeType = "image/png",
            title = "img.png",
            extraData = mapOf("custom_key" to "custom_value"),
        )

        val result = sut.toAttachments(listOf(meta))

        val extra = result.first().extraData
        assertEquals("custom_value", extra["custom_key"])
        assertEquals(uriString, extra[EXTRA_SOURCE_URI])
    }

    @Test
    fun `toAttachments without URI does not add source URI key`() {
        val meta = AttachmentMetaData(type = "file", mimeType = "application/pdf", title = "doc.pdf")

        val result = sut.toAttachments(listOf(meta))

        assertEquals(emptyMap<String, Any>(), result.first().extraData)
    }

    @Test
    fun `toAttachments uses empty name when title is null`() {
        val meta = AttachmentMetaData(type = "file", mimeType = "application/pdf", title = null)

        val result = sut.toAttachments(listOf(meta))

        assertEquals("", result.first().name)
    }

    @Test
    fun `toAttachments returns empty list for empty input`() {
        val result = sut.toAttachments(emptyList())

        assertEquals(emptyList<Attachment>(), result)
    }

    @Test
    fun `toAttachments converts multiple items preserving order`() {
        val meta1 = AttachmentMetaData(type = "image", mimeType = "image/jpeg", title = "a.jpg")
        val meta2 = AttachmentMetaData(type = "file", mimeType = "application/pdf", title = "b.pdf")

        val result = sut.toAttachments(listOf(meta1, meta2))

        assertEquals(2, result.size)
        assertEquals("a.jpg", result[0].name)
        assertEquals("b.pdf", result[1].name)
    }

    @Test
    fun `resolveAttachmentFiles skips attachment with existing upload`() {
        val existingFile = mock<File>()
        val attachment = Attachment(upload = existingFile, type = "image")

        val result = sut.resolveAttachmentFiles(listOf(attachment))

        assertSame(existingFile, result.first().upload)
    }

    @Test
    fun `resolveAttachmentFiles skips attachment without source URI`() {
        val attachment = Attachment(type = "image", extraData = emptyMap())

        val result = sut.resolveAttachmentFiles(listOf(attachment))

        assertNull(result.first().upload)
    }

    @Test
    fun `resolveAttachmentFiles resolves file from source URI`() {
        val cachedFile = mock<File>()
        val sourceUri = "content://media/external/images/42"
        val parsedUri = mock<Uri>()
        val attachment = Attachment(
            type = "image",
            mimeType = "image/jpeg",
            name = "photo.jpg",
            fileSize = 2048,
            extraData = mapOf(EXTRA_SOURCE_URI to sourceUri),
        )
        whenever(storageHelper.getCachedFileFromUri(eq(context), any())) doReturn cachedFile
        Mockito.mockStatic(Uri::class.java).use { mockedUri ->
            mockedUri.`when`<Uri> { Uri.parse(sourceUri) }.thenReturn(parsedUri)

            val result = sut.resolveAttachmentFiles(listOf(attachment))

            assertEquals(cachedFile, result.first().upload)
        }
    }

    @Test
    fun `resolveAttachmentFiles reconstructs metadata from attachment fields`() {
        val sourceUri = "content://media/external/images/42"
        val parsedUri = mock<Uri>()
        val attachment = Attachment(
            type = "image",
            mimeType = "image/jpeg",
            name = "photo.jpg",
            fileSize = 2048,
            extraData = mapOf(EXTRA_SOURCE_URI to sourceUri),
        )
        var capturedMeta: AttachmentMetaData? = null
        whenever(storageHelper.getCachedFileFromUri(eq(context), any())) doAnswer { invocation ->
            capturedMeta = invocation.getArgument(1)
            mock<File>()
        }
        Mockito.mockStatic(Uri::class.java).use { mockedUri ->
            mockedUri.`when`<Uri> { Uri.parse(sourceUri) }.thenReturn(parsedUri)

            sut.resolveAttachmentFiles(listOf(attachment))
        }

        assertEquals(parsedUri, capturedMeta?.uri)
        assertEquals("image", capturedMeta?.type)
        assertEquals("image/jpeg", capturedMeta?.mimeType)
        assertEquals("photo.jpg", capturedMeta?.title)
        assertEquals(2048L, capturedMeta?.size)
    }

    @Test
    fun `resolveAttachmentFiles drops attachment when file resolution fails`() {
        val sourceUri = "content://media/external/images/42"
        val parsedUri = mock<Uri>()
        val attachment = Attachment(
            type = "image",
            extraData = mapOf(EXTRA_SOURCE_URI to sourceUri),
        )
        whenever(storageHelper.getCachedFileFromUri(eq(context), any())) doReturn null
        Mockito.mockStatic(Uri::class.java).use { mockedUri ->
            mockedUri.`when`<Uri> { Uri.parse(sourceUri) }.thenReturn(parsedUri)

            val result = sut.resolveAttachmentFiles(listOf(attachment))

            assertEquals(emptyList<Attachment>(), result)
        }
    }

    @Test
    fun `resolveAttachmentFiles processes resolved, deferred, and missing-URI attachments independently`() {
        val existingFile = mock<File>()
        val cachedFile = mock<File>()
        val parsedUri = mock<Uri>()
        val sourceUri = "content://media/external/images/1"
        val alreadyResolved = Attachment(upload = existingFile, type = "file")
        val noUri = Attachment(type = "file", extraData = emptyMap())
        val deferred = Attachment(
            type = "image",
            extraData = mapOf(EXTRA_SOURCE_URI to sourceUri),
        )
        whenever(storageHelper.getCachedFileFromUri(eq(context), any())) doReturn cachedFile
        Mockito.mockStatic(Uri::class.java).use { mockedUri ->
            mockedUri.`when`<Uri> { Uri.parse(sourceUri) }.thenReturn(parsedUri)

            val result = sut.resolveAttachmentFiles(listOf(alreadyResolved, noUri, deferred))

            assertSame(existingFile, result[0].upload)
            assertNull(result[1].upload)
            assertEquals(cachedFile, result[2].upload)
        }
    }

    @Test
    fun `resolveMetadata returns filtered metadata for given URIs`() {
        val uri1 = mock<Uri>()
        val uri2 = mock<Uri>()
        val uris = listOf(uri1, uri2)
        val rawMetadata = listOf(
            AttachmentMetaData(uri = uri1, type = "image", mimeType = "image/jpeg", title = "a.jpg"),
            AttachmentMetaData(uri = uri2, type = "file", mimeType = "application/pdf", title = "b.pdf"),
        )
        whenever(storageHelper.getAttachmentsFromUriList(context, uris)) doReturn rawMetadata

        val result = sut.resolveMetadata(uris)

        assertEquals(rawMetadata, result)
    }

    @Test
    fun `resolveMetadata returns empty list for empty input`() {
        whenever(storageHelper.getAttachmentsFromUriList(context, emptyList())) doReturn emptyList()

        val result = sut.resolveMetadata(emptyList())

        assertEquals(emptyList<AttachmentMetaData>(), result)
    }
}
