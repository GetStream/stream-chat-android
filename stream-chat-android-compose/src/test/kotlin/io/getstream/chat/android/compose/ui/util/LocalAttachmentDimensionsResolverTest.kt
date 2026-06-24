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

package io.getstream.chat.android.compose.ui.util

import android.media.MediaMetadataRetriever
import io.getstream.chat.android.models.Attachment
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowMediaMetadataRetriever
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.NEWEST_SDK])
internal class LocalAttachmentDimensionsResolverTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @After
    fun tearDown() {
        ShadowMediaMetadataRetriever.reset()
    }

    @Test
    fun `resolveDimensions keeps video dimensions when rotation does not swap them`() {
        val file = videoFile(rotation = 180)
        val attachment = videoAttachment()

        val resolved = LocalAttachmentDimensionsResolver.resolveDimensions(attachment, file)

        assertEquals(RAW_WIDTH, resolved.originalWidth)
        assertEquals(RAW_HEIGHT, resolved.originalHeight)
    }

    @Test
    fun `resolveDimensions swaps video dimensions when rotation is 90`() {
        val file = videoFile(rotation = 90)
        val attachment = videoAttachment()

        val resolved = LocalAttachmentDimensionsResolver.resolveDimensions(attachment, file)

        assertEquals(RAW_HEIGHT, resolved.originalWidth)
        assertEquals(RAW_WIDTH, resolved.originalHeight)
    }

    @Test
    fun `resolveDimensions swaps video dimensions when rotation is 270`() {
        val file = videoFile(rotation = 270)
        val attachment = videoAttachment()

        val resolved = LocalAttachmentDimensionsResolver.resolveDimensions(attachment, file)

        assertEquals(RAW_HEIGHT, resolved.originalWidth)
        assertEquals(RAW_WIDTH, resolved.originalHeight)
    }

    @Test
    fun `resolveDimensions returns attachment unchanged when file is null`() {
        val attachment = imageAttachment()

        val resolved = LocalAttachmentDimensionsResolver.resolveDimensions(attachment, file = null)

        assertSame(attachment, resolved)
    }

    @Test
    fun `resolveDimensions does not overwrite already populated dimensions`() {
        val file = videoFile(rotation = 90)
        val attachment = videoAttachment().copy(originalWidth = 123, originalHeight = 456)

        val resolved = LocalAttachmentDimensionsResolver.resolveDimensions(attachment, file)

        assertEquals(123, resolved.originalWidth)
        assertEquals(456, resolved.originalHeight)
    }

    @Test
    fun `resolveDimensions returns attachment unchanged for non-media attachments`() {
        val file = tempFolder.newFile("document.pdf")
        val attachment = Attachment(type = "file", mimeType = "application/pdf", name = "document.pdf")

        val resolved = LocalAttachmentDimensionsResolver.resolveDimensions(attachment, file)

        assertNull(resolved.originalWidth)
        assertNull(resolved.originalHeight)
    }

    private fun videoFile(rotation: Int): File {
        val file = tempFolder.newFile("video.mp4")
        val path = file.absolutePath
        ShadowMediaMetadataRetriever.addMetadata(
            path,
            MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH,
            RAW_WIDTH.toString(),
        )
        ShadowMediaMetadataRetriever.addMetadata(
            path,
            MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT,
            RAW_HEIGHT.toString(),
        )
        ShadowMediaMetadataRetriever.addMetadata(
            path,
            MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION,
            rotation.toString(),
        )
        return file
    }

    private fun videoAttachment() = Attachment(type = "video", mimeType = "video/mp4", name = "video.mp4")

    private fun imageAttachment() = Attachment(type = "image", mimeType = "image/jpeg", name = "photo.jpg")

    private companion object {
        private const val RAW_WIDTH = 80
        private const val RAW_HEIGHT = 40
    }
}
