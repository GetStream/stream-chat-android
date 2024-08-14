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

package io.getstream.chat.android.offline

import io.getstream.chat.android.client.extensions.internal.applyPagination
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.test.utils.calendar
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.ascByName
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.descByName
import io.getstream.chat.android.randomChannel
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PaginationTest {

    /** [createPaginationInput] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.offline.PaginationTest#createPaginationInput")
    internal fun `Should return a list of channelEntityPairs properly sorted by pagination param`(
        inputList: List<Channel>,
        pagination: AnyChannelPaginationRequest,
        expectedList: List<Channel>,
    ) {
        // show an easy to use diff between the two results
        inputList.applyPagination(pagination) `should be equal to` expectedList
    }

    companion object {
        @JvmStatic
        fun createPaginationInput() = listOf(
            Arguments.of(listOf<Channel>(), AnyChannelPaginationRequest(), listOf<Channel>()),
            listOf(randomChannel()).let { Arguments.of(it, AnyChannelPaginationRequest(), it) },
            listOf(randomChannel(type = "a"), randomChannel(type = "b")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest(),
                    it,
                )
            },
            listOf(randomChannel(type = "b"), randomChannel(type = "a")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest(),
                    it,
                )
            },
            listOf(randomChannel(type = "a"), randomChannel(type = "b")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply { sort = QuerySortByField.ascByName("cid") },
                    it,
                )
            },
            listOf(randomChannel(type = "b"), randomChannel(type = "a")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply { sort = QuerySortByField.ascByName("cid") },
                    it.reversed(),
                )
            },
            listOf(randomChannel(type = "a"), randomChannel(type = "b")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply { sort = QuerySortByField.descByName("cid") },
                    it.reversed(),
                )
            },
            listOf(randomChannel(type = "b"), randomChannel(type = "a")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply { sort = QuerySortByField.descByName("cid") },
                    it,
                )
            },
            listOf(
                randomChannel(type = "a", id = "a"),
                randomChannel(type = "b", id = "a"),
                randomChannel(type = "b", id = "b"),
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySortByField.ascByName<Channel>("cid").ascByName("type")
                    },
                    it,
                )
            },
            listOf(
                randomChannel(type = "a", id = "a"),
                randomChannel(type = "b", id = "b"),
                randomChannel(type = "b", id = "a"),
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySortByField.ascByName<Channel>("cid").ascByName("type")
                    },
                    listOf(it[0], it[2], it[1]),
                )
            },
            listOf(
                randomChannel(type = "a", id = "a"),
                randomChannel(type = "b", id = "b"),
                randomChannel(type = "b", id = "a"),
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySortByField.ascByName<Channel>("cid").descByName("type")
                    },
                    listOf(it[0], it[2], it[1]),
                )
            },
            listOf(
                randomChannel(type = "a", id = "a"),
                randomChannel(type = "b", id = "a"),
                randomChannel(type = "b", id = "b"),
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySortByField.ascByName<Channel>("cid").descByName("type")
                    },
                    it,
                )
            },
            listOf(
                randomChannel(type = "a", id = "a"),
                randomChannel(type = "b", id = "a"),
                randomChannel(type = "b", id = "b"),
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort =
                            QuerySortByField.ascByName<Channel>("cid").ascByName("SomeInvalidField").descByName("type")
                    },
                    it,
                )
            },
            // last_updated is a computed field based on max(createdAt, lastMessageAt)
            listOf(
                randomChannel(type = "c", createdAt = null, lastMessageAt = calendar(2020, 10, 2)),
                randomChannel(type = "a", createdAt = calendar(2020, 10, 4), lastMessageAt = null),
                randomChannel(type = "b", createdAt = calendar(2020, 10, 1), lastMessageAt = calendar(2020, 10, 3)),
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySortByField.descByName("last_updated")
                    },
                    listOf(it[1], it[2], it[0]),
                )
            },
            // created_at should map to channel.createdAt
            listOf(
                randomChannel(type = "c", createdAt = calendar(2020, 10, 2)),
                randomChannel(type = "a", createdAt = calendar(2020, 10, 4)),
                randomChannel(type = "b", createdAt = calendar(2020, 10, 3)),
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySortByField.descByName("created_at")
                    },
                    listOf(it[1], it[2], it[0]),
                )
            },
            // last_message_at should map to channel.lastMessageAt
            listOf(
                randomChannel(type = "c", lastMessageAt = calendar(2020, 10, 2)),
                randomChannel(type = "a", lastMessageAt = calendar(2020, 10, 4)),
                randomChannel(type = "b", lastMessageAt = calendar(2020, 10, 3)),
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySortByField.descByName("last_message_at")
                    },
                    listOf(it[1], it[2], it[0]),
                )
            },
        )
    }
}
