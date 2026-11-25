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

package io.getstream.chat.android.client.internal.state.model.querychannels.pagination.internal

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class QueryChannelPaginationRequestTest {

    @Test
    fun `the first page is watched with the scheduled messages of the channel`() {
        val request = QueryChannelPaginationRequest().toWatchChannelRequest(userPresence = randomBoolean())

        request.includeScheduledMessages shouldBeEqualTo true
    }

    @Test
    fun `a page of older messages is watched with the scheduled messages of the channel`() {
        val request = QueryChannelPaginationRequest().apply {
            messageFilterDirection = Pagination.LESS_THAN
            messageFilterValue = randomString()
        }.toWatchChannelRequest(userPresence = randomBoolean())

        request.includeScheduledMessages shouldBeEqualTo true
    }
}
