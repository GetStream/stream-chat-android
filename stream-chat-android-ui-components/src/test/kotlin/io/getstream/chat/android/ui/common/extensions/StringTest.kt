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

package io.getstream.chat.android.ui.common.extensions

import io.getstream.chat.android.ui.utils.extensions.getOccurrenceRanges
import org.junit.jupiter.api.Test

internal class StringTest {

    @Test
    fun `getOccurrenceRanges works with invalid regex expressions`() {
        "test".getOccurrenceRanges(listOf("Ryan :)"))
        assert(true)
    }

    @Test
    fun `getOccurrenceRanges returns no position`() {
        val result = "test".getOccurrenceRanges(listOf("Ryan"))
        assert(result.isEmpty())
    }

    @Test
    fun `getOccurrenceRanges returns correct position`() {
        val result = "Ryan".getOccurrenceRanges(listOf("Ryan"))
        assert(result.size == 1)
        assert(result[0].first == 0)
        assert(result[0].last == 3)
    }

    @Test
    fun `getOccurrenceRanges returns correct positions`() {
        val result = "Ryan Ryan".getOccurrenceRanges(listOf("Ryan"))
        assert(result.size == 2)
        assert(result[0].first == 0)
        assert(result[0].last == 3)
        assert(result[1].first == 5)
        assert(result[1].last == 8)
    }

    @Test
    fun `getOccurrenceRanges returns no position if empty list`() {
        val result = "Ryan".getOccurrenceRanges(emptyList())
        assert(result.isEmpty())
    }

    @Test
    fun `getOccurrenceRanges returns no position if empty text`() {
        val result = "".getOccurrenceRanges(emptyList())
        assert(result.isEmpty())
    }
}
