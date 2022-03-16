package io.getstream.chat.android.offline.repository.database.converter

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QuerySort.Companion.ascByName
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.repository.database.converter.internal.QuerySortConverter
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class QuerySortConverterTests {

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Should store and extract the same object`(sort: QuerySort<Channel>) {
        val sut = QuerySortConverter()
        val string = sut.objectToString(sort)
        val output = sut.stringToObject(string)

        output shouldBeEqualTo sort
    }

    companion object {
        @JvmStatic
        fun arguments() = listOf<QuerySort<Channel>>(
            QuerySort.asc(Channel::memberCount),
            QuerySort.desc("member_count"),
            QuerySort.desc(Channel::lastMessageAt).ascByName("created_at"),
            QuerySort()
        )
    }
}
