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
}
