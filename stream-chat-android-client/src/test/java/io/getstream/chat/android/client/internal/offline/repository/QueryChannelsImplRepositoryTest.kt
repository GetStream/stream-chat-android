/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.internal.offline.repository

import io.getstream.chat.android.client.internal.offline.randomQueryChannelsEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.queryChannels.internal.DatabaseQueryChannelsRepository
import io.getstream.chat.android.client.internal.offline.repository.domain.queryChannels.internal.QueryChannelsDao
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.test.randomQueryChannelsSpec
import io.getstream.chat.android.models.ContainsFilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class QueryChannelsImplRepositoryTest {
    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    private lateinit var dao: QueryChannelsDao
    private lateinit var sut: DatabaseQueryChannelsRepository

    @BeforeEach
    fun before() {
        dao = mock()
        sut = DatabaseQueryChannelsRepository(dao)
    }

    @Test
    fun `When insert Should insert to DB`() = runTest {
        sut.insertQueryChannels(
            randomQueryChannelsSpec(
                cids = setOf("cid1", "cid2"),
            ),
        )

        verify(dao).insert(
            argThat {
                this.cids == listOf("cid1", "cid2")
            },
        )
    }

    @Test
    fun `Given query channels spec in DB When select by id Should return not null result`() = runTest {
        val identifier = QueryChannelsIdentifier.Standard(
            filter = Filters.contains("cid", "cid1"),
            sort = QuerySortByField(),
        )
        whenever(dao.select(any())) doReturn randomQueryChannelsEntity(
            id = "id1",
            filter = Filters.contains("cid", "cid1"),
            querySort = QuerySortByField(),
            cids = listOf("cid1"),
        )

        val result = sut.selectBy(identifier)

        result.shouldNotBeNull()
        result.filter.shouldBeInstanceOf<ContainsFilterObject>()
        (result.filter as ContainsFilterObject).fieldName shouldBeEqualTo "cid"
        (result.filter as ContainsFilterObject).value shouldBeEqualTo "cid1"
        result.cids shouldBeEqualTo setOf("cid1")
    }

    @Test
    fun `Given no row in DB with such id When select by id Should return null`() = runTest {
        whenever(dao.select(any())) doReturn null

        val result = sut.selectBy(QueryChannelsIdentifier.Standard(NeutralFilterObject, QuerySortByField()))

        result.shouldBeNull()
    }

    @Test
    fun `Two Predefined identifiers with same name but different filterValues produce different DB ids`() = runTest {
        val identifierA = QueryChannelsIdentifier.Predefined("p", mapOf("a" to 1), null)
        val identifierB = QueryChannelsIdentifier.Predefined("p", mapOf("a" to 2), null)

        sut.selectBy(identifierA)
        sut.selectBy(identifierB)

        val captor = argumentCaptor<String>()
        verify(dao, times(2)).select(captor.capture())
        assertEquals(2, captor.allValues.size)
        assertNotEquals(captor.allValues[0], captor.allValues[1])
    }

    @Test
    fun `selectBy with Predefined identifier round-trips predefined fields from the entity`() = runTest {
        val identifier = QueryChannelsIdentifier.Predefined("p", mapOf("a" to 1), mapOf("b" to 2))
        whenever(dao.select(any())) doReturn randomQueryChannelsEntity(
            id = "id-predefined",
            filter = NeutralFilterObject,
            querySort = QuerySortByField(),
            cids = listOf("cid1"),
            predefinedFilterName = "p",
            predefinedFilterValues = mapOf("a" to 1),
            predefinedSortValues = mapOf("b" to 2),
        )

        val spec = sut.selectBy(identifier)

        spec.shouldNotBeNull()
        assertEquals("p", spec.predefinedFilterName)
        assertEquals(mapOf("a" to 1), spec.predefinedFilterValues)
        assertEquals(mapOf("b" to 2), spec.predefinedSortValues)
    }

    @Test
    fun `selectBy groupKey looks up the row under the grouped DB id`() = runTest {
        whenever(dao.select(any())) doReturn randomQueryChannelsEntity(
            id = "grp:direct",
            cids = listOf("cid1"),
            groupKey = "direct",
        )

        val spec = sut.selectBy(QueryChannelsIdentifier.Grouped("direct"))

        spec.shouldNotBeNull()
        assertEquals("direct", spec.groupKey)
        assertEquals(setOf("cid1"), spec.cids)
        verify(dao).select("grp:direct")
    }

    @Test
    fun `selectBy groupKey returns null when no row exists`() = runTest {
        whenever(dao.select(any())) doReturn null

        val spec = sut.selectBy(QueryChannelsIdentifier.Grouped("direct"))

        spec.shouldBeNull()
    }

    @Test
    fun `Two Grouped identifiers with different groupKeys produce different DB ids`() = runTest {
        sut.selectBy(QueryChannelsIdentifier.Grouped("direct"))
        sut.selectBy(QueryChannelsIdentifier.Grouped("support"))

        val captor = argumentCaptor<String>()
        verify(dao, times(2)).select(captor.capture())
        assertEquals("grp:direct", captor.allValues[0])
        assertEquals("grp:support", captor.allValues[1])
    }
}
