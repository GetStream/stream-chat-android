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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(AndroidJUnit4::class)
internal class AttachmentInfoTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val pdf = "https://example.com/files/itinerary.pdf".toUri()
    private val upload = "https://example.com/uploads/7f3a".toUri()

    @Before
    fun setUp() {
        // The platform's map of media types, which Robolectric leaves empty. (Its method is spelled so.)
        shadowOf(MimeTypeMap.getSingleton()).apply {
            addExtensionMimeTypMapping("pdf", "application/pdf")
            addExtensionMimeTypMapping("png", "image/png")
            addExtensionMimeTypMapping("jpg", "image/jpeg")
        }
    }

    @Test
    fun `display name is its name, then its file name`() {
        assertEquals("Lisbon trip.pdf", AIMessageAttachment.file(pdf, name = "Lisbon trip.pdf").displayName(context))
        assertEquals("itinerary.pdf", AIMessageAttachment.file(pdf).displayName(context))
        assertEquals("itinerary.pdf", AIMessageAttachment.file(pdf, name = "").displayName(context))
    }

    @Test
    fun `display name says what it is without a name`() {
        assertEquals("Image", AIMessageAttachment(kind = AIMessageAttachment.Kind.Image).displayName(context))
        assertEquals("File", AIMessageAttachment(kind = AIMessageAttachment.Kind.File).displayName(context))
        assertEquals(
            "a page's root has no file name",
            "File",
            AIMessageAttachment.file("https://example.com/".toUri()).displayName(context),
        )
    }

    @Test
    fun `details are the type and the size`() {
        val size = Formatter.formatShortFileSize(context, 2_400_000)
        assertEquals("PDF · $size", AIMessageAttachment.file(pdf, size = 2_400_000).details(context))
        assertEquals("PDF", AIMessageAttachment.file(pdf).details(context))
        assertEquals(size, AIMessageAttachment(kind = AIMessageAttachment.Kind.File, size = 2_400_000).details(context))
        assertEquals("", AIMessageAttachment(kind = AIMessageAttachment.Kind.File).details(context))
    }

    @Test
    fun `details read the type from the name before the uri`() {
        assertEquals("DOCX", AIMessageAttachment.file(upload, name = "Notes.docx").details(context))
    }

    @Test
    fun `details read the type from the media type without an extension`() {
        assertEquals("PDF", AIMessageAttachment.file(upload, mimeType = "application/pdf").details(context))
    }

    @Test
    fun `an image file shows an image icon`() {
        assertEquals(AttachmentFileType.Image, AIMessageAttachment.file("file:///tmp/photo.jpg".toUri()).fileType())
        assertEquals(AttachmentFileType.Image, AIMessageAttachment.file(upload, mimeType = "image/png").fileType())
        assertEquals(AttachmentFileType.Pdf, AIMessageAttachment.file(pdf).fileType())
    }

    @Test
    fun `the icon follows the media type`() {
        val expected = mapOf(
            "application/pdf" to AttachmentFileType.Pdf,
            "image/heic" to AttachmentFileType.Image,
            "audio/mpeg" to AttachmentFileType.Audio,
            "audio/ogg; codecs=opus" to AttachmentFileType.Audio,
            "video/mp4" to AttachmentFileType.Video,
            "application/zip" to AttachmentFileType.Archive,
            "application/vnd.rar" to AttachmentFileType.Archive,
            "application/x-7z-compressed" to AttachmentFileType.Archive,
            "application/gzip" to AttachmentFileType.Archive,
            "APPLICATION/X-TAR" to AttachmentFileType.Archive,
            "text/plain" to AttachmentFileType.File,
            "application/octet-stream" to AttachmentFileType.File,
        )
        expected.forEach { (mimeType, type) ->
            assertEquals(mimeType, type, AIMessageAttachment.file(upload, mimeType = mimeType).fileType())
        }
    }

    @Test
    fun `the media type wins over the extension`() {
        val attachment = AIMessageAttachment.file(pdf, name = "Recording.pdf", mimeType = "audio/mp4")
        assertEquals(AttachmentFileType.Audio, attachment.fileType())
    }

    @Test
    fun `without a media type the icon follows the extension`() {
        shadowOf(MimeTypeMap.getSingleton()).apply {
            addExtensionMimeTypMapping("mp3", "audio/mpeg")
            addExtensionMimeTypMapping("mov", "video/quicktime")
        }
        val expected = mapOf(
            "Notes.PDF" to AttachmentFileType.Pdf,
            "Photo.jpg" to AttachmentFileType.Image,
            "Song.mp3" to AttachmentFileType.Audio,
            "Clip.mov" to AttachmentFileType.Video,
            "Backup.zip" to AttachmentFileType.Archive,
            "Backup.rar" to AttachmentFileType.Archive,
            "Backup.7z" to AttachmentFileType.Archive,
            "Backup.tar" to AttachmentFileType.Archive,
            "Backup.tar.gz" to AttachmentFileType.Archive,
            "Notes.docx" to AttachmentFileType.File,
        )
        expected.forEach { (name, type) ->
            assertEquals(name, type, AIMessageAttachment.file(upload, name = name).fileType())
        }
        assertEquals(
            "the extension of the uri without a name",
            AttachmentFileType.Archive,
            AIMessageAttachment.file("https://example.com/files/photos.zip".toUri()).fileType(),
        )
    }

    @Test
    fun `without a media type or extension the icon follows the kind`() {
        assertEquals(AttachmentFileType.Image, AIMessageAttachment.image(upload).fileType())
        assertEquals(AttachmentFileType.File, AIMessageAttachment.file(upload).fileType())
    }

    @Test
    fun `images decode on the android versions that read their format`() {
        assertTrue(isDecodableImage(mimeType = "image/jpeg", extension = null, sdkInt = 23))
        assertTrue(isDecodableImage(mimeType = null, extension = "webp", sdkInt = 23))
        assertFalse(isDecodableImage(mimeType = "image/heic", extension = null, sdkInt = 27))
        assertTrue(isDecodableImage(mimeType = "image/heic", extension = null, sdkInt = 28))
        assertFalse(isDecodableImage(mimeType = null, extension = "heif", sdkInt = 27))
        assertFalse(isDecodableImage(mimeType = "image/avif", extension = null, sdkInt = 30))
        assertTrue(isDecodableImage(mimeType = null, extension = "AVIF", sdkInt = 31))
        assertFalse(isDecodableImage(mimeType = "image/svg+xml", extension = "png", sdkInt = 33))
        assertFalse(isDecodableImage(mimeType = null, extension = null, sdkInt = 33))
    }

    @Test
    fun `a stream is read only up to its limit`() {
        val bytes = ByteArray(10) { it.toByte() }
        assertArrayEquals(bytes, bytes.inputStream().readAtMost(10))
        assertNull(bytes.inputStream().readAtMost(9))
    }

    @Test
    fun `an image is sampled down to at least the size it shows at`() {
        assertEquals(1, sampleSize(width = 400, height = 400, targetSizePx = 300))
        assertEquals(2, sampleSize(width = 1_200, height = 800, targetSizePx = 300))
        assertEquals(8, sampleSize(width = 4_000, height = 3_000, targetSizePx = 300))
        assertEquals(1, sampleSize(width = 4_000, height = 3_000, targetSizePx = 0))
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `a downloaded image is turned as its EXIF orientation says`() {
        // The color at the top-left and bottom-right of the shown image, for an image whose left
        // half is red and right half is blue as stored.
        val expected = mapOf(
            ExifInterface.ORIENTATION_NORMAL to (Color.RED to Color.BLUE),
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL to (Color.BLUE to Color.RED),
            ExifInterface.ORIENTATION_ROTATE_180 to (Color.BLUE to Color.RED),
            ExifInterface.ORIENTATION_FLIP_VERTICAL to (Color.RED to Color.BLUE),
            ExifInterface.ORIENTATION_TRANSPOSE to (Color.RED to Color.BLUE),
            ExifInterface.ORIENTATION_ROTATE_90 to (Color.RED to Color.BLUE),
            ExifInterface.ORIENTATION_TRANSVERSE to (Color.BLUE to Color.RED),
            ExifInterface.ORIENTATION_ROTATE_270 to (Color.BLUE to Color.RED),
        )
        val quarterTurns = setOf(
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_TRANSVERSE,
            ExifInterface.ORIENTATION_ROTATE_270,
        )

        expected.forEach { (orientation, colors) ->
            val bitmap = decodeSampledBitmap(jpeg(orientation), targetSizePx = 100)!!
            val upright = orientation in quarterTurns

            assertEquals("width for $orientation", if (upright) 30 else 60, bitmap.width)
            assertEquals("height for $orientation", if (upright) 60 else 30, bitmap.height)
            val topLeft = bitmap.getPixel(bitmap.width / 4, bitmap.height / 4)
            val bottomRight = bitmap.getPixel(bitmap.width * 3 / 4, bitmap.height * 3 / 4)
            assertEquals("top-left for $orientation", colors.first, nearest(topLeft))
            assertEquals("bottom-right for $orientation", colors.second, nearest(bottomRight))
        }
    }

    // A 60x30 JPEG, red on its left half and blue on its right, with an EXIF orientation.
    private fun jpeg(orientation: Int): ByteArray {
        val source = Bitmap.createBitmap(60, 30, Bitmap.Config.ARGB_8888)
        for (x in 0 until 60) {
            for (y in 0 until 30) source.setPixel(x, y, if (x < 30) Color.RED else Color.BLUE)
        }
        val file = File.createTempFile("image", ".jpg", context.cacheDir)
        file.outputStream().use { source.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        ExifInterface(file).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            saveAttributes()
        }
        return file.readBytes()
    }

    private fun nearest(pixel: Int): Int = if (Color.red(pixel) > Color.blue(pixel)) Color.RED else Color.BLUE
}
