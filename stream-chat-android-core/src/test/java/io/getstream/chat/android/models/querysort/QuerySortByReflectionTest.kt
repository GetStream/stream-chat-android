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

package io.getstream.chat.android.models.querysort

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.querysort.QuerySortByReflection.Companion.asc
import io.getstream.chat.android.models.querysort.QuerySortByReflection.Companion.desc
import io.getstream.chat.android.models.querysort.internal.SortAttribute
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.Test

internal class QuerySortByReflectionTest {

    @Test
    fun `When creating QuerySortByReflection via asc(fieldName), Then correct object is created`() {
        // given
        val querySort = asc<Channel>("name")
        // then
        val specifications = querySort.sortSpecifications
        specifications.size `should be equal to` 1
        specifications[0].sortAttribute `should be instance of` SortAttribute.FieldSortAttribute::class
        val fieldSortAttribute = specifications[0].sortAttribute as SortAttribute.FieldSortAttribute
        fieldSortAttribute.field.name `should be equal to` "name"
        specifications[0].sortDirection `should be equal to` SortDirection.ASC
    }

    @Test
    fun `When creating QuerySortByReflection via desc(fieldName), Then correct object is created`() {
        // given
        val querySort = desc<Channel>("name")
        // then
        val specifications = querySort.sortSpecifications
        specifications.size `should be equal to` 1
        specifications[0].sortAttribute `should be instance of` SortAttribute.FieldSortAttribute::class
        val fieldSortAttribute = specifications[0].sortAttribute as SortAttribute.FieldSortAttribute
        fieldSortAttribute.field.name `should be equal to` "name"
        specifications[0].sortDirection `should be equal to` SortDirection.DESC
    }

    @Test
    fun `When creating QuerySortByReflection via asc(field), Then correct object is created`() {
        // given
        val querySort = asc(Channel::name)
        // then
        val specifications = querySort.sortSpecifications
        specifications.size `should be equal to` 1
        specifications[0].sortAttribute `should be instance of` SortAttribute.FieldSortAttribute::class
        val fieldSortAttribute = specifications[0].sortAttribute as SortAttribute.FieldSortAttribute
        fieldSortAttribute.field.name `should be equal to` "name"
        specifications[0].sortDirection `should be equal to` SortDirection.ASC
    }

    @Test
    fun `When creating QuerySortByReflection via desc(field), Then correct object is created`() {
        // given
        val querySort = desc(Channel::name)
        // then
        val specifications = querySort.sortSpecifications
        specifications.size `should be equal to` 1
        specifications[0].sortAttribute `should be instance of` SortAttribute.FieldSortAttribute::class
        val fieldSortAttribute = specifications[0].sortAttribute as SortAttribute.FieldSortAttribute
        fieldSortAttribute.field.name `should be equal to` "name"
        specifications[0].sortDirection `should be equal to` SortDirection.DESC
    }

    @Test
    fun `When creating QuerySortByReflection by custom field asc, Then the sortSpecifications are updated`() {
        // given
        val querySort = QuerySortByReflection<Channel>()
        // when
        querySort.asc("some_custom_field")
        // then
        val specifications = querySort.sortSpecifications
        specifications.size `should be equal to` 1
        specifications[0].sortAttribute `should be instance of` SortAttribute.FieldNameSortAttribute::class
        val sortAttribute = specifications[0].sortAttribute as SortAttribute.FieldNameSortAttribute
        sortAttribute.name `should be equal to` "some_custom_field"
        specifications[0].sortDirection `should be equal to` SortDirection.ASC
    }

    @Test
    fun `When creating QuerySortByReflection by custom field desc, Then the sortSpecifications are updated`() {
        // given
        val querySort = QuerySortByReflection<Channel>()
        // when
        querySort.desc("some_custom_field")
        // then
        val specifications = querySort.sortSpecifications
        specifications.size `should be equal to` 1
        specifications[0].sortAttribute `should be instance of` SortAttribute.FieldNameSortAttribute::class
        val sortAttribute = specifications[0].sortAttribute as SortAttribute.FieldNameSortAttribute
        sortAttribute.name `should be equal to` "some_custom_field"
        specifications[0].sortDirection `should be equal to` SortDirection.DESC
    }

