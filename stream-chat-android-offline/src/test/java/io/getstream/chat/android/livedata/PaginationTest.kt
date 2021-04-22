package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QuerySort.Companion.ascByName
import io.getstream.chat.android.client.api.models.QuerySort.Companion.descByName
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.utils.calendar
import io.getstream.chat.android.offline.extensions.applyPagination
import io.getstream.chat.android.offline.request.AnyChannelPaginationRequest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PaginationTest {

    /** [createPaginationInput] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.livedata.PaginationTest#createPaginationInput")
    internal fun `Should return a list of channelEntityPairs properly sorted by pagination param`(
        inputList: List<Channel>,
        pagination: AnyChannelPaginationRequest,
        expectedList: List<Channel>
    ) {
        // show an easy to use diff between the two results
        inputList.applyPagination(pagination) `should be equal to` expectedList
    }

    companion object {
        @JvmStatic
        fun createPaginationInput() = listOf(
            Arguments.of(listOf<Channel>(), AnyChannelPaginationRequest(), listOf<Channel>()),
            listOf(randomChannel()).let { Arguments.of(it, AnyChannelPaginationRequest(), it) },
            listOf(randomChannel(cid = "a"), randomChannel(cid = "b")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest(),
                    it
                )
            },
            listOf(randomChannel(cid = "b"), randomChannel(cid = "a")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest(),
                    it
                )
            },
            listOf(randomChannel(cid = "a"), randomChannel(cid = "b")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply { sort = QuerySort.asc("cid") },
                    it
                )
            },
            listOf(randomChannel(cid = "b"), randomChannel(cid = "a")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply { sort = QuerySort.asc("cid") },
                    it.reversed()
                )
            },
            listOf(randomChannel(cid = "a"), randomChannel(cid = "b")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply { sort = QuerySort.desc("cid") },
                    it.reversed()
                )
            },
            listOf(randomChannel(cid = "b"), randomChannel(cid = "a")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply { sort = QuerySort.desc("cid") },
                    it
                )
            },
            listOf(
                randomChannel(cid = "a"),
                randomChannel(cid = "b", type = "a"),
                randomChannel(cid = "b", type = "b")
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort.asc<Channel>("cid").ascByName("type")
                    },
                    it
                )
            },
            listOf(
                randomChannel(cid = "a"),
                randomChannel(cid = "b", type = "b"),
                randomChannel(cid = "b", type = "a")
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort.asc<Channel>("cid").ascByName("type")
                    },
                    listOf(it[0], it[2], it[1])
                )
            },
            listOf(
                randomChannel(cid = "a"),
                randomChannel(cid = "b", type = "a"),
                randomChannel(cid = "b", type = "b")
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort.asc<Channel>("cid").descByName("type")
                    },
                    listOf(it[0], it[2], it[1])
                )
            },
            listOf(
                randomChannel(cid = "a"),
                randomChannel(cid = "b", type = "b"),
                randomChannel(cid = "b", type = "a")
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort.asc<Channel>("cid").descByName("type")
                    },
                    it
                )
            },
            listOf(
                randomChannel(cid = "a"),
                randomChannel(cid = "b", type = "b"),
                randomChannel(cid = "b", type = "a")
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort.asc<Channel>("cid").ascByName("SomeInvalidField").descByName("type")
                    },
                    it
                )
            },
            // last_updated is a computed field based on max(createdAt, lastMessageAt)
            listOf(
                randomChannel(cid = "c", createdAt = null, lastMessageAt = calendar(2020, 10, 2)),
                randomChannel(cid = "a", createdAt = calendar(2020, 10, 4), lastMessageAt = null),
                randomChannel(cid = "b", createdAt = calendar(2020, 10, 1), lastMessageAt = calendar(2020, 10, 3))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort.desc("last_updated")
                    },
                    listOf(it[1], it[2], it[0])
                )
            },
            // created_at should map to channel.createdAt
            listOf(
                randomChannel(cid = "c", createdAt = calendar(2020, 10, 2)),
                randomChannel(cid = "a", createdAt = calendar(2020, 10, 4)),
                randomChannel(cid = "b", createdAt = calendar(2020, 10, 3))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort.desc("created_at")
                    },
                    listOf(it[1], it[2], it[0])
                )
            },
            // last_message_at should map to channel.lastMessageAt
            listOf(
                randomChannel(cid = "c", lastMessageAt = calendar(2020, 10, 2)),
                randomChannel(cid = "a", lastMessageAt = calendar(2020, 10, 4)),
                randomChannel(cid = "b", lastMessageAt = calendar(2020, 10, 3))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort.desc("last_message_at")
                    },
                    listOf(it[1], it[2], it[0])
                )
            }
        )
    }
}
