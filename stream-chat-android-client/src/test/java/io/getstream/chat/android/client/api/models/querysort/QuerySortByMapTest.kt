package io.getstream.chat.android.client.api.models.querysort

import io.getstream.chat.android.client.api.models.querysort.QuerySortByMap.Companion.ascByName
import io.getstream.chat.android.client.models.BannedUsersSort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class QuerySortByMapTest {

    /**
     * [generateQuerySortInput]
     */
    @ParameterizedTest
    @MethodSource("generateQuerySortInput")
    fun `Two QuerySortByMap with the same content should produce the same hashcode`(
        a: QuerySortByMap<out QueryableByMap>,
        b: QuerySortByMap<out QueryableByMap>,
    ) {
        a.hashCode() `should be equal to` b.hashCode()
    }

    @Test
    fun `Two same query sorts should be equal`() {
        val sort1 = QuerySortByMap
            .ascByName<Channel>("member_count")
            .ascByName("created_at")

        val sort2 = QuerySortByMap
            .ascByName<Channel>("member_count")
            .ascByName("created_at")

        sort1 `should be equal to` sort2
    }

    companion object {

        @JvmStatic
        fun generateQuerySortInput() = listOf(
            randomString().let {
                Arguments.of(
                    QuerySortByMap.ascByName<Channel>(it),
                    QuerySortByMap.ascByName<Channel>(it)
                )
            },
            randomString().let {
                Arguments.of(
                    QuerySortByMap.ascByName<BannedUsersSort>(it),
                    QuerySortByMap.ascByName<BannedUsersSort>(it)
                )
            },
            randomString().let {
                Arguments.of(
                    QuerySortByMap.descByName<Channel>(it),
                    QuerySortByMap.descByName<Channel>(it)
                )
            },
            randomString().let {
                Arguments.of(
                    QuerySortByMap.descByName<BannedUsersSort>(it),
                    QuerySortByMap.descByName<BannedUsersSort>(it)
                )
            },
        )
    }
}
