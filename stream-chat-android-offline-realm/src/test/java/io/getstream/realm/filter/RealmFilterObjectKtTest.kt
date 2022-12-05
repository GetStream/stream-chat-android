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

package io.getstream.realm.filter

import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.realm.entity.FilterNode
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.Test

internal class RealmFilterObjectKtTest {

    @Test
    fun `it should be possible to convert a empty filter`() {
        val emptyFilerEntity = FilterNode().apply {
            filterType = ""
            field = ""
            value = ""
        }

        emptyFilerEntity.toFilterObject() `should be instance of` NeutralFilterObject::class
    }

    @Test
    fun `it should be possible to convert a filter object to realm`() {
        val filterObject = Filters.and(
            Filters.eq("name", "leandro"),
            Filters.exists("age")
        ).toFilterNode()

        filterObject.filterType `should be equal to` KEY_AND
    }

    @Test
    fun `it should be possible to convert a filter IN object to realm`() {
        val field = "field"
        val args = listOf("value1", "value2", "value3")
        val originalFilter = Filters.`in`(field, args)
        val filterEntity = originalFilter.toFilterNode()

        filterEntity.filterType `should be equal to` KEY_IN

        filterEntity.toFilterObject() `should be equal to` originalFilter
    }

    @Test
    fun `it should be possible to convert and revert a filter object to realm`() {
        val filterObject = Filters.and(
            Filters.eq("name", "leandro"),
            Filters.exists("age")
        )

        val newFilter = filterObject.toFilterNode().toFilterObject()

        newFilter `should be equal to` filterObject
    }

    @Test
    fun `it should be possible to convert and revert a filter object to realm - complex scenario`() {
        val filterObject = Filters.and(
            Filters.eq("name", "leandro"),
            Filters.or(
                Filters.greaterThan("age", "18"),
                Filters.contains("profession", "programmer")
            ),
            Filters.ne("something", "something")
        )

        val newFilter = filterObject.toFilterNode().toFilterObject()

        newFilter `should be equal to` filterObject
    }

    @Test
    fun `it should be possible to convert and revert a filter object to realm - complex scenario2`() {
        val filterObject = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", listOf("user_id")),
            Filters.or(Filters.notExists("draft"), Filters.eq("draft", false)),
        )

        val newFilter = filterObject.toFilterNode().toFilterObject()

        newFilter `should be equal to` filterObject
    }
}
