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
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
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
        assertTrue(AIMessageAttachment.file("file:///tmp/photo.jpg".toUri()).looksLikeImage())
        assertTrue(AIMessageAttachment.file(upload, mimeType = "image/png").looksLikeImage())
        assertFalse(AIMessageAttachment.file(pdf).looksLikeImage())
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
}
