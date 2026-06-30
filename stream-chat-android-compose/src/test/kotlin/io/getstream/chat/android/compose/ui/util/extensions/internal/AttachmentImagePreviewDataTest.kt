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

package io.getstream.chat.android.compose.ui.util.extensions.internal

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.images.internal.VideoThumbnailImageData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class AttachmentImagePreviewDataTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `image uses the image url`() {
        val data = previewDataOf(Attachment(type = AttachmentType.IMAGE, imageUrl = IMAGE_URL))

        assertEquals(IMAGE_URL, data)
    }

    @Test
    fun `image without a url falls back to the local upload`() {
        val upload = File("image")
        val data = previewDataOf(Attachment(type = AttachmentType.IMAGE, imageUrl = null, upload = upload))

        assertEquals(upload, data)
    }

    @Test
    fun `video uses the thumbnail and the asset as fallback`() {
        val data = previewDataOf(Attachment(type = AttachmentType.VIDEO, thumbUrl = THUMB_URL, assetUrl = VIDEO_URL))

        assertEquals(VideoThumbnailImageData(thumbnailUrl = THUMB_URL, videoUrl = VIDEO_URL), data)
    }

    @Test
    fun `video without a thumbnail still uses the asset`() {
        val data = previewDataOf(Attachment(type = AttachmentType.VIDEO, thumbUrl = null, assetUrl = VIDEO_URL))

        assertEquals(VideoThumbnailImageData(thumbnailUrl = null, videoUrl = VIDEO_URL), data)
    }

    @Test
    fun `video without a thumbnail or asset falls back to the local upload`() {
        val upload = File("video")
        val data = previewDataOf(
            Attachment(type = AttachmentType.VIDEO, thumbUrl = null, assetUrl = null, upload = upload),
        )

        assertEquals(upload, data)
    }

    @Test
    fun `video returns null when video thumbnails are disabled`() {
        val data = previewDataOf(
            attachment = Attachment(type = AttachmentType.VIDEO, thumbUrl = THUMB_URL, assetUrl = VIDEO_URL),
            videoThumbnailsEnabled = false,
        )

        assertNull(data)
    }

    @Test
    fun `non-media attachment returns null`() {
        val data = previewDataOf(Attachment(type = AttachmentType.FILE, assetUrl = VIDEO_URL))

        assertNull(data)
    }

    private fun previewDataOf(attachment: Attachment, videoThumbnailsEnabled: Boolean = true): Any? {
        var data: Any? = UNSET
        composeTestRule.setContent {
            ChatTheme(videoThumbnailsEnabled = videoThumbnailsEnabled) {
                data = attachment.imagePreviewData
            }
        }
        return data
    }

    private companion object {
        private val UNSET = Any()
        private const val IMAGE_URL = "https://cdn.example.com/image.jpg"
        private const val THUMB_URL = "https://cdn.example.com/thumb.jpg"
        private const val VIDEO_URL = "https://cdn.example.com/video.mp4"
    }
}
