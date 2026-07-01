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

package io.getstream.chat.android.client.cache.internal

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.test.core.app.ApplicationProvider
import io.getstream.chat.android.client.cache.VideoCacheConfig
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(UnstableApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
internal class VideoMediaCacheTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val cacheDir: File get() = File(context.cacheDir, SUB_DIR)

    private lateinit var cache: VideoMediaCache

    @Before
    fun setUp() {
        cacheDir.deleteRecursively()
        cache = VideoMediaCache.create(context, cacheDir, VideoCacheConfig())
    }

    @After
    fun tearDown() {
        cache.release()
        cacheDir.deleteRecursively()
    }

    @Test
    fun `creates the cache directory under the provided path`() {
        assertTrue("Expected cache directory to exist at ${cacheDir.absolutePath}", cacheDir.isDirectory)
    }

    @Test
    fun `release is idempotent across multiple calls`() {
        cache.release()
        cache.release()
    }

    @Test
    fun `create returns the same instance for the same directory`() {
        val secondConfig = VideoCacheConfig(maxSizeBytes = VideoCacheConfig.DEFAULT_MAX_SIZE_BYTES / 2)

        val second = VideoMediaCache.create(context, cacheDir, secondConfig)

        assertSame(cache, second)
    }

    @Test
    fun `create returns a fresh instance after release for the same directory`() {
        cache.release()

        val recreated = VideoMediaCache.create(context, cacheDir, VideoCacheConfig())

        assertNotNull(recreated)
        assertTrue("Expected a different instance after release", recreated !== cache)
        // Reassign so @After releases the recreated instance and Media3's SimpleCache
        // unlocks the directory; otherwise the next test's setUp would throw.
        cache = recreated
    }

    private companion object {
        private const val SUB_DIR = "video_cache_test"
    }
}
