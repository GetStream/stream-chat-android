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

import io.getstream.chat.android.client.api.models.querysort.QuerySortByReflection
import io.getstream.chat.android.client.api.models.querysort.QuerySortByReflection.Companion.ascByName
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class QuerySortTest {

    /**
     * [generateQuerySortInput]
     */
    @ParameterizedTest
    @MethodSource("generateQuerySortInput")
    fun `Two QuerySort with the same content should produce the same hashcode`(
        a: QuerySortByReflection<Any>,
        b: QuerySortByReflection<Any>,
    ) {
        a.hashCode() `should be equal to` b.hashCode()
    }

    @Test
    fun `Two same query sorts should be equal`() {
        val sort1 = QuerySortByReflection.asc(Channel::memberCount).ascByName("created_at")
        val sort2 = QuerySortByReflection.asc<Channel>("member_count").asc(Channel::createdAt)

        sort1 `should be equal to` sort2
    }

    companion object {

        @JvmStatic
        fun generateQuerySortInput() = listOf(
            randomString().let {
                Arguments.of(
                    QuerySortByReflection.Companion.asc<Channel>(it),
                    QuerySortByReflection.Companion.asc<Channel>(it)
                )
            },
            randomString().let {
                Arguments.of(
                    QuerySortByReflection.Companion.asc<Message>(it),
                    QuerySortByReflection.Companion.asc<Message>(it)
                )
            },
            randomString().let {
                Arguments.of(
                    QuerySortByReflection.Companion.desc<Channel>(it),
                    QuerySortByReflection.Companion.desc<Channel>(it)
                )
            },
            randomString().let {
                Arguments.of(
                    QuerySortByReflection.Companion.desc<Message>(it),
                    QuerySortByReflection.Companion.desc<Message>(it)
                )
            },
        )
    }
}
