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

package io.getstream.chat.android.client.internal.file

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class StreamFileManagerTest {

    private lateinit var context: Context
    private lateinit var streamFileManager: StreamFileManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        streamFileManager = StreamFileManager()
    }

    @After
    fun tearDown() {
        // Clean up all cache directories created during tests
        streamFileManager.clearAllCache(context)
    }

    @Test
    fun `getImageCache should return correct directory path`() {
        val imageCache = streamFileManager.getImageCache(context)

        assertEquals(File(context.cacheDir, "stream_image_cache").path, imageCache.path)
    }

    @Test
    fun `createFileInCache should return file reference without creating content`() {
        val fileName = "test_${randomString()}.txt"

        val result = streamFileManager.createFileInCache(context, fileName)

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        assertEquals(fileName, file.name)
        assertFalse(file.exists()) // File reference exists but no content written
    }

    @Test
    fun `writeFileInCache should successfully write file to cache`() = runTest {
        val fileName = "test_${randomString()}.txt"
        val content = "Hello, Stream Chat!"
        val inputStream = content.byteInputStream()

        val result = streamFileManager.writeFileInCache(context, fileName, inputStream)

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        assertTrue(file.exists())
        assertEquals(content, file.readText())
        assertEquals("stream_cache", file.parentFile?.name)
    }

    @Test
    fun `writeFileInCache should overwrite existing file`() = runTest {
        val fileName = "test_${randomString()}.txt"
        val firstContent = "First content"
        val secondContent = "Second content"

        // Write first time
        streamFileManager.writeFileInCache(context, fileName, firstContent.byteInputStream())

        // Write second time with different content
        val result = streamFileManager.writeFileInCache(context, fileName, secondContent.byteInputStream())

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        assertEquals(secondContent, file.readText())
    }

    @Test
    fun `writeFileInTimestampedCache should create file in timestamped folder`() {
        val fileName = "test_${randomString()}.txt"
        val content = "Timestamped content"
        val inputStream = content.byteInputStream()

        val result = streamFileManager.writeFileInTimestampedCache(context, fileName, inputStream)

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        assertTrue(file.exists())
        assertEquals(content, file.readText())
        assertTrue(file.parentFile?.name?.startsWith("STREAM_") == true)
    }

    @Test
    fun `writeFileInTimestampedCache should create unique folders for each call`() {
        val fileName = "test.txt"
        val content = "Content"

        val result1 = streamFileManager.writeFileInTimestampedCache(context, fileName, content.byteInputStream())
        Thread.sleep(10) // Small delay to ensure different timestamps
        val result2 = streamFileManager.writeFileInTimestampedCache(context, fileName, content.byteInputStream())

        assertTrue(result1 is Result.Success)
        assertTrue(result2 is Result.Success)
        val file1 = (result1 as Result.Success).value
        val file2 = (result2 as Result.Success).value
        assertNotEquals(file1.parentFile?.path, file2.parentFile?.path)
    }

    @Test
    fun `getFileFromCache should return success when file exists`() = runTest {
        val fileName = "existing_${randomString()}.txt"
        val content = "File content"

        // First create the file
        streamFileManager.writeFileInCache(context, fileName, content.byteInputStream())

        // Then retrieve it
        val result = streamFileManager.getFileFromCache(context, fileName)

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        assertTrue(file.exists())
        assertEquals(content, file.readText())
    }

    @Test
    fun `getFileFromCache should return failure when file does not exist`() = runTest {
        val fileName = "nonexistent_${randomString()}.txt"

        val result = streamFileManager.getFileFromCache(context, fileName)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `clearCache should delete all files in stream cache directory`() = runTest {
        val fileName1 = "file1_${randomString()}.txt"
        val fileName2 = "file2_${randomString()}.txt"

        // Create multiple files
        streamFileManager.writeFileInCache(context, fileName1, "content1".byteInputStream())
        streamFileManager.writeFileInCache(context, fileName2, "content2".byteInputStream())

        // Verify files exist
        val file1Result = streamFileManager.getFileFromCache(context, fileName1)
        val file2Result = streamFileManager.getFileFromCache(context, fileName2)
        assertTrue(file1Result is Result.Success)
        assertTrue(file2Result is Result.Success)

        // Clear cache
        val clearResult = streamFileManager.clearCache(context)

        assertTrue(clearResult is Result.Success)
        assertTrue(streamFileManager.getFileFromCache(context, fileName1) is Result.Failure)
        assertTrue(streamFileManager.getFileFromCache(context, fileName2) is Result.Failure)
    }

    @Test
    fun `clearAllCache should clear stream cache, image cache, and timestamped folders`() = runTest {
        // Create file in stream cache
        val streamFileName = "stream_${randomString()}.txt"
        streamFileManager.writeFileInCache(context, streamFileName, "content".byteInputStream())

        // Create file in timestamped cache
        val timestampedFileName = "timestamped_${randomString()}.txt"
        streamFileManager.writeFileInTimestampedCache(context, timestampedFileName, "content".byteInputStream())

        // Create image cache directory
        val imageCache = streamFileManager.getImageCache(context)
        imageCache.mkdirs()
        File(imageCache, "image.jpg").writeText("image")

        // Verify files exist
        val streamCacheDir = File(context.cacheDir, "stream_cache")
        assertTrue(streamCacheDir.exists())
        assertTrue(imageCache.exists())

        // Clear all caches
        val result = streamFileManager.clearAllCache(context)

        assertTrue(result is Result.Success)
        assertFalse(streamCacheDir.exists())
        assertFalse(imageCache.exists())
    }

    @Test
    fun `clearAllCache should not affect other cache directories`() {
        // Create a non-Stream cache directory
        val otherCacheDir = File(context.cacheDir, "other_cache")
        otherCacheDir.mkdirs()
        File(otherCacheDir, "file.txt").writeText("other content")

        // Clear all Stream caches
        streamFileManager.clearAllCache(context)

        // Verify other cache directory is not affected
        assertTrue(otherCacheDir.exists())
        assertTrue(File(otherCacheDir, "file.txt").exists())

        // Cleanup
        otherCacheDir.deleteRecursively()
    }

    @Test
    fun `createFileInCache should create parent directory if it doesn't exist`() {
        // Ensure cache directory doesn't exist
        streamFileManager.clearCache(context)

        val fileName = "new_${randomString()}.txt"
        val result = streamFileManager.createFileInCache(context, fileName)

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        assertTrue(file.parentFile?.exists() == true)
    }

    @Test
    fun `writeFileInCache should close input stream after writing`() = runTest {
        val fileName = "stream_test_${randomString()}.txt"
        val content = "Test content"
        val inputStream = content.byteInputStream()

        streamFileManager.writeFileInCache(context, fileName, inputStream)

        // InputStream should be closed (available should return 0)
        assertEquals(0, inputStream.available())
    }

    @Test
    fun `writeFileInTimestampedCache should close input stream after writing`() {
        val fileName = "stream_test_${randomString()}.txt"
        val content = "Test content"
        val inputStream = content.byteInputStream()

        streamFileManager.writeFileInTimestampedCache(context, fileName, inputStream)

        // InputStream should be closed (available should return 0)
        assertEquals(0, inputStream.available())
    }
}
