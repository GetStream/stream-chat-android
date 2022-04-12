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

package io.getstream.chat.android.offline.repository.database.converter

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QuerySort.Companion.ascByName
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.repository.database.converter.internal.QuerySortConverter
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class QuerySortConverterTests {

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Should store and extract the same object`(sort: QuerySort<Channel>) {
        val sut = QuerySortConverter()
        val string = sut.objectToString(sort)
        val output = sut.stringToObject(string)

        output shouldBeEqualTo sort
    }

    companion object {
        @JvmStatic
        fun arguments() = listOf<QuerySort<Channel>>(
            QuerySort.asc(Channel::memberCount),
            QuerySort.desc("member_count"),
            QuerySort.desc(Channel::lastMessageAt).ascByName("created_at"),
            QuerySort()
        )
    }
}
