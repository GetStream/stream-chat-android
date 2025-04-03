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

package io.getstream.chat.android.client.query.pagination

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

internal class AnyChannelPaginationRequestTest {

    @Test
    fun `isFirstPage should return true when channelOffset is 0`() {
        val request = AnyChannelPaginationRequest().apply {
            channelOffset = 0
        }
        request.isFirstPage() shouldBeEqualTo true
    }

    @Test
    fun `isFirstPage should return false when channelOffset is not 0`() {
        val request = AnyChannelPaginationRequest().apply {
            channelOffset = 1
        }
        request.isFirstPage() shouldBeEqualTo false
    }

    @Test
    fun `isRequestingMoreThanLastMessage should return true when first page and messageLimit greater than 1`() {
        val request = AnyChannelPaginationRequest().apply {
            channelOffset = 0
            messageLimit = 2
        }
        request.isRequestingMoreThanLastMessage() shouldBeEqualTo true
    }

    @Test
    fun `isRequestingMoreThanLastMessage should return true when not first page and messageLimit greater than 0`() {
        val request = AnyChannelPaginationRequest().apply {
            channelOffset = 1
            messageLimit = 1
        }
        request.isRequestingMoreThanLastMessage() shouldBeEqualTo true
    }

    @Test
    fun `isRequestingMoreThanLastMessage should return false when first page and messageLimit is 1`() {
        val request = AnyChannelPaginationRequest().apply {
            channelOffset = 0
            messageLimit = 1
        }
        request.isRequestingMoreThanLastMessage() shouldBeEqualTo false
    }

    @Test
    fun `isRequestingMoreThanLastMessage should return false when not first page and messageLimit is 0`() {
        val request = AnyChannelPaginationRequest().apply {
            channelOffset = 1
            messageLimit = 0
        }
        request.isRequestingMoreThanLastMessage() shouldBeEqualTo false
    }

    @Test
    fun `isNotFirstPage should return true when channelOffset is not 0`() {
        val request = AnyChannelPaginationRequest().apply {
            channelOffset = 1
        }
        request.isNotFirstPage() shouldBeEqualTo true
    }

    @Test
    fun `isNotFirstPage should return false when channelOffset is 0`() {
        val request = AnyChannelPaginationRequest().apply {
            channelOffset = 0
        }
        request.isNotFirstPage() shouldBeEqualTo false
    }
}
