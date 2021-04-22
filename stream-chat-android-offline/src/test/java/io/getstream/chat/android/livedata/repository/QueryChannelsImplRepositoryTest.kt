package io.getstream.chat.android.livedata.repository

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.api.models.ContainsFilterObject
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.livedata.randomQueryChannelsSpec
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsDao
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsEntity
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsWithSorts
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
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
        whenever(dao.select("id1")) doReturn randomQueryChannelsWithSorts(
            id = "id1",
            filterObject = Filters.contains("cid", "cid1"),
            cids = listOf("cid1")
        )

        val result = sut.selectById("id1")

        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result!!.filter).isInstanceOf(ContainsFilterObject::class.java)
        Truth.assertThat((result.filter as ContainsFilterObject).fieldName).isEqualTo("cid")
        Truth.assertThat(result.filter.value).isEqualTo("cid1")
        Truth.assertThat(result.cids).isEqualTo(listOf("cid1"))
    }

    @Test
    fun `Given no row in DB with such id When select by id Should return null`() = runBlockingTest {
        whenever(dao.select("id1")) doReturn null

        val result = sut.selectById("id1")

        Truth.assertThat(result).isNull()
    }

    @Test
    fun `Given query channels specs in DB When select by ids Should return according results`() = runBlockingTest {
        whenever(dao.select(listOf("id1", "id2"))) doReturn listOf(
            randomQueryChannelsWithSorts(
                cids = listOf(
                    "cid1",
                    "cid2"
                )
            ),
            randomQueryChannelsWithSorts(cids = listOf("cid2", "cid3"))
        )

        val result = sut.selectQueriesChannelsByIds(listOf("id1", "id2"))

        Truth.assertThat(result.size).isEqualTo(2)
        Assert.assertTrue(result.any { it.cids == listOf("cid1", "cid2") })
        Assert.assertTrue(result.any { it.cids == listOf("cid2", "cid3") })
    }

    private fun randomQueryChannelsWithSorts(
        id: String = randomString(),
        filterObject: FilterObject = NeutralFilterObject,
        cids: List<String>,
    ): QueryChannelsWithSorts {
        val query = QueryChannelsEntity(id = id, filter = filterObject, cids = cids)
        return QueryChannelsWithSorts(query, emptyList())
    }
}
