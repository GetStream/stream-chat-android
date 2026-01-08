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

package io.getstream.chat.android.models

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class FiltersTest {

    @Test
    fun testFiltersNeutral() {
        val filter = Filters.neutral()
        filter `should be equal to` NeutralFilterObject
    }

    @Test
    fun testFiltersExists() {
        val filter = Filters.exists("fieldName")
        filter `should be equal to` ExistsFilterObject("fieldName")
    }

    @Test
    fun testFiltersNotExists() {
        val filter = Filters.notExists("fieldName")
        filter `should be equal to` NotExistsFilterObject("fieldName")
    }

    @Test
    fun testFiltersContains() {
        val filter = Filters.contains("fieldName", "value")
        filter `should be equal to` ContainsFilterObject("fieldName", "value")
    }

    @Test
    fun testFiltersAnd() {
        val filter1 = Filters.eq("fieldName1", "value1")
        val filter2 = Filters.eq("fieldName2", "value2")
        val andFilter = Filters.and(filter1, filter2)
        andFilter `should be equal to` AndFilterObject(setOf(filter1, filter2))
    }

    @Test
    fun testFiltersOr() {
        val filter1 = Filters.eq("fieldName1", "value1")
        val filter2 = Filters.eq("fieldName2", "value2")
        val orFilter = Filters.or(filter1, filter2)
        orFilter `should be equal to` OrFilterObject(setOf(filter1, filter2))
    }

    @Test
    fun testFiltersNor() {
        val filter1 = Filters.eq("fieldName1", "value1")
        val filter2 = Filters.eq("fieldName2", "value2")
        val norFilter = Filters.nor(filter1, filter2)
        norFilter `should be equal to` NorFilterObject(setOf(filter1, filter2))
    }

    @Test
    fun testFiltersEq() {
        val filter = Filters.eq("fieldName", "value")
        filter `should be equal to` EqualsFilterObject("fieldName", "value")
    }

    @Test
    fun testFiltersNe() {
        val filter = Filters.ne("fieldName", "value")
        filter `should be equal to` NotEqualsFilterObject("fieldName", "value")
    }

    @Test
    fun testFiltersGreaterThan() {
        val filter = Filters.greaterThan("fieldName", "value")
        filter `should be equal to` GreaterThanFilterObject("fieldName", "value")
    }

    @Test
    fun testFiltersGreaterThanOrEquals() {
        val filter = Filters.greaterThanEquals("fieldName", "value")
        filter `should be equal to` GreaterThanOrEqualsFilterObject("fieldName", "value")
    }

    @Test
    fun testFiltersLessThan() {
        val filter = Filters.lessThan("fieldName", "value")
        filter `should be equal to` LessThanFilterObject("fieldName", "value")
    }

    @Test
    fun testFiltersLessThanOrEquals() {
        val filter = Filters.lessThanEquals("fieldName", "value")
        filter `should be equal to` LessThanOrEqualsFilterObject("fieldName", "value")
    }

    @Test
    fun testFiltersInStringArray() {
        val filter = Filters.`in`("fieldName", "value1", "value2")
        filter `should be equal to` InFilterObject("fieldName", setOf("value1", "value2"))
    }

    @Test
    fun testFiltersInList() {
        val filter = Filters.`in`("fieldName", listOf("value1", "value2"))
        filter `should be equal to` InFilterObject("fieldName", setOf("value1", "value2"))
    }

    @Test
    fun testFiltersInNumberArray() {
        val filter = Filters.`in`("fieldName", 1, 2)
        filter `should be equal to` InFilterObject("fieldName", setOf(1, 2))
    }

    @Test
    fun testFiltersAutocomplete() {
        val filter = Filters.autocomplete("fieldName", "value")
        filter `should be equal to` AutocompleteFilterObject("fieldName", "value")
    }

    @Test
    fun testFiltersDistinct() {
        val filter = Filters.distinct(listOf("memberId1", "memberId2"))
        filter `should be equal to` DistinctFilterObject(setOf("memberId1", "memberId2"))
    }
}
