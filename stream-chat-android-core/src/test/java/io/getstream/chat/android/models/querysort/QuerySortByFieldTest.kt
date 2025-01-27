/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.models.querysort

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.ascByName
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.descByName
import io.getstream.chat.android.models.querysort.internal.SortAttribute
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class QuerySortByFieldTest {

    @Test
    fun `When using QuerySortByField, when calling comparatorFromFieldSort, should throw exception`() {
        // given
        val querySortByField = QuerySortByField<Channel>()
        // when / then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            querySortByField.comparatorFromFieldSort(
                firstSort = SortAttribute.FieldSortAttribute(
                    field = Channel::memberCount,
                    name = Channel::memberCount.name,
                ),
                sortDirection = SortDirection.ASC,
            )
        }
    }

    @Test
    fun `When using QuerySortByField, when calling comparatorFromNameAttribute, should return comparator`() {
        // given
        val querySortByField = QuerySortByField<Channel>()
        // when
        val comparator = querySortByField.comparatorFromNameAttribute(
            name = SortAttribute.FieldNameSortAttribute(Channel::memberCount.name),
            sortDirection = SortDirection.ASC,
        )
        // then
        comparator `should be instance of` Comparator::class
    }

    @Test
    fun `When creating QuerySortByField via ascByName, Then correct object is created`() {
        // given
        val querySort = QuerySortByField.ascByName<Channel>("created_at")
        // then
        val specifications = querySort.sortSpecifications
        specifications.size `should be equal to` 1
        specifications[0].sortAttribute `should be instance of` SortAttribute.FieldNameSortAttribute::class
        specifications[0].sortDirection `should be equal to` SortDirection.ASC
    }

    @Test
    fun `When creating QuerySortByField via descByName, Then correct object is created`() {
        // given
        val querySort = QuerySortByField.descByName<Channel>("created_at")
        // then
        val specifications = querySort.sortSpecifications
        specifications.size `should be equal to` 1
        specifications[0].sortAttribute `should be instance of` SortAttribute.FieldNameSortAttribute::class
        specifications[0].sortDirection `should be equal to` SortDirection.DESC
    }

    @Test
    fun `When creating QuerySortByField via ascByName extension, Then correct object is created`() {
        // given
        val querySort = QuerySortByField<Channel>().ascByName("created_at")
        // then
        val specifications = querySort.sortSpecifications
        specifications.size `should be equal to` 1
        specifications[0].sortAttribute `should be instance of` SortAttribute.FieldNameSortAttribute::class
        specifications[0].sortDirection `should be equal to` SortDirection.ASC
    }

    @Test
    fun `When creating QuerySortByField via descByName extension, Then correct object is created`() {
        // given
        val querySort = QuerySortByField<Channel>().descByName("created_at")
        // then
        val specifications = querySort.sortSpecifications
        specifications.size `should be equal to` 1
        specifications[0].sortAttribute `should be instance of` SortAttribute.FieldNameSortAttribute::class
        specifications[0].sortDirection `should be equal to` SortDirection.DESC
    }

    @Test
    fun `When using QuerySortByField asc, Then objects are sorted in ascending order`() {
        // given
        val channel1 = Channel(id = "cid1", name = "Channel 1")
        val channel2 = Channel(id = "cid2", name = "Channel 2")
        val channelsToSort = listOf(channel2, channel1)
        val querySort = QuerySortByField<Channel>().asc("name")
        // when
        val sortedChannels = channelsToSort.sortedWith(querySort.comparator)
        // then
        sortedChannels `should be equal to` listOf(channel1, channel2)
    }

    @Test
    fun `When using QuerySortByField desc, Then objects are sorted in descending order`() {
        // given
        val channel1 = Channel(id = "cid1", name = "Channel 1")
        val channel2 = Channel(id = "cid2", name = "Channel 2")
        val channelsToSort = listOf(channel1, channel2)
        val querySort = QuerySortByField<Channel>().desc("name")
        // when
        val sortedChannels = channelsToSort.sortedWith(querySort.comparator)
        // then
        sortedChannels `should be equal to` listOf(channel2, channel1)
    }
}