    @Test
    fun `When using QuerySortByReflection asc(fieldName), Then objects are sorted in ascending order`() {
        // given
        val channel1 = Channel(id = "cid1", name = "Channel 1")
        val channel2 = Channel(id = "cid2", name = "Channel 2")
        val channelsToSort = listOf(channel2, channel1)
        val querySort = asc<Channel>("name")
        // when
        val sortedChannels = channelsToSort.sortedWith(querySort.comparator)
        // then
        sortedChannels `should be equal to` listOf(channel1, channel2)
    }

    @Test
    fun `When using QuerySortByReflection desc(fieldName), Then objects are sorted in descending order`() {
        // given
        val channel1 = Channel(id = "cid1", name = "Channel 1")
        val channel2 = Channel(id = "cid2", name = "Channel 2")
        val channelsToSort = listOf(channel1, channel2)
        val querySort = desc<Channel>("name")
        // when
        val sortedChannels = channelsToSort.sortedWith(querySort.comparator)
        // then
        sortedChannels `should be equal to` listOf(channel2, channel1)
    }

    @Test
    fun `When using QuerySortByReflection asc(field), Then objects are sorted in ascending order`() {
        // given
        val channel1 = Channel(id = "cid1", name = "Channel 1")
        val channel2 = Channel(id = "cid2", name = "Channel 2")
        val channelsToSort = listOf(channel2, channel1)
        val querySort = asc(Channel::name)
        // when
        val sortedChannels = channelsToSort.sortedWith(querySort.comparator)
        // then
        sortedChannels `should be equal to` listOf(channel1, channel2)
    }

    @Test
    fun `When using QuerySortByReflection desc(field), Then objects are sorted in descending order`() {
        // given
        val channel1 = Channel(id = "cid1", name = "Channel 1")
        val channel2 = Channel(id = "cid2", name = "Channel 2")
        val channelsToSort = listOf(channel1, channel2)
        val querySort = desc(Channel::name)
        // when
        val sortedChannels = channelsToSort.sortedWith(querySort.comparator)
        // then
        sortedChannels `should be equal to` listOf(channel2, channel1)
    }

    @Test
    fun `When using QuerySortByReflection asc by custom field, Then objects are sorted in ascending order`() {
        // given
        val channel1 = Channel(id = "cid1", name = "Channel 1", extraData = mutableMapOf("sort_order" to 2))
        val channel2 = Channel(id = "cid2", name = "Channel 2", extraData = mutableMapOf("sort_order" to 1))
        val channelsToSort = listOf(channel1, channel2)
        val querySort = QuerySortByReflection<Channel>().asc("sort_order")
        // when
        val sortedChannels = channelsToSort.sortedWith(querySort.comparator)
        // then
        sortedChannels `should be equal to` listOf(channel2, channel1)
    }

    @Test
    fun `When using QuerySortByReflection desc by custom field, Then objects are sorted in descending order`() {
        // given
        val channel1 = Channel(id = "cid1", name = "Channel 1", extraData = mutableMapOf("sort_order" to 1))
        val channel2 = Channel(id = "cid2", name = "Channel 2", extraData = mutableMapOf("sort_order" to 2))
        val channelsToSort = listOf(channel1, channel2)
        val querySort = QuerySortByReflection<Channel>().desc("sort_order")
        // when
        val sortedChannels = channelsToSort.sortedWith(querySort.comparator)
        // then
        sortedChannels `should be equal to` listOf(channel2, channel1)
    }

    @Test
    fun `When using QuerySortByReflection by multiple field, Then object are sorted in correct order`() {
        // given
        val channel1 = Channel(id = "cid1", name = "Channel 1", extraData = mutableMapOf("importance" to 1))
        val channel2 = Channel(id = "cid2", name = "Channel 2", extraData = mutableMapOf("importance" to 1))
        val channel3 = Channel(id = "cid3", name = "Channel 2", extraData = mutableMapOf("importance" to 2))
        val channelsToSort = listOf(channel1, channel2, channel3)
        val querySort = QuerySortByReflection<Channel>()
            .desc("name")
            .asc("importance")
        // when
        val sortedChannels = channelsToSort.sortedWith(querySort.comparator)
        // then
        sortedChannels `should be equal to` listOf(channel2, channel3, channel1)
    }
}
