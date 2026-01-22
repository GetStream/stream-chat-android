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
import org.junit.Assert.assertNotNull
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
    fun `clearCache should handle already empty cache gracefully`() = runTest {
        // Ensure cache is clear
        streamFileManager.clearCache(context)

        // Attempt to clear again
        val result = streamFileManager.clearCache(context)

        assertTrue(result is Result.Success)
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

    @Test
    fun `createPhotoInExternalDir should create file reference in external pictures directory`() {
        val result = streamFileManager.createPhotoInExternalDir(context)

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        assertTrue(file.name.startsWith("STREAM_IMG_"))
        assertTrue(file.name.endsWith(".jpg"))
        assertTrue(file.parentFile?.name == "Pictures")
    }

    @Test
    fun `createPhotoInExternalDir should create unique filenames for each call`() {
        val result1 = streamFileManager.createPhotoInExternalDir(context)
        Thread.sleep(1000) // Wait 1 second to ensure different timestamp
        val result2 = streamFileManager.createPhotoInExternalDir(context)

        assertTrue(result1 is Result.Success)
        assertTrue(result2 is Result.Success)
        val file1 = (result1 as Result.Success).value
        val file2 = (result2 as Result.Success).value
        assertNotEquals(file1.name, file2.name)
    }

    @Test
    fun `createPhotoInExternalDir should return file reference without creating physical file`() {
        val result = streamFileManager.createPhotoInExternalDir(context)

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        assertFalse(file.exists()) // File reference exists but physical file not created
    }

    @Test
    fun `createVideoInExternalDir should create file reference in external movies directory`() {
        val result = streamFileManager.createVideoInExternalDir(context)

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        assertTrue(file.name.startsWith("STREAM_VID_"))
        assertTrue(file.name.endsWith(".mp4"))
        assertTrue(file.parentFile?.name == "Movies")
    }

    @Test
    fun `createVideoInExternalDir should create unique filenames for each call`() {
        val result1 = streamFileManager.createVideoInExternalDir(context)
        Thread.sleep(1000) // Wait 1 second to ensure different timestamp
        val result2 = streamFileManager.createVideoInExternalDir(context)

        assertTrue(result1 is Result.Success)
        assertTrue(result2 is Result.Success)
        val file1 = (result1 as Result.Success).value
        val file2 = (result2 as Result.Success).value
        assertNotEquals(file1.name, file2.name)
    }

    @Test
    fun `createVideoInExternalDir should return file reference without creating physical file`() {
        val result = streamFileManager.createVideoInExternalDir(context)

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        assertFalse(file.exists()) // File reference exists but physical file not created
    }

    @Test
    fun `createPhotoInExternalDir filename should include timestamp`() {
        val result = streamFileManager.createPhotoInExternalDir(context)

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        // Filename format: STREAM_IMG_yyyyMMdd_HHmmss.jpg
        // Should match pattern: STREAM_IMG_20XXXXXX_XXXXXX.jpg
        assertTrue(file.name.matches(Regex("STREAM_IMG_\\d{8}_\\d{6}\\.jpg")))
    }

    @Test
    fun `createVideoInExternalDir filename should include timestamp`() {
        val result = streamFileManager.createVideoInExternalDir(context)

        assertTrue(result is Result.Success)
        val file = (result as Result.Success).value
        // Filename format: STREAM_VID_yyyyMMdd_HHmmss.mp4
        // Should match pattern: STREAM_VID_20XXXXXX_XXXXXX.mp4
        assertTrue(file.name.matches(Regex("STREAM_VID_\\d{8}_\\d{6}\\.mp4")))
    }

    @Test
    fun `clearExternalStorage should delete both photos and videos`() {
        // Create photo and video files
        val photo = streamFileManager.createPhotoInExternalDir(context)
        val video = streamFileManager.createVideoInExternalDir(context)

        assertTrue(photo is Result.Success)
        assertTrue(video is Result.Success)

        val photoFile = (photo as Result.Success).value
        val videoFile = (video as Result.Success).value

        // Write content to the files
        photoFile.writeText("photo")
        videoFile.writeText("video")

        assertTrue(photoFile.exists())
        assertTrue(videoFile.exists())

        // Clear external storage
        val clearResult = streamFileManager.clearExternalStorage(context)

        assertTrue(clearResult is Result.Success)
        assertFalse(photoFile.exists())
        assertFalse(videoFile.exists())
    }

    @Test
    fun `clearExternalStorage should not delete non-Stream files`() {
        // Create a Stream photo file
        val streamPhoto = streamFileManager.createPhotoInExternalDir(context)
        assertTrue(streamPhoto is Result.Success)
        val streamPhotoFile = (streamPhoto as Result.Success).value
        streamPhotoFile.writeText("stream photo")

        // Create a non-Stream file in the same directory
        val otherFile = File(streamPhotoFile.parentFile, "other_photo.jpg")
        otherFile.writeText("other photo")

        assertTrue(streamPhotoFile.exists())
        assertTrue(otherFile.exists())

        // Clear external storage
        val clearResult = streamFileManager.clearExternalStorage(context)

        assertTrue(clearResult is Result.Success)
        assertFalse(streamPhotoFile.exists())
        assertTrue(otherFile.exists()) // Non-Stream file should remain

        // Cleanup
        otherFile.delete()
    }

    @Test
    fun `clearExternalStorage should not delete directories, only files`() {
        // Create a Stream photo file
        val photo = streamFileManager.createPhotoInExternalDir(context)
        assertTrue(photo is Result.Success)
        val photoFile = (photo as Result.Success).value
        photoFile.writeText("photo")

        val picturesDir = photoFile.parentFile
        assertTrue(picturesDir?.exists() == true)

        // Clear external storage
        streamFileManager.clearExternalStorage(context)

        // Directory should still exist
        assertTrue(picturesDir?.exists() == true)
        // But the file should be gone
        assertFalse(photoFile.exists())
    }

    @Test
    fun `clearExternalStorage should only delete photos matching exact pattern`() {
        // Create a valid Stream photo file
        val validPhoto = streamFileManager.createPhotoInExternalDir(context)
        assertTrue(validPhoto is Result.Success)
        val validPhotoFile = (validPhoto as Result.Success).value
        validPhotoFile.writeText("valid photo")

        val picturesDir = validPhotoFile.parentFile
        assertNotNull(picturesDir)

        // Create files with similar but incorrect patterns that should NOT be deleted
        val invalidFiles = listOf(
            // Missing underscore separator
            File(picturesDir, "STREAM_IMG20260122_143052.jpg"),
            // Wrong date format (missing digits)
            File(picturesDir, "STREAM_IMG_2026122_143052.jpg"),
            // Wrong time format (extra digits)
            File(picturesDir, "STREAM_IMG_20260122_1430520.jpg"),
            // Missing timestamp entirely
            File(picturesDir, "STREAM_IMG_.jpg"),
            // Just prefix match
            File(picturesDir, "STREAM_IMG_backup.jpg"),
            // Prefix with different suffix
            File(picturesDir, "STREAM_IMG_notes_20260122.jpg"),
            // Wrong extension
            File(picturesDir, "STREAM_IMG_20260122_143052.png"),
            // Extra text after valid pattern
            File(picturesDir, "STREAM_IMG_20260122_143052.jpg.bak"),
        )

        // Write content to all invalid files
        invalidFiles.forEach { it.writeText("should not be deleted") }

        // Verify all files exist before clearing
        assertTrue(validPhotoFile.exists())
        invalidFiles.forEach { assertTrue(it.exists()) }

        // Clear external storage
        val result = streamFileManager.clearExternalStorage(context)

        // Verify clear succeeded
        assertTrue(result is Result.Success)

        // Valid file should be deleted
        assertFalse(validPhotoFile.exists())

        // All invalid pattern files should still exist
        invalidFiles.forEach { file ->
            assertTrue("File ${file.name} should not be deleted", file.exists())
        }

        // Cleanup
        invalidFiles.forEach { it.delete() }
    }

    @Test
    fun `clearExternalStorage should only delete videos matching exact pattern`() {
        // Create a valid Stream video file
        val validVideo = streamFileManager.createVideoInExternalDir(context)
        assertTrue(validVideo is Result.Success)
        val validVideoFile = (validVideo as Result.Success).value
        validVideoFile.writeText("valid video")

        val moviesDir = validVideoFile.parentFile
        assertNotNull(moviesDir)

        // Create videos with invalid patterns
        val invalidVideos = listOf(
            File(moviesDir, "STREAM_VID_backup.mp4"),
            File(moviesDir, "STREAM_VID_2026.mp4"),
            File(moviesDir, "STREAM_VID_20260122.mp4"), // Missing time
            File(moviesDir, "STREAM_VID_143052.mp4"), // Missing date
            File(moviesDir, "STREAM_VIDX_20260122_143052.mp4"), // Extra character in prefix
        )

        // Write content to all invalid files
        invalidVideos.forEach { it.writeText("should not be deleted") }

        // Verify all files exist
        assertTrue(validVideoFile.exists())
        invalidVideos.forEach { assertTrue(it.exists()) }

        // Clear external storage
        val result = streamFileManager.clearExternalStorage(context)

        assertTrue(result is Result.Success)

        // Valid file should be deleted
        assertFalse(validVideoFile.exists())

        // All invalid pattern files should still exist
        invalidVideos.forEach { file ->
            assertTrue("File ${file.name} should not be deleted", file.exists())
        }

        // Cleanup
        invalidVideos.forEach { it.delete() }
    }

    @Test
    fun `clearExternalStorage should succeed when directories are empty`() {
        val result = streamFileManager.clearExternalStorage(context)

        assertTrue(result is Result.Success)
    }
}
