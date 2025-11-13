/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.images

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.video.VideoFrameDecoder
import okio.Path.Companion.toOkioPath
import org.amshove.kluent.internal.assertEquals
import org.amshove.kluent.internal.assertFalse
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class StreamImageLoaderFactoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `newImageLoader configures memory cache correctly`() {
        val sut = StreamImageLoaderFactory()

        val imageLoader = sut.newImageLoader(context)

        val memoryCache = imageLoader.memoryCache
        assertNotNull(memoryCache)
        val expected = MemoryCache.Builder().maxSizePercent(context, percent = 0.25).build()
        assertEquals(expected.size, memoryCache!!.size)
        assertEquals(expected.maxSize, memoryCache.maxSize)
        assertEquals(expected.keys, memoryCache.keys)
    }

    @Test
    fun `newImageLoader configures disk cache correctly`() {
        val sut = StreamImageLoaderFactory()

        val imageLoader = sut.newImageLoader(context)

        val diskCache = imageLoader.diskCache
        assertNotNull(diskCache)
        val expected = DiskCache.Builder()
            .directory(context.cacheDir.resolve("stream_coil3_image_cache").toOkioPath())
            .maxSizePercent(percent = 0.02)
            .build()
        assertEquals(expected.maxSize, diskCache!!.maxSize)
        assertEquals(expected.size, diskCache.size)
        assertEquals(expected.directory, diskCache.directory)
        assertEquals(expected.fileSystem, diskCache.fileSystem)
    }

    @Test
    @Config(sdk = [27])
    fun `newImageLoader configures image decoders for SDKs prior 28`() {
        val sut = StreamImageLoaderFactory()

        val imageLoader = sut.newImageLoader(context = ApplicationProvider.getApplicationContext())

        val decoderFactories = imageLoader.components.decoderFactories
        val hasAnimatedDecoder = decoderFactories.filterIsInstance<AnimatedImageDecoder.Factory>().isNotEmpty()
        val hasGifDecoder = decoderFactories.filterIsInstance<GifDecoder.Factory>().isNotEmpty()
        val hasVideoDecoder = decoderFactories.filterIsInstance<VideoFrameDecoder.Factory>().isNotEmpty()
        assertFalse(hasAnimatedDecoder)
        assertTrue(hasGifDecoder)
        assertTrue(hasVideoDecoder)
    }

    @Test
    @Config(sdk = [28, 29])
    fun `newImageLoader configures image decoders for SDKs greater than or equal to 28`() {
        val sut = StreamImageLoaderFactory()

        val imageLoader = sut.newImageLoader(ApplicationProvider.getApplicationContext())

        val decoderFactories = imageLoader.components.decoderFactories
        val hasAnimatedDecoder = decoderFactories.filterIsInstance<AnimatedImageDecoder.Factory>().isNotEmpty()
        val hasGifDecoder = decoderFactories.filterIsInstance<GifDecoder.Factory>().isNotEmpty()
        val hasVideoDecoder = decoderFactories.filterIsInstance<VideoFrameDecoder.Factory>().isNotEmpty()
        assertTrue(hasAnimatedDecoder)
        assertFalse(hasGifDecoder)
        assertTrue(hasVideoDecoder)
    }

    @Test
    fun `newImageLoader applies custom builder lambda`() {
        var customApplied = false
        val sut = StreamImageLoaderFactory { customApplied = true }

        sut.newImageLoader(context)

        assertTrue(customApplied)
    }
}
