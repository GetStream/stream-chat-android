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
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(InternalStreamChatApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class StringExtensionsTest {

    @Test
    fun `given resizing disabled the original url is returned unchanged`() {
        val originalUrl = createStreamCdnImageLink(originalWidth = 1000, originalHeight = 500)

        val result = originalUrl.applyStreamCdnImageResizingIfEnabled(
            StreamCdnImageResizing.defaultStreamCdnImageResizing(),
        )

        result shouldBeEqualTo originalUrl
    }

    @Test
    fun `given resizing enabled the width and height percentages map to the correct dimensions`() {
        val originalWidth = 1000
        val originalHeight = 500
        val widthPercentage = 0.5f
        val heightPercentage = 0.2f
        val originalUrl = createStreamCdnImageLink(originalWidth, originalHeight)

        val resizedUri = originalUrl.applyStreamCdnImageResizingIfEnabled(
            StreamCdnImageResizing.defaultStreamCdnImageResizing().copy(
                imageResizingEnabled = true,
                resizedWidthPercentage = widthPercentage,
                resizedHeightPercentage = heightPercentage,
            ),
        ).toUri()

        // Regression guard: the width percentage must drive the width param (w) and the
        // height percentage the height param (h) — a swap between them is a bug.
        resizedUri.getQueryParameter("w") shouldBeEqualTo (originalWidth * widthPercentage).toInt().toString()
        resizedUri.getQueryParameter("h") shouldBeEqualTo (originalHeight * heightPercentage).toInt().toString()
    }

    private companion object {
        fun createStreamCdnImageLink(originalWidth: Int, originalHeight: Int) =
            "https://us-east.stream-io-cdn.com/1/images/IMAGE_NAME.jpg" +
                "?Key-Pair-Id=SODHGWNRLG&Policy=akIjUneI9Kmbds2&oh=$originalHeight&ow=$originalWidth"
    }
}
