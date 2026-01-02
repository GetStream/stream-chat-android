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

package io.getstream.chat.android.ui.common.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.test.TestCall
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.Result.Failure
import io.getstream.result.Result.Success
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertInstanceOf
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class StreamFileUtilTest {

    private val mockContext = mock<Context>()

    @Test
    fun `getUriForFile returns uri from FileProvider`() {
        val mockPackageManager = mock(PackageManager::class.java)
        val providerInfo = ProviderInfo().apply { authority = "test.authority" }
        whenever(mockContext.packageManager) doReturn mockPackageManager
        whenever(mockPackageManager.getProviderInfo(any<ComponentName>(), eq(0))) doReturn
            providerInfo
        val file = File(mockContext.cacheDir, "test.txt")
        val uri = "content://test/test.txt".toUri()

        mockStatic(FileProvider::class.java).use { mocked ->
            mocked.`when`<Uri> {
                FileProvider.getUriForFile(any(), anyString(), any())
            } doReturn uri

            val actual = StreamFileUtil.getUriForFile(mockContext, file)

            assertEquals(uri, actual)
        }
    }

    @Test
    fun `writeImageToSharableFile returns Success uri when bitmap is written`() = runTest {
        val mockBitmap = mock(Bitmap::class.java)
        whenever(mockBitmap.compress(eq(Bitmap.CompressFormat.PNG), anyInt(), any())) doReturn true
        val uri = "content://test/test.txt".toUri()

        mockStatic(FileProvider::class.java).use { mocked ->
            mocked.`when`<Uri> {
                FileProvider.getUriForFile(any(), anyString(), any())
            } doReturn uri

            val result = StreamFileUtil.writeImageToSharableFile(mockContext, mockBitmap) { uri }

            assertInstanceOf<Success<Uri>>(result)
            assertEquals(uri, result.value)
        }
    }

    @Test
    fun `writeImageToSharableFile returns Failure on IOException`() = runTest {
        val mockBitmap = mock(Bitmap::class.java)
        whenever(mockBitmap.compress(eq(Bitmap.CompressFormat.PNG), anyInt(), any())) doAnswer {
            throw IOException("fail")
        }

        val result = StreamFileUtil.writeImageToSharableFile(mockContext, mockBitmap)

        assertInstanceOf<Failure>(result)
        assertEquals("Could not write image to file.", result.value.message)
    }

    @Test
    fun `createFileInCacheDir returns Success file for valid file`() {
        val fileName = "file.txt"

        val result = StreamFileUtil.createFileInCacheDir(mockContext, fileName)

        assertInstanceOf<Success<File>>(result)
        val file = result.value
        assertEquals(fileName, file.name)
    }

    @Test
    fun `createFileInCacheDir returns Failure on exception`() {
        whenever(mockContext.cacheDir).thenThrow(RuntimeException("fail"))

        val result = StreamFileUtil.createFileInCacheDir(mockContext, "file.txt")

        assertInstanceOf<Failure>(result)
        assertEquals("Could not get or create the Stream cache directory", result.value.message)
    }

    @Test
    fun `clearStreamCache deletes directory and returns Success`() {
        val cacheDir = File(mockContext.cacheDir, "stream_cache").also(File::mkdirs)
        File(cacheDir, "temp.txt").writeText("test")
        assertTrue(cacheDir.exists())

        val result = StreamFileUtil.clearStreamCache(mockContext)

        assertInstanceOf<Success<Uri>>(result)
        assertFalse(cacheDir.exists())
    }

    @Test
    fun `clearStreamCache returns Failure on exception`() {
        whenever(mockContext.cacheDir).thenThrow(RuntimeException("fail"))

        val result = StreamFileUtil.clearStreamCache(mockContext)

        assertInstanceOf<Failure>(result)
    }

    @Test
    fun `getFileFromCache returns Success if file exists and size matches`() = runTest {
        val attachment = randomAttachment(fileSize = 4)
        val hash = attachment.assetUrl?.hashCode()
        val fileName = "TMP${hash}${attachment.name}"
        val cacheDir = File(mockContext.cacheDir, "stream_cache").also(File::mkdirs)
        val file = File(cacheDir, fileName)
        file.writeBytes(ByteArray(4))

        val result = StreamFileUtil.getFileFromCache(mockContext, attachment) { file.toUri() }

        assertInstanceOf<Success<Uri>>(result)
        assertNotNull(result.value)
    }

    @Test
    fun `getFileFromCache returns Failure if file does not exist`() = runTest {
        val attachment = randomAttachment()

        val result = StreamFileUtil.getFileFromCache(mockContext, attachment)

        assertInstanceOf<Failure>(result)
    }

    @Test
    fun `writeFileToShareableFile returns Failure if url is null`() = runTest {
        val attachment = randomAttachment(assetUrl = null, imageUrl = null)

        val result = StreamFileUtil.writeFileToShareableFile(mockContext, attachment)

        assertTrue(result is Failure)
    }

    @Test
    fun `writeFileToShareableFile returns Success if file exists and size matches`() = runTest {
        val attachment = randomAttachment(fileSize = 4)
        val hash = attachment.assetUrl?.hashCode()
        val fileName = "TMP${hash}${attachment.name}"
        val cacheDir = File(mockContext.cacheDir, "stream_cache").also(File::mkdirs)
        val file = File(cacheDir, fileName)
        file.writeBytes(ByteArray(4))

        val result = StreamFileUtil.writeFileToShareableFile(mockContext, attachment) { file.toUri() }

        assertInstanceOf<Success<Uri>>(result)
    }

    @Test
    fun `writeFileToShareableFile downloads and writes file if not cached`() = runTest {
        val attachment = randomAttachment(fileSize = 4)
        val chatClient = mock(ChatClient::class.java)
        whenever(chatClient.downloadFile(anyString())) doReturn
            TestCall(Success(TestResponseBody()))

        val result = StreamFileUtil.writeFileToShareableFile(
            context = mockContext,
            attachment = attachment,
            chatClient = { chatClient },
            getUri = { Uri.EMPTY },
        )

        assertInstanceOf<Success<Uri>>(result)
    }

    @Test
    fun `writeFileToShareableFile returns Failure if download fails`() = runTest {
        val attachment = randomAttachment()
        val chatClient = mock(ChatClient::class.java)
        val downloadResult: Result<ResponseBody> = Failure(Error.GenericError("download fail"))
        whenever(chatClient.downloadFile(anyString())) doReturn TestCall(downloadResult)

        val result = StreamFileUtil.writeFileToShareableFile(
            context = mockContext,
            attachment = attachment,
            chatClient = { chatClient },
            getUri = { Uri.EMPTY },
        )

        assertInstanceOf<Failure>(result)
    }
}

private class TestResponseBody : ResponseBody() {
    private val buffer = Buffer().writeString("", Charset.defaultCharset())
    override fun contentLength(): Long = buffer.size
    override fun contentType(): MediaType? = "application/json".toMediaType()
    override fun source(): BufferedSource = buffer
}
