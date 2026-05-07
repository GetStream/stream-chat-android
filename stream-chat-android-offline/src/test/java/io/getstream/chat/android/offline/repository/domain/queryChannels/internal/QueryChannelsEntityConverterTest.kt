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

package io.getstream.chat.android.offline.repository.domain.queryChannels.internal

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.ascByName
import io.getstream.chat.android.offline.createRoomDB
import io.getstream.chat.android.offline.randomQueryChannelsEntity
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the round-trip serialization of [QueryChannelsEntity] through Room. Two converter
 * scopes are exercised together:
 *  - **Database scope** — [filter] uses `FilterObjectConverter`, [querySort] uses
 *    `QuerySortConverter`, [cids] uses `ListConverter`.
 *  - **Entity scope (overrides DB)** — [predefinedFilterValues] and [predefinedSortValues] use
 *    [NullableMapConverter] so `null` round-trips as `null` (rather than being collapsed to an
 *    empty map by the DB-level `ExtraDataConverter`).
 *
 * If Room ever picked the wrong converter for a column (e.g. `NullableMapConverter` for `filter`,
 * or `ExtraDataConverter` for `predefinedFilterValues`), these tests would fail.
 */
@RunWith(AndroidJUnit4::class)
internal class QueryChannelsEntityConverterTest {

    private lateinit var database: ChatDatabase
    private lateinit var dao: QueryChannelsDao

    @Before
    fun setUp() {
        database = createRoomDB()
        dao = database.queryChannelsDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `null predefined value maps round-trip as null`() = runTest {
        val entity = randomQueryChannelsEntity(
            predefinedFilterValues = null,
            predefinedSortValues = null,
        )

        dao.insert(entity)
        val read = dao.select(entity.id)

        assertNull(read?.predefinedFilterValues)
        assertNull(read?.predefinedSortValues)
    }

    @Test
    fun `empty predefined value maps round-trip as empty maps`() = runTest {
        val entity = randomQueryChannelsEntity(
            predefinedFilterValues = emptyMap(),
            predefinedSortValues = emptyMap(),
        )

        dao.insert(entity)
        val read = dao.select(entity.id)

        assertEquals(emptyMap<String, Any>(), read?.predefinedFilterValues)
        assertEquals(emptyMap<String, Any>(), read?.predefinedSortValues)
    }

    @Test
    fun `populated predefined value maps round-trip with values preserved`() = runTest {
        val filterValues = mapOf<String, Any>("status" to "active", "score" to 7.0)
        val sortValues = mapOf<String, Any>("direction" to "desc")
        val entity = randomQueryChannelsEntity(
            predefinedFilterValues = filterValues,
            predefinedSortValues = sortValues,
        )

        dao.insert(entity)
        val read = dao.select(entity.id)

        assertEquals(filterValues, read?.predefinedFilterValues)
        assertEquals(sortValues, read?.predefinedSortValues)
    }

    @Test
    fun `filter and querySort round-trip via their dedicated DB-level converters`() = runTest {
        // Non-trivial filter and sort. NullableMapConverter cannot serialise these types — only
        // FilterObjectConverter and QuerySortConverter can — so a successful round-trip proves
        // Room dispatches each column to the correct converter.
        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.contains("members", "user-1"),
        )
        val querySort = QuerySortByField.descByName<Channel>("last_message_at")
            .ascByName("created_at")
        val entity = randomQueryChannelsEntity(
            filter = filter,
            querySort = querySort,
            cids = listOf("messaging:cid-1", "messaging:cid-2"),
        )

        dao.insert(entity)
        val read = dao.select(entity.id)

        assertNotNull(read)
        assertEquals(filter, read?.filter)
        assertEquals(querySort, read?.querySort)
        assertEquals(listOf("messaging:cid-1", "messaging:cid-2"), read?.cids)
    }

    @Test
    fun `standard and predefined columns round-trip together with their distinct converters`() = runTest {
        // Combine a non-trivial filter/sort (handled by DB-level converters) with non-null
        // predefined value maps (handled by the entity-scoped NullableMapConverter). All four
        // must survive the round-trip independently — which can only happen if Room picked
        // FilterObjectConverter for filter, QuerySortConverter for querySort, and
        // NullableMapConverter (not ExtraDataConverter) for the two map columns.
        val filter = Filters.eq("type", "team")
        val querySort = QuerySortByField.ascByName<Channel>("name")
        val predefinedFilterValues = mapOf<String, Any>("status" to "active")
        val predefinedSortValues = mapOf<String, Any>("direction" to "desc")
        val entity = randomQueryChannelsEntity(
            filter = filter,
            querySort = querySort,
            predefinedFilterName = "my-filter",
            predefinedFilterValues = predefinedFilterValues,
            predefinedSortValues = predefinedSortValues,
        )

        dao.insert(entity)
        val read = dao.select(entity.id)

        assertNotNull(read)
        assertEquals(filter, read?.filter)
        assertEquals(querySort, read?.querySort)
        assertEquals("my-filter", read?.predefinedFilterName)
        assertEquals(predefinedFilterValues, read?.predefinedFilterValues)
        assertEquals(predefinedSortValues, read?.predefinedSortValues)
    }

    @Test
    fun `null and empty are distinguishable after round-trip`() = runTest {
        val nullEntity = randomQueryChannelsEntity(
            predefinedFilterValues = null,
            predefinedSortValues = null,
        )
        val emptyEntity = randomQueryChannelsEntity(
            predefinedFilterValues = emptyMap(),
            predefinedSortValues = emptyMap(),
        )

        dao.insert(nullEntity)
        dao.insert(emptyEntity)

        val nullRead = dao.select(nullEntity.id)
        val emptyRead = dao.select(emptyEntity.id)

        assertNull(nullRead?.predefinedFilterValues)
        assertNull(nullRead?.predefinedSortValues)
        assertEquals(emptyMap<String, Any>(), emptyRead?.predefinedFilterValues)
        assertEquals(emptyMap<String, Any>(), emptyRead?.predefinedSortValues)
    }
}
