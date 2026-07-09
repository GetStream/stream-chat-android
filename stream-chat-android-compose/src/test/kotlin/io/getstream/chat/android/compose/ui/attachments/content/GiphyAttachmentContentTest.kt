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

package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

internal class GiphyAttachmentContentTest {

    @Test
    fun `landscape ratio is constrained by the max width`() {
        val result = calculateResultingDimensions(
            maxWidth = 250.dp,
            maxHeight = 200.dp,
            giphyWidth = 400.dp,
            giphyHeight = 200.dp,
        )

        assertEquals(250f, result.width.value, DELTA)
        assertEquals(125f, result.height.value, DELTA)
    }

    @Test
    fun `portrait ratio is constrained by the max height`() {
        val result = calculateResultingDimensions(
            maxWidth = 250.dp,
            maxHeight = 200.dp,
            giphyWidth = 200.dp,
            giphyHeight = 400.dp,
        )

        assertEquals(100f, result.width.value, DELTA)
        assertEquals(200f, result.height.value, DELTA)
    }

    @Test
    fun `square ratio is constrained by the smaller max dimension`() {
        val result = calculateResultingDimensions(
            maxWidth = 250.dp,
            maxHeight = 200.dp,
            giphyWidth = 300.dp,
            giphyHeight = 300.dp,
        )

        assertEquals(200f, result.width.value, DELTA)
        assertEquals(200f, result.height.value, DELTA)
    }

    private companion object {
        private const val DELTA = 0.01f
    }
}
