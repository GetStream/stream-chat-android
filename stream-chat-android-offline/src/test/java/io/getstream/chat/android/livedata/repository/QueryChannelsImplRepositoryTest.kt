package io.getstream.chat.android.livedata.repository

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.randomQueryChannelsSpec
import io.getstream.chat.android.livedata.repository.domain.queryChannels.QueryChannelsDao
import io.getstream.chat.android.livedata.repository.domain.queryChannels.QueryChannelsRepositoryImpl
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
                sort = QuerySort.Companion.desc(
                    Channel::cid
                )
            )
        )

        verify(dao).insert(
            argThat {
                this.query.cids == listOf("cid1", "cid2") &&
                    sortInnerEntities.any { it.name == "cid" && it.direction == QuerySort.SortDirection.DESC.value }
            }
        )
    }
}
