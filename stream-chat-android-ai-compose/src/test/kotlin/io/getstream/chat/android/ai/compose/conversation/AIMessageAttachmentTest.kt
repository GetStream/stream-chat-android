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

package io.getstream.chat.android.ai.compose.conversation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
internal class AIMessageAttachmentTest {

    private val uri = "https://example.com/files/itinerary.pdf".toUri()

    @Test
    fun `an image to load is an image at its uri`() {
        val image = AIMessageAttachment.image("https://example.com/photo.jpg".toUri(), name = "Sunset.jpg")

        assertEquals(AIMessageAttachment.Kind.Image, image.kind)
        assertEquals("https://example.com/photo.jpg".toUri(), image.uri)
        assertEquals("Sunset.jpg", image.name)
        assertNull(image.bitmap)
        assertNull(image.size)
        assertNull(image.mimeType)
        assertEquals(emptyMap<String, Any?>(), image.extraData)
    }

    @Test
    fun `an image the app holds has no uri`() {
        val bitmap = ImageBitmap(2, 2)

        val image = AIMessageAttachment.image(bitmap)

        assertEquals(AIMessageAttachment.Kind.Image, image.kind)
        assertSame(bitmap, image.bitmap)
        assertNull(image.uri)
        assertNull(image.name)
    }

    @Test
    fun `a file has its name, size and media type`() {
        val file = AIMessageAttachment.file(uri, name = "Itinerary.pdf", size = 2_400_000, mimeType = "application/pdf")

        assertEquals(AIMessageAttachment.Kind.File, file.kind)
        assertEquals(uri, file.uri)
        assertEquals("Itinerary.pdf", file.name)
        assertEquals(2_400_000L, file.size)
        assertEquals("application/pdf", file.mimeType)
        assertNull(file.bitmap)
    }

    @Test
    fun `an attachment from an image file is an image`() {
        val photo = AIMessageAttachment.from("file:///tmp/photo.HEIC".toUri(), size = 1_200_000)

        assertEquals(AIMessageAttachment.Kind.Image, photo.kind)
        assertEquals("file:///tmp/photo.HEIC".toUri(), photo.uri)
        assertEquals(1_200_000L, photo.size)
        listOf("a.jpg", "a.jpeg", "a.png", "a.webp", "a.gif", "a.bmp", "a.heif", "a.avif").forEach { name ->
            assertEquals(name, AIMessageAttachment.Kind.Image, AIMessageAttachment.from(uri, name = name).kind)
        }
    }

    @Test
    fun `an attachment from any other file is a file`() {
        val file = AIMessageAttachment.from(uri, name = "Itinerary.pdf", size = 2_400_000, mimeType = "application/pdf")

        assertEquals(AIMessageAttachment.Kind.File, file.kind)
        assertEquals(AIMessageAttachment.file(uri, "Itinerary.pdf", 2_400_000, "application/pdf"), file)
        assertEquals(
            "images load as bitmaps, which a vector image is not",
            AIMessageAttachment.Kind.File,
            AIMessageAttachment.from("file:///tmp/logo.svg".toUri()).kind,
        )
        assertEquals(AIMessageAttachment.Kind.File, AIMessageAttachment.from("https://example.com/uploads/7f3a".toUri()).kind)
    }

    @Test
    fun `an attachment from a file reads the media type over the extension`() {
        val upload = "https://example.com/uploads/7f3a".toUri()

        assertEquals(AIMessageAttachment.Kind.Image, AIMessageAttachment.from(upload, mimeType = "image/png").kind)
        assertEquals(AIMessageAttachment.Kind.Image, AIMessageAttachment.from(upload, mimeType = "IMAGE/JPEG").kind)
        assertEquals(
            AIMessageAttachment.Kind.File,
            AIMessageAttachment.from(uri, name = "photo.jpg", mimeType = "application/octet-stream").kind,
        )
        assertEquals(
            "the name's extension before the uri's",
            AIMessageAttachment.Kind.Image,
            AIMessageAttachment.from(uri, name = "Scan.png").kind,
        )
    }

    @Test
    fun `attachments with the same values are equal`() {
        val extra = mapOf("pages" to 3)
        val one = AIMessageAttachment(AIMessageAttachment.Kind.File, uri, null, "a.pdf", 10, "application/pdf", extra)
        val other = AIMessageAttachment(AIMessageAttachment.Kind.File, uri, null, "a.pdf", 10, "application/pdf", extra)

        assertEquals(one, one)
        assertEquals(one, other)
        assertEquals(one.hashCode(), other.hashCode())
        assertEquals(AIMessageAttachment.image(uri).hashCode(), AIMessageAttachment.image(uri).hashCode())
    }

    @Test
    fun `attachments differing in any value aren't equal`() {
        val bitmap = ImageBitmap(1, 1)
        val base = AIMessageAttachment(AIMessageAttachment.Kind.File, uri, bitmap, "a.pdf", 10, "application/pdf")
        val variants = listOf(
            AIMessageAttachment(AIMessageAttachment.Kind.Image, uri, bitmap, "a.pdf", 10, "application/pdf"),
            AIMessageAttachment(AIMessageAttachment.Kind.File, null, bitmap, "a.pdf", 10, "application/pdf"),
            AIMessageAttachment(AIMessageAttachment.Kind.File, uri, ImageBitmap(1, 1), "a.pdf", 10, "application/pdf"),
            AIMessageAttachment(AIMessageAttachment.Kind.File, uri, bitmap, "b.pdf", 10, "application/pdf"),
            AIMessageAttachment(AIMessageAttachment.Kind.File, uri, bitmap, "a.pdf", 11, "application/pdf"),
            AIMessageAttachment(AIMessageAttachment.Kind.File, uri, bitmap, "a.pdf", 10, "text/plain"),
            AIMessageAttachment(AIMessageAttachment.Kind.File, uri, bitmap, "a.pdf", 10, "application/pdf", mapOf("x" to 1)),
        )

        variants.forEach { assertNotEquals(base, it) }
        assertNotEquals(base, "a.pdf")
        assertNotEquals(base, null)
    }

    @Test
    fun `an attachment reads as its kind, uri and name`() {
        assertEquals(
            "AIMessageAttachment(kind=file, uri=$uri, name=Itinerary.pdf)",
            AIMessageAttachment.file(uri, name = "Itinerary.pdf").toString(),
        )
    }

    @Test
    fun `a kind is an open set named by its raw value`() {
        assertEquals("image", AIMessageAttachment.Kind.Image.rawValue)
        assertEquals("file", AIMessageAttachment.Kind.File.toString())
        assertEquals(AIMessageAttachment.Kind("audio"), AIMessageAttachment.Kind("audio"))
        assertNotEquals(AIMessageAttachment.Kind.File, AIMessageAttachment.Kind("audio"))
    }
}
