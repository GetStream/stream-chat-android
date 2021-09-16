package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.models.Filters
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.Test

internal class QueryChannelsSpecTest {
    @Test
    fun `When the same FilterObject objects are specified Should generate the same ids by QueryChannelsSpec`() {
        val query = QueryChannelsSpec(
            Filters.eq(
                "type",
                "messaging"
            ),
        )
        val query2 = QueryChannelsSpec(
            Filters.eq(
                "type",
                "messaging"
            ),
        )
        val query3 = QueryChannelsSpec(
            Filters.eq(
                "type",
                "commerce"
            ),
        )
        // verify that 1 and 2 are equal
        query2.id shouldBeEqualTo query.id
        // verify that 3 is not equal to 2
        query2.id shouldNotBeEqualTo query3.id
    }
}
