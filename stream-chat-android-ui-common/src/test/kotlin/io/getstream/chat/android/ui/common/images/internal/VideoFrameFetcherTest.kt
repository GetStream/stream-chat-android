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

package io.getstream.chat.android.ui.common.images.internal

import android.content.Context
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.BitmapImage
import coil3.Extras
import coil3.ImageLoader
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import coil3.size.Size
import coil3.toUri
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowMediaMetadataRetriever
import org.robolectric.shadows.util.DataSource

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class VideoFrameFetcherTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val factory = VideoFrameFetcher.Factory()

    @After
    fun tearDown() {
        ShadowMediaMetadataRetriever.reset()
    }

    @Test
    fun `creates a fetcher when the request is marked as a video preview`() {
        val fetcher = factory.create(VIDEO_URL.toUri(), optionsWith(videoPreview = true), mock<ImageLoader>())

        assertNotNull(fetcher)
    }

    @Test
    fun `skips requests that are not marked as a video preview`() {
        val fetcher = factory.create(VIDEO_URL.toUri(), optionsWith(videoPreview = false), mock<ImageLoader>())

        assertNull(fetcher)
    }

    @Test
    fun `extracts a scaled frame when a target size is set`() = runTest {
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        ShadowMediaMetadataRetriever.addScaledFrame(
            DataSource.toDataSource(VIDEO_URL, emptyMap()),
            FRAME_MICROS,
            FRAME_SIZE,
            FRAME_SIZE,
            bitmap,
        )
        val options = Options(context = context, size = Size(FRAME_SIZE, FRAME_SIZE))

        val result = VideoFrameFetcher(VIDEO_URL.toUri(), options).fetch()

        assertTrue(result is ImageFetchResult)
        assertEquals(bitmap, (result as ImageFetchResult).image.let { (it as BitmapImage).bitmap })
    }

    @Test
    fun `extracts an unscaled frame when the size is original`() = runTest {
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        ShadowMediaMetadataRetriever.addFrame(VIDEO_URL, emptyMap(), FRAME_MICROS, bitmap)
        val options = Options(context = context, size = Size.ORIGINAL)

        val result = VideoFrameFetcher(VIDEO_URL.toUri(), options).fetch()

        assertTrue(result is ImageFetchResult)
        assertEquals(bitmap, (result as ImageFetchResult).image.let { (it as BitmapImage).bitmap })
    }

    @Test
    fun `throws when no frame can be extracted`() = runTest {
        val options = Options(context = context, size = Size.ORIGINAL)

        var thrown = false
        try {
            VideoFrameFetcher(VIDEO_URL.toUri(), options).fetch()
        } catch (_: IllegalStateException) {
            thrown = true
        }

        assertTrue(thrown)
    }

    private fun optionsWith(videoPreview: Boolean): Options {
        val extras = Extras.Builder().set(videoFramePreviewKey, videoPreview).build()
        return Options(context = context, extras = extras)
    }

    private companion object {
        private const val VIDEO_URL = "https://cdn.example.com/video.mp4"
        private const val FRAME_MICROS = 100_000L
        private const val FRAME_SIZE = 100
    }
}
