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

package io.getstream.chat.android.ui.common.images.resizing

import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Suppress("DEPRECATION")
@OptIn(InternalStreamChatApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class StreamCdnImageResizerTest {

    @Test
    fun `given legacy resizing enabled the percentages win over the resizer`() {
        val originalWidth = 1000
        val originalHeight = 500
        val widthPercentage = 0.5f
        val heightPercentage = 0.2f
        val originalUrl = createStreamCdnImageLink(originalWidth, originalHeight)

        val resizedUri = originalUrl.applyStreamCdnImageResizing(
            streamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing().copy(
                imageResizingEnabled = true,
                resizedWidthPercentage = widthPercentage,
                resizedHeightPercentage = heightPercentage,
            ),
            // A resizer that would produce a different result, to prove the legacy config wins.
            streamCdnImageResizer = StreamCdnMaxPixelsImageResizer(maxImagePixels = 1L),
        ).toUri()

        resizedUri.getQueryParameter("w") shouldBeEqualTo (originalWidth * widthPercentage).toInt().toString()
        resizedUri.getQueryParameter("h") shouldBeEqualTo (originalHeight * heightPercentage).toInt().toString()
    }

    @Test
    fun `given legacy resizing disabled the max pixels resizer caps an over-budget image`() {
        val originalUrl = createStreamCdnImageLink(originalWidth = 4000, originalHeight = 2000)
        val maxImagePixels = 2_000_000L

        val resizedUri = originalUrl.applyStreamCdnImageResizing(
            streamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing(),
            streamCdnImageResizer = StreamCdnMaxPixelsImageResizer(maxImagePixels = maxImagePixels),
        ).toUri()

        val resizedWidth = resizedUri.getQueryParameter("w")!!.toInt()
        val resizedHeight = resizedUri.getQueryParameter("h")!!.toInt()
        val resizedPixels = resizedWidth.toLong() * resizedHeight.toLong()

        val delta = Math.abs(resizedPixels - maxImagePixels).toDouble() / maxImagePixels
        (delta <= 0.01) shouldBeEqualTo true
    }

    @Test
    fun `given legacy resizing disabled the no-op resizer returns the url unchanged`() {
        val originalUrl = createStreamCdnImageLink(originalWidth = 4000, originalHeight = 2000)

        val result = originalUrl.applyStreamCdnImageResizing(
            streamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing(),
            streamCdnImageResizer = NoOpStreamCdnImageResizer,
        )

        result shouldBeEqualTo originalUrl
    }

    @Test
    fun `given a non-positive max pixel budget the resizer construction fails`() {
        invoking {
            StreamCdnMaxPixelsImageResizer(maxImagePixels = 0L)
        } shouldThrow IllegalArgumentException::class
        invoking {
            StreamCdnMaxPixelsImageResizer(maxImagePixels = -1L)
        } shouldThrow IllegalArgumentException::class
    }

    private companion object {
        fun createStreamCdnImageLink(originalWidth: Int, originalHeight: Int) =
            "https://us-east.stream-io-cdn.com/1/images/IMAGE_NAME.jpg" +
                "?Key-Pair-Id=SODHGWNRLG&Policy=akIjUneI9Kmbds2&oh=$originalHeight&ow=$originalWidth"
    }
}
