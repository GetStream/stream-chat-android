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

package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.randomInt
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID

internal class QueryChannelRequestTests {

    @Test
    fun `ensure 'withMembers' sets 'state' to True`() {
        QueryChannelRequest().withMembers(randomInt(), randomInt()).apply {
            state `should be equal to` true
        }
    }

    @Test
    fun `ensure 'withMessages' sets 'state' to True`() {
        QueryChannelRequest().withMessages(randomInt()).apply {
            state `should be equal to` true
        }
    }

    @ParameterizedTest
    @MethodSource("generatePaginationList")
    fun `ensure paginated 'withMessages' sets 'state' to True`(pagination: Pagination) {
        val messageId = UUID.randomUUID().toString()
        QueryChannelRequest().withMessages(pagination, messageId, randomInt()).apply {
            state `should be equal to` true
        }
    }

    @Test
    fun `ensure 'withWatchers' sets 'state' to True`() {
        QueryChannelRequest().withWatchers(randomInt(), randomInt()).apply {
            state `should be equal to` true
        }
    }

    internal companion object {
        @JvmStatic
        fun generatePaginationList() = Pagination.values().toList()
    }
}
