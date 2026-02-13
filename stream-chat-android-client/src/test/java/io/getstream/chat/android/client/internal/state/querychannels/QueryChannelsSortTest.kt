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

package io.getstream.chat.android.client.internal.state.querychannels

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.CustomObject
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.ascByName
import io.getstream.chat.android.randomChannel
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date

internal class QueryChannelsSortTest {

    /** [sortArguments] */
    @ParameterizedTest(name = "{0}")
    @MethodSource("sortArguments")
    fun `Given shuffled channels When sorting the list Should return sorted channels`(
        testName: String,
        channelList: List<Channel>,
        querySort: QuerySortByField<Channel>,
        expectedChannelList: List<CustomObject>,
    ) {
        val result = channelList.sortedWith(querySort.comparator)

        result `should be equal to` expectedChannelList
    }

    companion object {

        @JvmStatic
        fun sortArguments() = lastUpdatedSortArguments() +
            lastMessageAtSortArguments() +
            updatedAtSortArguments() +
            createdAtSortArguments() +
            memberCountSortArguments() +
            unreadCountSortArguments() +
            hasUnreadSortArguments() +
            nameSortArguments() +
            unsupportedSortArguments() +
            multiSortByFieldReferencesArguments() +
            multiSortByFieldNamesArguments()

        @Suppress("LongMethod")
        @JvmStatic
        fun lastUpdatedSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by lastUpdated field reference in ascending order",
                querySort = QuerySortByField.ascByName("lastUpdated"),
            ) {
                randomChannel(
                    createdAt = dateWithOffset(offsetSeconds = -100),
                    lastMessageAt = dateWithOffset(offsetSeconds = it),
                )
            },
            sortArguments(
                testName = "Sorting by lastUpdated field reference in descending order",
                querySort = QuerySortByField.descByName("lastUpdated"),
            ) {
                randomChannel(
                    createdAt = dateWithOffset(offsetSeconds = -100),
                    lastMessageAt = dateWithOffset(offsetSeconds = -it),
                )
            },
            sortArguments(
                testName = "Sorting by last_updated field name in ascending order",
                querySort = QuerySortByField.ascByName("last_updated"),
            ) {
                randomChannel(
                    createdAt = dateWithOffset(offsetSeconds = -100),
                    lastMessageAt = dateWithOffset(offsetSeconds = it),
                )
            },
            sortArguments(
                testName = "Sorting by last_updated field name in descending order",
                querySort = QuerySortByField.descByName("last_updated"),
            ) {
                randomChannel(
                    createdAt = dateWithOffset(offsetSeconds = -100),
                    lastMessageAt = dateWithOffset(offsetSeconds = -it),
                )
            },
        )

        @JvmStatic
        fun lastMessageAtSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by lastMessageAt field reference in ascending order",
                querySort = QuerySortByField.ascByName("lastMessageAt"),
            ) {
                randomChannel(
                    lastMessageAt = dateWithOffset(offsetSeconds = it),
                )
            },
            sortArguments(
                testName = "Sorting by lastMessageAt field reference in descending order",
                querySort = QuerySortByField.descByName("lastMessageAt"),
            ) {
                randomChannel(
                    lastMessageAt = dateWithOffset(offsetSeconds = -it),
                )
            },
            sortArguments(
                testName = "Sorting by last_message_at field name in ascending order",
                querySort = QuerySortByField.ascByName("last_message_at"),
            ) {
                randomChannel(
                    lastMessageAt = dateWithOffset(offsetSeconds = it),
                )
            },
            sortArguments(
                testName = "Sorting by last_message_at field name in descending order",
                querySort = QuerySortByField.descByName("last_message_at"),
            ) {
                randomChannel(
                    lastMessageAt = dateWithOffset(offsetSeconds = -it),
                )
            },
        )

        @JvmStatic
        fun updatedAtSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by updatedAt field reference in ascending order",
                querySort = QuerySortByField.ascByName("updatedAt"),
            ) {
                randomChannel(updatedAt = dateWithOffset(offsetSeconds = it))
            },
            sortArguments(
                testName = "Sorting by updatedAt field reference in descending order",
                querySort = QuerySortByField.descByName("updatedAt"),
            ) {
                randomChannel(updatedAt = dateWithOffset(offsetSeconds = -it))
            },
            sortArguments(
                testName = "Sorting by updated_at field name in ascending order",
                querySort = QuerySortByField.ascByName("updated_at"),
            ) {
                randomChannel(updatedAt = dateWithOffset(offsetSeconds = it))
            },
            sortArguments(
                testName = "Sorting by updated_at field name in descending order",
                querySort = QuerySortByField.descByName("updated_at"),
            ) {
                randomChannel(updatedAt = dateWithOffset(offsetSeconds = -it))
            },
        )

        @JvmStatic
        fun createdAtSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by createdAt field reference in ascending order",
                querySort = QuerySortByField.ascByName("createdAt"),
            ) {
                randomChannel(createdAt = dateWithOffset(offsetSeconds = it))
            },
            sortArguments(
                testName = "Sorting by createdAt field reference in descending order",
                querySort = QuerySortByField.descByName("createdAt"),
            ) {
                randomChannel(createdAt = dateWithOffset(offsetSeconds = -it))
            },
            sortArguments(
                testName = "Sorting by created_at field name in ascending order",
                querySort = QuerySortByField.ascByName("created_at"),
            ) {
                randomChannel(createdAt = dateWithOffset(offsetSeconds = it))
            },
            sortArguments(
                testName = "Sorting by created_at field name in descending order",
                querySort = QuerySortByField.descByName("created_at"),
            ) {
                randomChannel(createdAt = dateWithOffset(offsetSeconds = -it))
            },
        )

        @JvmStatic
        fun memberCountSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by memberCount field reference in ascending order",
                querySort = QuerySortByField.ascByName("memberCount"),
            ) {
                randomChannel(memberCount = it)
            },
            sortArguments(
                testName = "Sorting by memberCount field reference in descending order",
                querySort = QuerySortByField.descByName("memberCount"),
            ) {
                randomChannel(memberCount = 9 - it)
            },
            sortArguments(
                testName = "Sorting by member_count field name in ascending order",
                querySort = QuerySortByField.ascByName("member_count"),
            ) {
                randomChannel(memberCount = it)
            },
            sortArguments(
                testName = "Sorting by member_count field name in descending order",
                querySort = QuerySortByField.descByName("member_count"),
            ) {
                randomChannel(memberCount = 9 - it)
            },
        )

        @JvmStatic
        fun unreadCountSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by unreadCount field reference in ascending order",
                querySort = QuerySortByField.ascByName("unreadCount"),
            ) {
                randomChannel(unreadCount = it)
            },
            sortArguments(
                testName = "Sorting by unreadCount field reference in descending order",
                querySort = QuerySortByField.descByName("unreadCount"),
            ) {
                randomChannel(unreadCount = 9 - it)
            },
            sortArguments(
                testName = "Sorting by unread_count field name in ascending order",
                querySort = QuerySortByField.ascByName("unread_count"),
            ) {
                randomChannel(unreadCount = it)
            },
            sortArguments(
                testName = "Sorting by unread_count field name in descending order",
                querySort = QuerySortByField.descByName("unread_count"),
            ) {
                randomChannel(unreadCount = 9 - it)
            },
        )

        @JvmStatic
        @Suppress("LongMethod")
        fun hasUnreadSortArguments() = listOf(
            List(6) {
                randomChannel(unreadCount = if (it < 3) 0 else it)
            }.let { expectedList ->
                Arguments.of(
                    "Sorting by hasUnread field reference in ascending order",
                    listOf(
                        expectedList[0],
                        expectedList[3],
                        expectedList[1],
                        expectedList[4],
                        expectedList[2],
                        expectedList[5],
                    ),
                    QuerySortByField.ascByName<Channel>("hasUnread"),
                    expectedList,
                )
            },
            List(6) {
                randomChannel(unreadCount = if (it < 3) 6 - it else 0)
            }.let { expectedList ->
                Arguments.of(
                    "Sorting by hasUnread field reference in descending order",
                    listOf(
                        expectedList[0],
                        expectedList[3],
                        expectedList[1],
                        expectedList[4],
                        expectedList[2],
                        expectedList[5],
                    ),
                    QuerySortByField.descByName<Channel>("hasUnread"),
                    expectedList,
                )
            },
            List(6) {
                randomChannel(unreadCount = if (it < 3) 0 else it)
            }.let { expectedList ->
                Arguments.of(
                    "Sorting by has_unread field name in ascending order",
                    listOf(
                        expectedList[0],
                        expectedList[3],
                        expectedList[1],
                        expectedList[4],
                        expectedList[2],
                        expectedList[5],
                    ),
                    QuerySortByField.ascByName<Channel>("has_unread"),
                    expectedList,
                )
            },
            List(6) {
                randomChannel(unreadCount = if (it < 3) 6 - it else 0)
            }.let { expectedList ->
                Arguments.of(
                    "Sorting by has_unread field name in descending order",
                    listOf(
                        expectedList[0],
                        expectedList[3],
                        expectedList[1],
                        expectedList[4],
                        expectedList[2],
                        expectedList[5],
                    ),
                    QuerySortByField.descByName<Channel>("has_unread"),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun nameSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by name extra data field in ascending order",
                querySort = QuerySortByField.ascByName("name"),
            ) {
                randomChannel(name = "$it")
            },
            sortArguments(
                testName = "Sorting by name extra data field in descending order",
                querySort = QuerySortByField.descByName("name"),
            ) {
                randomChannel(name = "${9 - it}")
            },
        )

        @JvmStatic
        fun unsupportedSortArguments() = listOf(
            List(10) { randomChannel() }.let { expectedList ->
                Arguments.of(
                    "Sorting by unsupported field in ascending order",
                    expectedList,
                    QuerySortByField.ascByName<Channel>("unsupported_field"),
                    expectedList,
                )
            },
            List(10) { randomChannel() }.let { expectedList ->
                Arguments.of(
                    "Sorting by unsupported field in descending order",
                    expectedList,
                    QuerySortByField.descByName<Channel>("unsupported_field"),
                    expectedList,
                )
            },
        )

        @JvmStatic
        fun multiSortByFieldReferencesArguments() = listOf(
            sortArguments(
                testName = "Sorting by unreadCount field reference in descending order and by memberCount field " +
                    "reference in ascending order",
                querySort = QuerySortByField
                    .descByName<Channel>("unreadCount")
                    .ascByName("memberCount"),
            ) {
                randomChannel(
                    memberCount = it,
                    unreadCount = if (it < 3) 6 - it else 0,
                )
            },
        )

        @JvmStatic
        fun multiSortByFieldNamesArguments() = listOf(
            sortArguments(
                testName = "Sorting by unread_count field name in descending order and by name extra data field in " +
                    "ascending order",
                querySort = QuerySortByField
                    .descByName<Channel>("unreadCount")
                    .ascByName("name"),
            ) {
                randomChannel(
                    name = "$it",
                    unreadCount = if (it < 5) 9 - it else 0,
                )
            },
        )

        private fun sortArguments(
            testName: String,
            querySort: QuerySortByField<Channel>,
            channelFactory: (Int) -> Channel,
        ): Arguments {
            return List(10, channelFactory).let { expectedList ->
                Arguments.of(
                    testName,
                    expectedList.shuffled(),
                    querySort,
                    expectedList,
                )
            }
        }

        private fun dateWithOffset(offsetSeconds: Int): Date {
            return Date(System.currentTimeMillis() + offsetSeconds * 1000)
        }
    }
}
