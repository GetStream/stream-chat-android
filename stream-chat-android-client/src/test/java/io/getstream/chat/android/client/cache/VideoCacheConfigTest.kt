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

package io.getstream.chat.android.client.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

internal class VideoCacheConfigTest {

    @Test
    fun `default constructor uses the documented default cap`() {
        assertEquals(VideoCacheConfig.DEFAULT_MAX_SIZE_BYTES, VideoCacheConfig().maxSizeBytes)
    }

    @Test
    fun `accepts positive maxSizeBytes`() {
        assertEquals(1L, VideoCacheConfig(maxSizeBytes = 1L).maxSizeBytes)
    }

    @Test
    fun `rejects zero maxSizeBytes`() {
        assertThrows(IllegalArgumentException::class.java) {
            VideoCacheConfig(maxSizeBytes = 0L)
        }
    }

    @Test
    fun `rejects negative maxSizeBytes`() {
        assertThrows(IllegalArgumentException::class.java) {
            VideoCacheConfig(maxSizeBytes = -1L)
        }
    }
}
