package io.getstream.chat.android.livedata.repository

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.api.models.ContainsFilterObject
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.livedata.randomQueryChannelsSpec
import io.getstream.chat.android.livedata.repository.domain.queryChannels.QueryChannelsDao
import io.getstream.chat.android.livedata.repository.domain.queryChannels.QueryChannelsEntity
import io.getstream.chat.android.livedata.repository.domain.queryChannels.QueryChannelsRepositoryImpl
import io.getstream.chat.android.livedata.repository.domain.queryChannels.QueryChannelsWithSorts
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class QueryChannelsImplRepositoryTest {

    private lateinit var dao: QueryChannelsDao
    private lateinit var sut: QueryChannelsRepositoryImpl

    @BeforeEach
    fun before() {
        dao = mock()
        sut = QueryChannelsRepositoryImpl(dao)
    }

    @Test
    fun `When insert Should insert to DB`() = runBlockingTest {
        sut.insertQueryChannels(
            randomQueryChannelsSpec(
                cids = listOf("cid1", "cid2"),
                sort = QuerySort.Companion.desc(Channel::cid)
            )
        )

        verify(dao).insert(
            argThat {
                this.query.cids == listOf("cid1", "cid2") &&
                    sortInnerEntities.any { it.name == "cid" && it.direction == QuerySort.SortDirection.DESC.value }
            }
        )
    }

    @Test
    fun `Given query channels spec in DB When select by id Should return not null result`() = runBlockingTest {
        whenever(dao.select("id1")) doReturn randomQueryChannelsWithSorts(id = "id1", filterObject = Filters.contains("cid", "cid1"), cids = listOf("cid1"))

        val result = sut.selectById("id1")

        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result!!.filter).isInstanceOf(ContainsFilterObject::class.java)
        Truth.assertThat((result.filter as ContainsFilterObject).fieldName).isEqualTo("cid")
        Truth.assertThat(result.filter.value).isEqualTo("cid1")
        Truth.assertThat(result.cids).isEqualTo(listOf("cid1"))
    }

    private fun randomQueryChannelsWithSorts(id: String, filterObject: FilterObject, cids: List<String>): QueryChannelsWithSorts {
        val query = QueryChannelsEntity(id = id, filter = filterObject, cids = cids)
        return QueryChannelsWithSorts(query, emptyList())
    }
}
