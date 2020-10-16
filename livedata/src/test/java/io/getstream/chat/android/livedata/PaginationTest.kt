package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.livedata.entity.ChannelEntityPair
import io.getstream.chat.android.livedata.extensions.applyPagination
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.livedata.utils.calendar
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PaginationTest {

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.livedata.PaginationTest#createPaginationInput")
    internal fun `Should return a list of channelEntityPairs properly sorted by pagination param`(
        inputList: List<ChannelEntityPair>,
        pagination: AnyChannelPaginationRequest,
        expectedList: List<ChannelEntityPair>
    ) {
        // show an easy to use diff between the two results
        inputList.applyPagination(pagination) `should be equal to` expectedList
    }

    companion object {
        @JvmStatic
        fun createPaginationInput() = listOf(
            Arguments.of(listOf<ChannelEntityPair>(), AnyChannelPaginationRequest(), listOf<ChannelEntityPair>()),
            listOf(randomChannelEntityPair()).let { Arguments.of(it, AnyChannelPaginationRequest(), it) },
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "a")),
                randomChannelEntityPair(channel = randomChannel(cid = "b"))
            ).let { Arguments.of(it, AnyChannelPaginationRequest(), it) },
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "b")),
                randomChannelEntityPair(channel = randomChannel(cid = "a"))
            ).let { Arguments.of(it, AnyChannelPaginationRequest(), it) },
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "a")),
                randomChannelEntityPair(channel = randomChannel(cid = "b"))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply { asc("cid") }
                    },
                    it
                )
            },
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "b")),
                randomChannelEntityPair(channel = randomChannel(cid = "a"))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply { asc("cid") }
                    },
                    it.reversed()
                )
            },
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "a")),
                randomChannelEntityPair(channel = randomChannel(cid = "b"))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply { desc("cid") }
                    },
                    it.reversed()
                )
            },
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "b")),
                randomChannelEntityPair(channel = randomChannel(cid = "a"))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply { desc("cid") }
                    },
                    it
                )
            },
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "a")),
                randomChannelEntityPair(channel = randomChannel(cid = "b", type = "a")),
                randomChannelEntityPair(channel = randomChannel(cid = "b", type = "b"))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply {
                            asc("cid")
                            asc("type")
                        }
                    },
                    it
                )
            },
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "a")),
                randomChannelEntityPair(channel = randomChannel(cid = "b", type = "b")),
                randomChannelEntityPair(channel = randomChannel(cid = "b", type = "a"))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply {
                            asc("cid")
                            asc("type")
                        }
                    },
                    listOf(it[0], it[2], it[1])
                )
            },
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "a")),
                randomChannelEntityPair(channel = randomChannel(cid = "b", type = "a")),
                randomChannelEntityPair(channel = randomChannel(cid = "b", type = "b"))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply {
                            asc("cid")
                            desc("type")
                        }
                    },
                    listOf(it[0], it[2], it[1])
                )
            },
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "a")),
                randomChannelEntityPair(channel = randomChannel(cid = "b", type = "b")),
                randomChannelEntityPair(channel = randomChannel(cid = "b", type = "a"))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply {
                            asc("cid")
                            desc("type")
                        }
                    },
                    it
                )
            },
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "a")),
                randomChannelEntityPair(channel = randomChannel(cid = "b", type = "b")),
                randomChannelEntityPair(channel = randomChannel(cid = "b", type = "a"))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply {
                            asc("cid")
                            asc("SomeInvalidField")
                            desc("type")
                        }
                    },
                    it
                )
            },
            // last_updated is a computed field based on max(createdAt, lastMessageAt)
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "c", createdAt = null, lastMessageAt = calendar(2020, 10, 2))),
                randomChannelEntityPair(channel = randomChannel(cid = "a", createdAt = calendar(2020, 10, 4), lastMessageAt = null)),
                randomChannelEntityPair(channel = randomChannel(cid = "b", createdAt = calendar(2020, 10, 1), lastMessageAt = calendar(2020, 10, 3)))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply {
                            desc("last_updated")
                        }
                    },
                    listOf(it[1], it[2], it[0])
                )
            },
            // created_at should map to channel.createdAt
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "c", createdAt = calendar(2020, 10, 2))),
                randomChannelEntityPair(channel = randomChannel(cid = "a", createdAt = calendar(2020, 10, 4))),
                randomChannelEntityPair(channel = randomChannel(cid = "b", createdAt = calendar(2020, 10, 3)))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply {
                            desc("created_at")
                        }
                    },
                    listOf(it[1], it[2], it[0])
                )
            },
            // last_message_at should map to channel.lastMessageAt
            listOf(
                randomChannelEntityPair(channel = randomChannel(cid = "c", lastMessageAt = calendar(2020, 10, 2))),
                randomChannelEntityPair(channel = randomChannel(cid = "a", lastMessageAt = calendar(2020, 10, 4))),
                randomChannelEntityPair(channel = randomChannel(cid = "b", lastMessageAt = calendar(2020, 10, 3)))
            ).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply {
                        sort = QuerySort().apply {
                            desc("last_message_at")
                        }
                    },
                    listOf(it[1], it[2], it[0])
                )
            }
        )
    }
}
