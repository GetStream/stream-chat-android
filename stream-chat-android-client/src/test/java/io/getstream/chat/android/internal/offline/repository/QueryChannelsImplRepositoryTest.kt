/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.internal.offline.repository

import io.getstream.chat.android.client.test.randomQueryChannelsSpec
import io.getstream.chat.android.internal.offline.randomQueryChannelsEntity
import io.getstream.chat.android.internal.offline.repository.domain.queryChannels.internal.DatabaseQueryChannelsRepository
import io.getstream.chat.android.internal.offline.repository.domain.queryChannels.internal.QueryChannelsDao
import io.getstream.chat.android.models.ContainsFilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
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
        whenever(dao.select(any())) doReturn randomQueryChannelsEntity(
            id = "id1",
            filter = Filters.contains("cid", "cid1"),
            querySort = QuerySortByField(),
            cids = listOf("cid1"),
        )

        val result = sut.selectBy(Filters.contains("cid", "cid1"), QuerySortByField())

        result.shouldNotBeNull()
        result.filter.shouldBeInstanceOf<ContainsFilterObject>()
        (result.filter as ContainsFilterObject).fieldName shouldBeEqualTo "cid"
        (result.filter as ContainsFilterObject).value shouldBeEqualTo "cid1"
        result.cids shouldBeEqualTo setOf("cid1")
    }

    @Test
    fun `Given no row in DB with such id When select by id Should return null`() = runTest {
        whenever(dao.select(any())) doReturn null

        val result = sut.selectBy(NeutralFilterObject, QuerySortByField())

        result.shouldBeNull()
    }
}
