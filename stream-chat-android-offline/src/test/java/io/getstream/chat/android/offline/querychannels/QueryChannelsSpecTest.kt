package io.getstream.chat.android.offline.querychannels

import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import org.junit.jupiter.api.Test

internal class QueryChannelsSpecTest {
    @Test
    fun `When the same FilterObject objects are specified Should generate the same ids by QueryChannelsSpec`() {
        val query = QueryChannelsSpec(
            Filters.eq(
                "type",
                "messaging"
            ),
            QuerySort()
        )
        val query2 = QueryChannelsSpec(
            Filters.eq(
                "type",
                "messaging"
            ),
            QuerySort()
        )
        val query3 = QueryChannelsSpec(
            Filters.eq(
                "type",
                "commerce"
            ),
            QuerySort()
        )
        val query4 = QueryChannelsSpec(
            Filters.eq(
                "type",
                "messaging"
            ),
            QuerySort<Channel>().asc("name")
        )
        // verify that 1 and 2 are equal
        Truth.assertThat(query2.id).isEqualTo(query.id)
        // verify that 3 and 4 are not equal to 2
        Truth.assertThat(query2.id).isNotEqualTo(query3.id)
        Truth.assertThat(query2.id).isNotEqualTo(query4.id)
    }
}
