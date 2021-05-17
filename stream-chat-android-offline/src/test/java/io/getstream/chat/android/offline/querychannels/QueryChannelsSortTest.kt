package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.CustomObject
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.offline.randomChannel
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
        querySort: QuerySort<Channel>,
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
            multiSortByFieldReferencesArguments() +
            multiSortByFieldNamesArguments() +
            unreadCountWithNullsSortArguments()

        @JvmStatic
        fun lastUpdatedSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by lastUpdated field reference in ascending order",
                querySort = QuerySort<Channel>().asc(Channel::lastUpdated)
            ) {
                randomChannel(
                    createdAt = dateWithOffset(offsetSeconds = -100),
                    lastMessageAt = dateWithOffset(offsetSeconds = it)
                )
            },
            sortArguments(
                testName = "Sorting by lastUpdated field reference in descending order",
                querySort = QuerySort<Channel>().desc(Channel::lastUpdated)
            ) {
                randomChannel(
                    createdAt = dateWithOffset(offsetSeconds = -100),
                    lastMessageAt = dateWithOffset(offsetSeconds = -it)
                )
            },
            sortArguments(
                testName = "Sorting by last_updated field name in ascending order",
                querySort = QuerySort<Channel>().asc("last_updated")
            ) {
                randomChannel(
                    createdAt = dateWithOffset(offsetSeconds = -100),
                    lastMessageAt = dateWithOffset(offsetSeconds = it)
                )
            },
            sortArguments(
                testName = "Sorting by last_updated field name in descending order",
                querySort = QuerySort<Channel>().desc("last_updated")
            ) {
                randomChannel(
                    createdAt = dateWithOffset(offsetSeconds = -100),
                    lastMessageAt = dateWithOffset(offsetSeconds = -it)
                )
            },
        )

        @JvmStatic
        fun lastMessageAtSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by lastMessageAt field reference in ascending order",
                querySort = QuerySort<Channel>().asc(Channel::lastMessageAt)
            ) {
                randomChannel(lastMessageAt = dateWithOffset(offsetSeconds = it))
            },
            sortArguments(
                testName = "Sorting by lastMessageAt field reference in descending order",
                querySort = QuerySort<Channel>().desc(Channel::lastMessageAt)
            ) {
                randomChannel(lastMessageAt = dateWithOffset(offsetSeconds = -it))
            },
            sortArguments(
                testName = "Sorting by last_message_at field name in ascending order",
                querySort = QuerySort<Channel>().asc("last_message_at")
            ) {
                randomChannel(lastMessageAt = dateWithOffset(offsetSeconds = it))
            },
            sortArguments(
                testName = "Sorting by last_message_at field name in descending order",
                querySort = QuerySort<Channel>().desc("last_message_at")
            ) {
                randomChannel(lastMessageAt = dateWithOffset(offsetSeconds = -it))
            },
        )

        @JvmStatic
        fun updatedAtSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by updatedAt field reference in ascending order",
                querySort = QuerySort<Channel>().asc(Channel::updatedAt)
            ) {
                randomChannel(updatedAt = dateWithOffset(offsetSeconds = it))
            },
            sortArguments(
                testName = "Sorting by updatedAt field reference in descending order",
                querySort = QuerySort<Channel>().desc(Channel::updatedAt)
            ) {
                randomChannel(updatedAt = dateWithOffset(offsetSeconds = -it))
            },
            sortArguments(
                testName = "Sorting by updated_at field name in ascending order",
                querySort = QuerySort<Channel>().asc("updated_at")
            ) {
                randomChannel(updatedAt = dateWithOffset(offsetSeconds = it))
            },
            sortArguments(
                testName = "Sorting by updated_at field name in descending order",
                querySort = QuerySort<Channel>().desc("updated_at")
            ) {
                randomChannel(updatedAt = dateWithOffset(offsetSeconds = -it))
            },
        )

        @JvmStatic
        fun createdAtSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by createdAt field reference in ascending order",
                querySort = QuerySort<Channel>().asc(Channel::createdAt)
            ) {
                randomChannel(createdAt = dateWithOffset(offsetSeconds = it))
            },
            sortArguments(
                testName = "Sorting by createdAt field reference in descending order",
                querySort = QuerySort<Channel>().desc(Channel::createdAt)
            ) {
                randomChannel(createdAt = dateWithOffset(offsetSeconds = -it))
            },
            sortArguments(
                testName = "Sorting by created_at field name in ascending order",
                querySort = QuerySort<Channel>().asc("created_at")
            ) {
                randomChannel(createdAt = dateWithOffset(offsetSeconds = it))
            },
            sortArguments(
                testName = "Sorting by created_at field name in descending order",
                querySort = QuerySort<Channel>().desc("created_at")
            ) {
                randomChannel(createdAt = dateWithOffset(offsetSeconds = -it))
            }
        )

        @JvmStatic
        fun memberCountSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by memberCount field reference in ascending order",
                querySort = QuerySort<Channel>().asc(Channel::memberCount)
            ) {
                randomChannel(memberCount = it)
            },
            sortArguments(
                testName = "Sorting by memberCount field reference in descending order",
                querySort = QuerySort<Channel>().desc(Channel::memberCount)
            ) {
                randomChannel(memberCount = 10 - it)
            },
            sortArguments(
                testName = "Sorting by member_count field name in ascending order",
                querySort = QuerySort<Channel>().asc("member_count")
            ) {
                randomChannel(memberCount = it)
            },
            sortArguments(
                testName = "Sorting by member_count field name in descending order",
                querySort = QuerySort<Channel>().desc("member_count")
            ) {
                randomChannel(memberCount = 10 - it)
            },
        )

        @JvmStatic
        fun unreadCountSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by unreadCount field reference in ascending order",
                querySort = QuerySort<Channel>().asc(Channel::unreadCount)
            ) {
                randomChannel(unreadCount = it)
            },
            sortArguments(
                testName = "Sorting by unreadCount field reference in descending order",
                querySort = QuerySort<Channel>().desc(Channel::unreadCount)
            ) {
                randomChannel(unreadCount = 10 - it)
            },
            sortArguments(
                testName = "Sorting by unread_count field name in ascending order",
                querySort = QuerySort<Channel>().asc("unread_count")
            ) {
                randomChannel(unreadCount = it)
            },
            sortArguments(
                testName = "Sorting by unread_count field name in descending order",
                querySort = QuerySort<Channel>().desc("unread_count")
            ) {
                randomChannel(unreadCount = 10 - it)
            },
        )

        @JvmStatic
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
                        expectedList[5]
                    ),
                    QuerySort<Channel>().asc(Channel::hasUnread),
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
                        expectedList[5]
                    ),
                    QuerySort<Channel>().desc(Channel::hasUnread),
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
                        expectedList[5]
                    ),
                    QuerySort<Channel>().asc("has_unread"),
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
                        expectedList[5]
                    ),
                    QuerySort<Channel>().desc("has_unread"),
                    expectedList,
                )
            }
        )

        @JvmStatic
        fun nameSortArguments() = listOf(
            sortArguments(
                testName = "Sorting by name extra data field in ascending order",
                querySort = QuerySort<Channel>().asc("name")
            ) {
                randomChannel().apply { name = "$it" }
            },
            sortArguments(
                testName = "Sorting by name extra data field in descending order",
                querySort = QuerySort<Channel>().desc("name")
            ) {
                randomChannel().apply { name = "${10 - it}" }
            }
        )

        @JvmStatic
        fun multiSortByFieldReferencesArguments() = listOf(
            sortArguments(
                testName = "Sorting by unreadCount field reference in descending order and by memberCount field reference in ascending order",
                querySort = QuerySort<Channel>()
                    .desc(Channel::unreadCount)
                    .asc(Channel::memberCount)
            ) {
                randomChannel(
                    memberCount = it,
                    unreadCount = if (it < 3) 6 - it else 0
                )
            },
        )

        @JvmStatic
        fun multiSortByFieldNamesArguments() = listOf(
            sortArguments(
                testName = "Sorting by unread_count field name in descending order and by name extra data field in ascending order",
                querySort = QuerySort<Channel>()
                    .desc("unread_count")
                    .asc("name")
            ) {
                randomChannel().apply {
                    name = "$it"
                    unreadCount = if (it < 5) 10 - it else 0
                }
            },
        )

        @JvmStatic
        fun unreadCountWithNullsSortArguments() = listOf(
            List(6) {
                randomChannel(unreadCount = if (it < 3) null else it)
            }.let { expectedList ->
                Arguments.of(
                    "Sorting by nullable hasUnread field reference in ascending order",
                    listOf(
                        expectedList[0],
                        expectedList[3],
                        expectedList[1],
                        expectedList[4],
                        expectedList[2],
                        expectedList[5]
                    ),
                    QuerySort<Channel>().asc(Channel::hasUnread),
                    expectedList,
                )
            },
            List(6) {
                randomChannel(unreadCount = if (it < 3) 6 - it else null)
            }.let { expectedList ->
                Arguments.of(
                    "Sorting by nullable hasUnread field reference in descending order",
                    listOf(
                        expectedList[0],
                        expectedList[3],
                        expectedList[1],
                        expectedList[4],
                        expectedList[2],
                        expectedList[5]
                    ),
                    QuerySort<Channel>().desc(Channel::hasUnread),
                    expectedList,
                )
            },
            List(6) {
                randomChannel(unreadCount = if (it < 3) null else it)
            }.let { expectedList ->
                Arguments.of(
                    "Sorting by nullable has_unread field name in descending order",
                    listOf(
                        expectedList[0],
                        expectedList[3],
                        expectedList[1],
                        expectedList[4],
                        expectedList[2],
                        expectedList[5]
                    ),
                    QuerySort<Channel>().asc("unread_count"),
                    expectedList,
                )
            },
            List(6) {
                randomChannel(unreadCount = if (it < 3) 6 - it else null)
            }.let { expectedList ->
                Arguments.of(
                    "Sorting by nullable has_unread field name in descending order",
                    listOf(
                        expectedList[0],
                        expectedList[3],
                        expectedList[1],
                        expectedList[4],
                        expectedList[2],
                        expectedList[5]
                    ),
                    QuerySort<Channel>().desc("unread_count"),
                    expectedList,
                )
            }
        )

        private fun sortArguments(
            testName: String,
            querySort: QuerySort<Channel>,
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
