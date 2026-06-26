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
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.getExtra
import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.size.Size
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class VideoThumbnailFallbackInterceptorTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val interceptor = VideoThumbnailFallbackInterceptor()

    @Test
    fun `loads the thumbnail and skips the video when the thumbnail succeeds`() = runTest {
        val chain = chainFor(
            VideoThumbnailImageData(thumbnailUrl = THUMB_URL, videoUrl = VIDEO_URL),
            resultFor = { url -> if (url == THUMB_URL) success else error },
        )

        val result = interceptor.intercept(chain)

        assertEquals(listOf(THUMB_URL), chain.proceeded)
        assertTrue(result is SuccessResult)
    }

    @Test
    fun `falls back to the video frame when the thumbnail fails`() = runTest {
        val chain = chainFor(
            VideoThumbnailImageData(thumbnailUrl = THUMB_URL, videoUrl = VIDEO_URL),
            resultFor = { url -> if (url == VIDEO_URL) success else error },
        )

        val result = interceptor.intercept(chain)

        assertEquals(listOf(THUMB_URL, VIDEO_URL), chain.proceeded)
        assertTrue(result is SuccessResult)
        // The video fallback request must be marked so VideoFrameFetcher handles it.
        assertTrue(chain.proceededRequests.last().getExtra(videoFramePreviewKey))
    }

    @Test
    fun `loads the video frame directly when there is no thumbnail`() = runTest {
        val chain = chainFor(
            VideoThumbnailImageData(thumbnailUrl = null, videoUrl = VIDEO_URL),
            resultFor = { success },
        )

        val result = interceptor.intercept(chain)

        assertEquals(listOf(VIDEO_URL), chain.proceeded)
        assertTrue(result is SuccessResult)
    }

    @Test
    fun `returns the thumbnail error when there is no video to fall back to`() = runTest {
        val chain = chainFor(
            VideoThumbnailImageData(thumbnailUrl = THUMB_URL, videoUrl = null),
            resultFor = { error },
        )

        val result = interceptor.intercept(chain)

        assertEquals(listOf(THUMB_URL), chain.proceeded)
        assertTrue(result is ErrorResult)
    }

    @Test
    fun `passes through requests that are not video thumbnails`() = runTest {
        val chain = chainFor(THUMB_URL, resultFor = { success })

        interceptor.intercept(chain)

        assertEquals(listOf(THUMB_URL), chain.proceeded)
    }

    private val success: ImageResult get() = mock<SuccessResult>()
    private val error: ImageResult get() = mock<ErrorResult>()

    private fun chainFor(data: Any, resultFor: (String?) -> ImageResult): FakeCoilChain {
        val request = ImageRequest.Builder(context).data(data).build()
        return FakeCoilChain(request, resultFor)
    }

    @Suppress("EmptyFunctionBlock")
    private class FakeCoilChain(
        override val request: ImageRequest,
        private val resultFor: (String?) -> ImageResult,
        val proceeded: MutableList<String?> = mutableListOf(),
        val proceededRequests: MutableList<ImageRequest> = mutableListOf(),
    ) : Interceptor.Chain {
        override val size: Size get() = Size.ORIGINAL

        override suspend fun proceed(): ImageResult {
            val key = request.data.toString()
            proceeded.add(key)
            proceededRequests.add(request)
            return resultFor(key)
        }

        override fun withRequest(request: ImageRequest): Interceptor.Chain =
            FakeCoilChain(request, resultFor, proceeded, proceededRequests)

        override fun withSize(size: Size): Interceptor.Chain = this
    }

    private companion object {
        private const val THUMB_URL = "https://cdn.example.com/thumb.jpg"
        private const val VIDEO_URL = "https://cdn.example.com/video.mp4"
    }
}
