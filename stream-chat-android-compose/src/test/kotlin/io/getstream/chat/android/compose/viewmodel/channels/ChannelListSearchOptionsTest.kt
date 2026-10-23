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

package io.getstream.chat.android.compose.viewmodel.channels

import io.getstream.chat.android.compose.viewmodel.channels.ChannelListSearchOptions.ChannelSearchParams
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListSearchOptions.MessageSearchParams
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ChannelListSearchOptionsTest {

    private val options = ChannelListSearchOptions()
    private val query = randomString()
    private val currentUser = randomUser()

    @Test
    fun `Given no list filter When building the channel filter Should match name and membership`() {
        val filter = options.channelFilter(channelParams(listFilter = null))

        assertEquals(nameAndMembership(), filter)
    }

    @Test
    fun `Given an AND list filter When building the channel filter Should add the search to its conditions`() {
        val typeFilter = Filters.eq("type", "messaging")
        val frozenFilter = Filters.eq("frozen", false)

        val filter = options.channelFilter(channelParams(listFilter = Filters.and(typeFilter, frozenFilter)))

        assertEquals(Filters.and(typeFilter, frozenFilter, nameAndMembership()), filter)
    }

    @Test
    fun `Given a list filter that is not AND When building the channel filter Should combine both with AND`() {
        val listFilter = Filters.eq("type", "messaging")

        val filter = options.channelFilter(channelParams(listFilter = listFilter))

        assertEquals(Filters.and(listFilter, nameAndMembership()), filter)
    }

    @Test
    fun `Given no current user When building the channel filter Should match an empty member id`() {
        val filter = options.channelFilter(channelParams(listFilter = null, currentUser = null))

        val expected = Filters.and(Filters.autocomplete("name", query), Filters.`in`("members", ""))
        assertEquals(expected, filter)
    }

    @Test
    fun `Given a list sort When building the channel sort Should use it`() {
        val listSort = QuerySortByField.descByName<Channel>("last_updated")

        val sort = options.channelSort(channelParams(listSort = listSort))

        assertEquals(listSort, sort)
    }

    @Test
    fun `Given no list sort When building the channel sort Should use an empty sort`() {
        val sort = options.channelSort(channelParams(listSort = null))

        assertEquals(QuerySortByField<Channel>(), sort)
    }

    @Test
    fun `Given a list filter When building the message channel filter Should use it`() {
        val listFilter = Filters.eq("type", "messaging")

        val filter = options.messageChannelFilter(messageParams(listFilter = listFilter))

        assertEquals(listFilter, filter)
    }

    @Test
    fun `Given no list filter When building the message channel filter Should match membership`() {
        val filter = options.messageChannelFilter(messageParams(listFilter = null))

        assertEquals(Filters.`in`("members", listOf(currentUser.id)), filter)
    }

    @Test
    fun `Given no list filter and no current user When building the message channel filter Should skip the search`() {
        val filter = options.messageChannelFilter(messageParams(listFilter = null, currentUser = null))

        assertNull(filter)
    }

    @Test
    fun `When building the message filter Should autocomplete the text`() {
        val filter = options.messageFilter(messageParams(listFilter = null))

        assertEquals(Filters.autocomplete("text", query), filter)
    }

    private fun nameAndMembership() = Filters.and(
        Filters.autocomplete("name", query),
        Filters.`in`("members", currentUser.id),
    )

    private fun channelParams(
        listFilter: FilterObject? = null,
        listSort: QuerySortByField<Channel>? = null,
        currentUser: User? = this.currentUser,
    ) = ChannelSearchParams(query = query, currentUser = currentUser, listFilter = listFilter, listSort = listSort)

    private fun messageParams(
        listFilter: FilterObject?,
        currentUser: User? = this.currentUser,
    ) = MessageSearchParams(query = query, currentUser = currentUser, listFilter = listFilter)
}
