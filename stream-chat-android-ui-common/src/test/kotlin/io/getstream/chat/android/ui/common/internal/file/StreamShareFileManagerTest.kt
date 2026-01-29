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

package io.getstream.chat.android.ui.common.internal.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.internal.file.StreamFileManager
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.test.TestCall
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.io.File
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class StreamShareFileManagerTest {

    private lateinit var context: Context
    private lateinit var fileManager: StreamFileManager
    private lateinit var uriProvider: ShareableUriProvider
    private lateinit var shareFileManager: StreamShareFileManager

    @Before
    fun setUp() {
        context = mock()
        fileManager = mock()
        uriProvider = mock()
        shareFileManager = StreamShareFileManager(fileManager, uriProvider)

        val uri = "content://path/to/file.txt".toUri()
        whenever(uriProvider.getUriForFile(any(), any())).thenReturn(uri)
    }

    @Test
    fun `writeBitmapToShareableFile returns Uri when write is successful`() = runTest {
        // given
        val file = File("path/to/file.txt")
        val bitmap = createTestBitmap()
        whenever(fileManager.writeFileInCache(any(), any(), any()))
            .thenReturn(Result.Success(file))

        // when
        val result = shareFileManager.writeBitmapToShareableFile(context, bitmap)

        // then
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `writeBitmapToShareableFile returns Error when write fails`() = runTest {
        // given
        val bitmap = createTestBitmap()
        whenever(fileManager.writeFileInCache(any(), any(), any()))
            .thenReturn(Result.Failure(Error.GenericError("Write failed")))

        // when
        val result = shareFileManager.writeBitmapToShareableFile(context, bitmap)

        // then
        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `writeBitmapToShareableFile handles exception and returns Error`() = runTest {
        // given
        val bitmap = createTestBitmap()
        whenever(fileManager.writeFileInCache(any(), any(), any()))
            .thenThrow(RuntimeException("Unexpected error"))

        // when
        val result = shareFileManager.writeBitmapToShareableFile(context, bitmap)

        // then
        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `writeAttachmentToShareableFile returns cached file Uri when attachment is already cached`() = runTest {
        // given
        val attachment = randomAttachment(
            assetUrl = "https://example.com/file.pdf",
            fileSize = 1024,
            name = "document.pdf",
        )
        val cachedFile = mock<File>()
        whenever(fileManager.getFileFromCache(any(), any()))
            .thenReturn(Result.Success(cachedFile))
        whenever(cachedFile.exists()).thenReturn(true)
        whenever(cachedFile.length()).thenReturn(1024L)

        // when
        val result = shareFileManager.writeAttachmentToShareableFile(context, attachment)

        // then
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `writeAttachmentToShareableFile downloads and caches file when not cached`() = runTest {
        // given
        val attachment = randomAttachment(
            assetUrl = "https://example.com/file.pdf",
            fileSize = 1024,
            name = "document.pdf",
        )
        val downloadedFile = File("path/to/downloaded/file.pdf")
        val chatClient = mock<ChatClient>()
        val responseBody = TestResponseBody("test content")

        // File not cached
        whenever(fileManager.getFileFromCache(any(), any()))
            .thenReturn(Result.Failure(Error.GenericError("Not cached")))

        // Download succeeds
        whenever(chatClient.downloadFile(any())) doReturn TestCall(Result.Success(responseBody))

        // Write succeeds
        whenever(fileManager.writeFileInCache(any(), any(), any()))
            .thenReturn(Result.Success(downloadedFile))

        // when
        val result = shareFileManager.writeAttachmentToShareableFile(
            context = context,
            attachment = attachment,
            chatClient = { chatClient },
        )

        // then
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `writeAttachmentToShareableFile returns Error when file URL is null`() = runTest {
        // given
        val attachment = randomAttachment(assetUrl = null, imageUrl = null)
        whenever(fileManager.getFileFromCache(any(), any()))
            .thenReturn(Result.Failure(Error.GenericError("Not cached")))

        // when
        val result = shareFileManager.writeAttachmentToShareableFile(context, attachment)

        // then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue((result as Result.Failure).value is Error.GenericError)
    }

    @Test
    fun `writeAttachmentToShareableFile returns Error when download fails`() = runTest {
        // given
        val attachment = randomAttachment(
            assetUrl = "https://example.com/file.pdf",
            fileSize = 1024,
            name = "document.pdf",
        )
        val chatClient = mock<ChatClient>()

        // File not cached
        whenever(fileManager.getFileFromCache(any(), any()))
            .thenReturn(Result.Failure(Error.GenericError("Not cached")))

        // Download fails
        whenever(chatClient.downloadFile(any())) doReturn
            TestCall(Result.Failure(Error.GenericError("Download failed")))

        // when
        val result = shareFileManager.writeAttachmentToShareableFile(
            context = context,
            attachment = attachment,
            chatClient = { chatClient },
        )

        // then
        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `writeAttachmentToShareableFile returns Error when write to cache fails`() = runTest {
        // given
        val attachment = randomAttachment(
            assetUrl = "https://example.com/file.pdf",
            fileSize = 1024,
            name = "document.pdf",
        )
        val chatClient = mock<ChatClient>()
        val responseBody = TestResponseBody("test content")

        // File not cached
        whenever(fileManager.getFileFromCache(any(), any()))
            .thenReturn(Result.Failure(Error.GenericError("Not cached")))

        // Download succeeds
        whenever(chatClient.downloadFile(any())) doReturn TestCall(Result.Success(responseBody))

        // Write fails
        whenever(fileManager.writeFileInCache(any(), any(), any()))
            .thenReturn(Result.Failure(Error.GenericError("Write failed")))

        // when
        val result = shareFileManager.writeAttachmentToShareableFile(
            context = context,
            attachment = attachment,
            chatClient = { chatClient },
        )

        // then
        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `writeAttachmentToShareableFile uses imageUrl when assetUrl is null`() = runTest {
        // given
        val attachment = randomAttachment(
            assetUrl = null,
            imageUrl = "https://example.com/image.jpg",
            fileSize = 2048,
            name = "photo.jpg",
        )
        val downloadedFile = File("path/to/downloaded/image.jpg")
        val chatClient = mock<ChatClient>()
        val responseBody = TestResponseBody("image data")

        // File not cached
        whenever(fileManager.getFileFromCache(any(), any()))
            .thenReturn(Result.Failure(Error.GenericError("Not cached")))

        // Download succeeds
        whenever(chatClient.downloadFile(any())) doReturn TestCall(Result.Success(responseBody))

        // Write succeeds
        whenever(fileManager.writeFileInCache(any(), any(), any()))
            .thenReturn(Result.Success(downloadedFile))

        // when
        val result = shareFileManager.writeAttachmentToShareableFile(
            context = context,
            attachment = attachment,
            chatClient = { chatClient },
        )

        // then
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `getShareableUriForAttachment returns Uri when file is cached`() = runTest {
        // given
        val attachment = randomAttachment(
            assetUrl = "https://example.com/file.pdf",
            fileSize = 1024,
            name = "document.pdf",
        )
        val cachedFile = mock<File>()
        whenever(fileManager.getFileFromCache(any(), any()))
            .thenReturn(Result.Success(cachedFile))
        whenever(cachedFile.exists()).thenReturn(true)
        whenever(cachedFile.length()).thenReturn(1024L)

        // when
        val result = shareFileManager.getShareableUriForAttachment(context, attachment)

        // then
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `getShareableUriForAttachment returns Error when file is not cached`() = runTest {
        // given
        val attachment = randomAttachment(
            assetUrl = "https://example.com/file.pdf",
            fileSize = 1024,
            name = "document.pdf",
        )
        whenever(fileManager.getFileFromCache(any(), any()))
            .thenReturn(Result.Failure(Error.GenericError("Not cached")))

        // when
        val result = shareFileManager.getShareableUriForAttachment(context, attachment)

        // then
        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `getShareableUriForAttachment returns Error when cached file does not exist`() = runTest {
        // given
        val attachment = randomAttachment(
            assetUrl = "https://example.com/file.pdf",
            fileSize = 1024,
            name = "document.pdf",
        )
        val cachedFile = mock<File>()
        whenever(fileManager.getFileFromCache(any(), any()))
            .thenReturn(Result.Success(cachedFile))
        whenever(cachedFile.exists()).thenReturn(false)

        // when
        val result = shareFileManager.getShareableUriForAttachment(context, attachment)

        // then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue((result as Result.Failure).value is Error.GenericError)
    }

    @Test
    fun `getShareableUriForAttachment returns Error when cached file size does not match`() = runTest {
        // given
        val attachment = randomAttachment(
            assetUrl = "https://example.com/file.pdf",
            fileSize = 1024,
            name = "document.pdf",
        )
        val cachedFile = mock<File>()
        whenever(fileManager.getFileFromCache(any(), any()))
            .thenReturn(Result.Success(cachedFile))
        whenever(cachedFile.exists()).thenReturn(true)
        whenever(cachedFile.length()).thenReturn(512L) // Different size

        // when
        val result = shareFileManager.getShareableUriForAttachment(context, attachment)

        // then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue((result as Result.Failure).value is Error.GenericError)
    }

    private fun createTestBitmap(width: Int = 100, height: Int = 100): Bitmap {
        // 1. Define the pixel colors. Here, a simple pattern of red and black.
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                // Set a test pattern (e.g., a simple checkerboard or a solid color)
                pixels[y * width + x] = if ((x + y) % 2 == 0) Color.RED else Color.BLACK
            }
        }

        // 2. Create the bitmap from the pixel array
        val bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

        // 3. Return the created bitmap
        return bitmap
    }

    private class TestResponseBody(content: String) : ResponseBody() {
        private val buffer = Buffer().writeString(content, Charset.defaultCharset())
        override fun contentLength(): Long = buffer.size
        override fun contentType() = "application/octet-stream".toMediaType()
        override fun source(): BufferedSource = buffer
    }
}
