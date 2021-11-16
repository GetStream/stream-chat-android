package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.api.models.QuerySort.Companion.ascByName
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
    fun `Two QuerySort with the same content should produce the same hashcode`(a: QuerySort<Any>, b: QuerySort<Any>) {
        a.hashCode() `should be equal to` b.hashCode()
    }

    @Test
    fun `Two same query sorts should be equal`() {
        val sort1 = QuerySort.asc(Channel::memberCount).ascByName("created_at")
        val sort2 = QuerySort.asc<Channel>("member_count").asc(Channel::createdAt)

        sort1 `should be equal to` sort2
    }

    companion object {

        @JvmStatic
        fun generateQuerySortInput() = listOf(
            randomString().let { Arguments.of(QuerySort.Companion.asc<Channel>(it), QuerySort.Companion.asc<Channel>(it)) },
            randomString().let { Arguments.of(QuerySort.Companion.asc<Message>(it), QuerySort.Companion.asc<Message>(it)) },
            randomString().let { Arguments.of(QuerySort.Companion.desc<Channel>(it), QuerySort.Companion.desc<Channel>(it)) },
            randomString().let { Arguments.of(QuerySort.Companion.desc<Message>(it), QuerySort.Companion.desc<Message>(it)) },
        )
    }
}
