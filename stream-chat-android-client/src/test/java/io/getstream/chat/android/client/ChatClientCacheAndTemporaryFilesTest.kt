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

package io.getstream.chat.android.client

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.internal.file.StreamFileManager
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

/**
 * Test class for [ChatClient.clearCacheAndTemporaryFiles] method.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ChatClientCacheAndTemporaryFilesTest {

    private companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var context: Context
    private lateinit var chatClient: ChatClient
    private lateinit var streamFileManager: StreamFileManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        streamFileManager = StreamFileManager()

        // Build a minimal ChatClient for testing
        chatClient = ChatClient.Builder("test-api-key", context)
            .build()
    }

    @After
    fun tearDown() {
        // Clean up all cache and external storage after each test
        streamFileManager.clearAllCache(context)
        streamFileManager.clearExternalStorage(context)
    }

    @Test
    fun `clearCacheAndTemporaryFiles should clear all cache and external storage together`() = runTest {
        // Create files in stream cache
        streamFileManager.writeFileInCache(context, "cache_${randomString()}.txt", "content".byteInputStream())

        // Create files in image cache
        val imageCache = streamFileManager.getImageCache(context)
        imageCache.mkdirs()
        File(imageCache, "image.jpg").writeText("image")

        // Create files in timestamped cache
        streamFileManager.writeFileInTimestampedCache(context, "timestamped_${randomString()}.txt", "content".byteInputStream())

        // Create photo in external storage
        val photoResult = streamFileManager.createPhotoInExternalDir(context)
        assertTrue(photoResult is Result.Success)
        val photoFile = (photoResult as Result.Success).value
        photoFile.writeText("photo")

        // Create video in external storage
        val videoResult = streamFileManager.createVideoInExternalDir(context)
        assertTrue(videoResult is Result.Success)
        val videoFile = (videoResult as Result.Success).value
        videoFile.writeText("video")

        // Verify all exist
        val streamCacheDir = File(context.cacheDir, "stream_cache")
        assertTrue(streamCacheDir.exists())
        assertTrue(imageCache.exists())
        assertTrue(photoFile.exists())
        assertTrue(videoFile.exists())

        // Clear cache and temporary files
        val result = chatClient.clearCacheAndTemporaryFiles(context).execute()

        // Verify all were cleared
        assertTrue(result is Result.Success)
        assertFalse(streamCacheDir.exists())
        assertFalse(imageCache.exists())
        assertFalse(photoFile.exists())
        assertFalse(videoFile.exists())
    }

    @Test
    fun `clearCacheAndTemporaryFiles should not affect other cache directories`() = runTest {
        // Create Stream cache
        streamFileManager.writeFileInCache(context, "stream_${randomString()}.txt", "content".byteInputStream())

        // Create non-Stream cache directory
        val otherCacheDir = File(context.cacheDir, "other_cache")
        otherCacheDir.mkdirs()
        val otherFile = File(otherCacheDir, "other_file.txt")
        otherFile.writeText("other content")

        assertTrue(otherCacheDir.exists())
        assertTrue(otherFile.exists())

        // Clear cache and temporary files
        val result = chatClient.clearCacheAndTemporaryFiles(context).execute()

        // Verify Stream cache was cleared but other cache was not affected
        assertTrue(result is Result.Success)
        assertTrue(otherCacheDir.exists())
        assertTrue(otherFile.exists())

        // Cleanup
        otherCacheDir.deleteRecursively()
    }

    @Test
    fun `clearCacheAndTemporaryFiles should not affect non-Stream external files`() {
        // Create Stream photo
        val streamPhotoResult = streamFileManager.createPhotoInExternalDir(context)
        assertTrue(streamPhotoResult is Result.Success)
        val streamPhotoFile = (streamPhotoResult as Result.Success).value
        streamPhotoFile.writeText("stream photo")

        // Create non-Stream file in the same directory
        val otherFile = File(streamPhotoFile.parentFile, "other_photo.jpg")
        otherFile.writeText("other photo")

        assertTrue(streamPhotoFile.exists())
        assertTrue(otherFile.exists())

        // Clear cache and temporary files
        val result = chatClient.clearCacheAndTemporaryFiles(context).execute()

        // Verify Stream file was cleared but other file was not affected
        assertTrue(result is Result.Success)
        assertFalse(streamPhotoFile.exists())
        assertTrue(otherFile.exists())

        // Cleanup
        otherFile.delete()
    }
}
