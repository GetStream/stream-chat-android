package io.getstream.chat.android.offline.repository

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.api.models.ContainsFilterObject
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.randomQueryChannelsEntity
import io.getstream.chat.android.offline.randomQueryChannelsSpec
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsDao
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsRepositoryImpl
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
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
                cids = setOf("cid1", "cid2"),
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

        result.shouldNotBeNull()
        result.filter.shouldBeInstanceOf<ContainsFilterObject>()
        (result.filter as ContainsFilterObject).fieldName shouldBeEqualTo "cid"
        result.filter.value shouldBeEqualTo "cid1"
        result.cids shouldBeEqualTo setOf("cid1")
    }

    @Test
    fun `Given no row in DB with such id When select by id Should return null`() = runBlockingTest {
        whenever(dao.select("id1")) doReturn null

        val result = sut.selectById("id1")

        result.shouldBeNull()
    }
}
