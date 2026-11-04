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

package io.getstream.chat.android.ai.a2ui.compose.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class UrlImageLoaderTest {

    @Test
    fun `an image keeps both sides at least the target`() {
        assertEquals(4, UrlImageLoader.sampleSize(width = 4000, height = 3000, targetSizePx = 500))
        assertEquals(1, UrlImageLoader.sampleSize(width = 400, height = 300, targetSizePx = 500))
    }

    @Test
    fun `a panorama is sampled until it fits the pixel cap`() {
        val width = 30_000
        val height = 1_100

        val sampleSize = UrlImageLoader.sampleSize(width, height, targetSizePx = 1_100)

        assertEquals(4, sampleSize)
        val pixels = (width / sampleSize).toLong() * (height / sampleSize)
        assertTrue("$pixels pixels", pixels <= UrlImageLoader.MAX_DECODED_PIXELS)
        // One step less would be over the cap.
        assertTrue((width / (sampleSize / 2)).toLong() * (height / (sampleSize / 2)) > UrlImageLoader.MAX_DECODED_PIXELS)
    }
}
