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

package io.getstream.chat.android.compose.handlers

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class LoadMoreHandlerTest {

    @Test
    fun `should not load more when list is empty`() {
        val result = shouldLoadMore(
            totalItemsCount = 0,
            lastVisibleItemIndex = -1,
            loadMoreThreshold = 3,
        )
        assertFalse(result)
    }

    @Test
    fun `should not load more when list is smaller than threshold`() {
        val result = shouldLoadMore(
            totalItemsCount = 2,
            lastVisibleItemIndex = 1,
            loadMoreThreshold = 3,
        )
        assertFalse(result)
    }

    @Test
    fun `should not load more when last visible item is not near end`() {
        val result = shouldLoadMore(
            totalItemsCount = 10,
            lastVisibleItemIndex = 5,
            loadMoreThreshold = 3,
        )
        assertFalse(result)
    }

    @Test
    fun `should not load more when exactly at threshold boundary`() {
        val result = shouldLoadMore(
            totalItemsCount = 5,
            lastVisibleItemIndex = 1,
            loadMoreThreshold = 3,
        )
        assertFalse(result)
    }

    @Test
    fun `should load more when last visible item is within threshold from end`() {
        val result = shouldLoadMore(
            totalItemsCount = 10,
            lastVisibleItemIndex = 7,
            loadMoreThreshold = 3,
        )
        assertTrue(result)
    }
}
