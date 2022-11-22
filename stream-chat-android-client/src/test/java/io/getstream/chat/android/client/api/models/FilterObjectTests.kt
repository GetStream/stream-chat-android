/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.models.Filters
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.Test

internal class FilterObjectTests {

    @Test
    fun `Two filters with different arguments Should not be equal`() {
        val filterObject1 = Filters.`in`("members", listOf("userId1", "userId2"))
        val filterObject2 = Filters.`in`("members", listOf("userId1", "userId3"))

        filterObject1 `should not be equal to` filterObject2
    }

    @Test
    fun `Two filters with different types Should not be equal`() {
        val filterObject1 = Filters.`in`("members", listOf("userId1"))
        val filterObject2 = Filters.nin("members", listOf("userId1"))

        filterObject1 `should not be equal to` filterObject2
    }

    @Test
    fun `Two different filter objects Should be equal`() {
        val filterObject1 = Filters.`in`("members", listOf("userId1", "userId2"))
        val filterObject2 = Filters.`in`("members", listOf("userId1", "userId2"))

        filterObject1 `should be equal to` filterObject2
    }
}
