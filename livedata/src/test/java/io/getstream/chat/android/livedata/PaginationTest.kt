package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.extensions.applyPagination
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class PaginationTest {

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.livedata.PaginationTest#createPaginationInput")
    internal fun `Should return a list of channelEntityPairs properly sorted by pagination param`(
        inputList: List<Channel>,
        pagination: AnyChannelPaginationRequest,
        expectedList: List<Channel>
    ) {
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
                    AnyChannelPaginationRequest().apply { sort = QuerySort().apply { asc("cid") } },
                    it
                )
            },
            listOf(randomChannel(cid = "b"), randomChannel(cid = "a")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply { sort = QuerySort().apply { asc("cid") } },
                    it.reversed()
                )
            },
            listOf(randomChannel(cid = "a"), randomChannel(cid = "b")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply { sort = QuerySort().apply { desc("cid") } },
                    it.reversed()
                )
            },
            listOf(randomChannel(cid = "b"), randomChannel(cid = "a")).let {
                Arguments.of(
                    it,
                    AnyChannelPaginationRequest().apply { sort = QuerySort().apply { desc("cid") } },
                    it
                )
            },
            listOf(
                randomChannel(cid = "a"),
                randomChannel(cid = "b", type = "a"),
                randomChannel(cid = "b", type = "b")
            ).let {
                Arguments.of(it, AnyChannelPaginationRequest().apply {
                    sort = QuerySort().apply {
                        asc("cid")
                        asc("type")
                    }
                }, it)
            },
            listOf(
                randomChannel(cid = "a"),
                randomChannel(cid = "b", type = "b"),
                randomChannel(cid = "b", type = "a")
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
                randomChannel(cid = "a"),
                randomChannel(cid = "b", type = "a"),
                randomChannel(cid = "b", type = "b")
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
                randomChannel(cid = "a"),
                randomChannel(cid = "b", type = "b"),
                randomChannel(cid = "b", type = "a")
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
                randomChannel(cid = "a"),
                randomChannel(cid = "b", type = "b"),
                randomChannel(cid = "b", type = "a")
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
            }
        )
    }
}
