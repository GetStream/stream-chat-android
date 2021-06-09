package io.getstream.chat.android.offline.repository

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
import io.getstream.chat.android.offline.randomQueryChannelsEntity
import io.getstream.chat.android.offline.randomQueryChannelsSpec
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsDao
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsEntity
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsRepositoryImpl
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
            )
        )

        verify(dao).insert(
            argThat {
                this.cids == listOf("cid1", "cid2")
            }
        )
    }

    @Test
    fun `Given query channels spec in DB When select by id Should return not null result`() = runBlockingTest {
        whenever(dao.select("id1")) doReturn randomQueryChannelsEntity(
            id = "id1",
            filter = Filters.contains("cid", "cid1"),
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
}
