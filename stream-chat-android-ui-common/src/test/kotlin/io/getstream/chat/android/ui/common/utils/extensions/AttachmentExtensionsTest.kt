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

package io.getstream.chat.android.ui.common.utils.extensions

import io.getstream.chat.android.models.Attachment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@Suppress("DEPRECATION")
internal class AttachmentExtensionsTest {

    @Test
    fun `imagePreviewUrl returns thumbUrl when both thumbUrl and imageUrl are set`() {
        val attachment = Attachment(
            thumbUrl = "https://cdn.example.com/thumb.jpg",
            imageUrl = "https://cdn.example.com/image.jpg",
        )

        assertEquals("https://cdn.example.com/thumb.jpg", attachment.imagePreviewUrl)
    }

    @Test
    fun `imagePreviewUrl returns thumbUrl when imageUrl is null`() {
        val attachment = Attachment(
            thumbUrl = "https://cdn.example.com/thumb.jpg",
            imageUrl = null,
        )

        assertEquals("https://cdn.example.com/thumb.jpg", attachment.imagePreviewUrl)
    }

    @Test
    fun `imagePreviewUrl returns imageUrl when thumbUrl is null`() {
        val attachment = Attachment(
            thumbUrl = null,
            imageUrl = "https://cdn.example.com/image.jpg",
        )

        assertEquals("https://cdn.example.com/image.jpg", attachment.imagePreviewUrl)
    }

    @Test
    fun `imagePreviewUrl returns null when both are null`() {
        val attachment = Attachment(
            thumbUrl = null,
            imageUrl = null,
        )

        assertNull(attachment.imagePreviewUrl)
    }

    // linkPreviewImageUrl tests

    @Test
    fun `linkPreviewImageUrl returns thumbUrl when both thumbUrl and imageUrl are set`() {
        val attachment = Attachment(thumbUrl = "thumb", imageUrl = "image")
        assertEquals("thumb", attachment.linkPreviewImageUrl)
    }

    @Test
    fun `linkPreviewImageUrl returns imageUrl when thumbUrl is null`() {
        val attachment = Attachment(thumbUrl = null, imageUrl = "image")
        assertEquals("image", attachment.linkPreviewImageUrl)
    }

    @Test
    fun `linkPreviewImageUrl returns null when both are null`() {
        val attachment = Attachment(thumbUrl = null, imageUrl = null)
        assertNull(attachment.linkPreviewImageUrl)
    }

    // linkUrl tests

    @Test
    fun `linkUrl returns titleLink when both titleLink and ogUrl are set`() {
        val attachment = Attachment(titleLink = "titleLink", ogUrl = "ogUrl")
        assertEquals("titleLink", attachment.linkUrl)
    }

    @Test
    fun `linkUrl returns ogUrl when titleLink is null`() {
        val attachment = Attachment(titleLink = null, ogUrl = "ogUrl")
        assertEquals("ogUrl", attachment.linkUrl)
    }

    @Test
    fun `linkUrl returns null when both are null`() {
        val attachment = Attachment(titleLink = null, ogUrl = null)
        assertNull(attachment.linkUrl)
    }

    // giphyFallbackPreviewUrl tests

    @Test
    fun `giphyFallbackPreviewUrl returns thumbUrl when all are set`() {
        val attachment = Attachment(thumbUrl = "thumb", titleLink = "titleLink", ogUrl = "ogUrl")
        assertEquals("thumb", attachment.giphyFallbackPreviewUrl)
    }

    @Test
    fun `giphyFallbackPreviewUrl returns titleLink when thumbUrl is null`() {
        val attachment = Attachment(thumbUrl = null, titleLink = "titleLink", ogUrl = "ogUrl")
        assertEquals("titleLink", attachment.giphyFallbackPreviewUrl)
    }

    @Test
    fun `giphyFallbackPreviewUrl returns ogUrl when thumbUrl and titleLink are null`() {
        val attachment = Attachment(thumbUrl = null, titleLink = null, ogUrl = "ogUrl")
        assertEquals("ogUrl", attachment.giphyFallbackPreviewUrl)
    }

    @Test
    fun `giphyFallbackPreviewUrl returns null when all are null`() {
        val attachment = Attachment(thumbUrl = null, titleLink = null, ogUrl = null)
        assertNull(attachment.giphyFallbackPreviewUrl)
    }
}
