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

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.ascByName
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.randomString
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class QuerySortByFieldTest {

    /**
     * [generateQuerySortByFieldInput]
     */
    @ParameterizedTest
    @MethodSource("generateQuerySortByFieldInput")
    fun `Two QuerySorter with the same content should produce the same hashcode`(
        a: QuerySorter<Any>,
        b: QuerySorter<Any>,
    ) {
        a.hashCode() `should be equal to` b.hashCode()
    }

    @Test
    fun `Two same query sorts should be equal`() {
        val sort1 = QuerySortByField.ascByName<Channel>("memberCount").ascByName("createdAt")
        val sort2 = QuerySortByField.ascByName<Channel>("memberCount").ascByName("createdAt")

        sort1 `should be equal to` sort2
    }

    companion object {

        @JvmStatic
        fun generateQuerySortByFieldInput() = listOf(
            randomString().let {
                Arguments.of(
                    QuerySortByField.ascByName<Channel>(it),
                    QuerySortByField.ascByName<Channel>(it),
                )
            },
            randomString().let {
                Arguments.of(
                    QuerySortByField.ascByName<Message>(it),
                    QuerySortByField.ascByName<Message>(it),
                )
            },
            randomString().let {
                Arguments.of(
                    QuerySortByField.descByName<Channel>(it),
                    QuerySortByField.descByName<Channel>(it),
                )
            },
            randomString().let {
                Arguments.of(
                    QuerySortByField.descByName<Message>(it),
                    QuerySortByField.descByName<Message>(it),
                )
            },
        )
    }
}
