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

package io.getstream.chat.docs.java.ui.guides.realm.filter

import io.getstream.chat.android.models.AndFilterObject
import io.getstream.chat.android.models.AutocompleteFilterObject
import io.getstream.chat.android.models.ContainsFilterObject
import io.getstream.chat.android.models.DistinctFilterObject
import io.getstream.chat.android.models.EqualsFilterObject
import io.getstream.chat.android.models.ExistsFilterObject
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.GreaterThanFilterObject
import io.getstream.chat.android.models.GreaterThanOrEqualsFilterObject
import io.getstream.chat.android.models.InFilterObject
import io.getstream.chat.android.models.LessThanFilterObject
import io.getstream.chat.android.models.LessThanOrEqualsFilterObject
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.NorFilterObject
import io.getstream.chat.android.models.NotEqualsFilterObject
import io.getstream.chat.android.models.NotExistsFilterObject
import io.getstream.chat.android.models.NotInFilterObject
import io.getstream.chat.android.models.OrFilterObject
import io.getstream.chat.docs.java.ui.guides.realm.entities.FilterNode

/**
 * [Representing Filters as a graph](https://getstream.io/chat/docs/sdk/android/client/guides/replace-database/#representing-filters-as-a-graph)
 */

@Suppress("ComplexMethod")
internal fun FilterObject.toFilterNode(): FilterNode = when (this) {
    is AndFilterObject -> createCompositeFilterNode(KEY_AND, this.filterObjects.map(FilterObject::toFilterNode))
    is OrFilterObject -> createCompositeFilterNode(KEY_OR, this.filterObjects.map(FilterObject::toFilterNode))
    is NorFilterObject -> createCompositeFilterNode(KEY_NOR, this.filterObjects.map(FilterObject::toFilterNode))
    is ExistsFilterObject -> createFilterNode(KEY_EXIST, this.fieldName, null)
    is NotExistsFilterObject -> createFilterNode(KEY_NOT_EXIST, this.fieldName, null)
    is EqualsFilterObject -> createFilterNode(KEY_EQUALS, this.fieldName, this.value)
    is NotEqualsFilterObject -> createFilterNode(KEY_NOT_EQUALS, this.fieldName, this.value)
    is ContainsFilterObject -> createFilterNode(KEY_CONTAINS, this.fieldName, this.value)
    is GreaterThanFilterObject -> createFilterNode(KEY_GREATER_THAN, this.fieldName, this.value)
    is GreaterThanOrEqualsFilterObject ->
        createFilterNode(KEY_GREATER_THAN_OR_EQUALS, this.fieldName, this.value)
    is LessThanFilterObject -> createFilterNode(KEY_LESS_THAN, this.fieldName, this.value)
    is LessThanOrEqualsFilterObject -> createFilterNode(KEY_LESS_THAN_OR_EQUALS, this.fieldName, this.value)
    is InFilterObject -> createFilterNode(KEY_IN, this.fieldName, this.values)
    is NotInFilterObject -> createFilterNode(KEY_NOT_IN, this.fieldName, this.values)
    is AutocompleteFilterObject -> createFilterNode(KEY_AUTOCOMPLETE, this.fieldName, this.value)
    is DistinctFilterObject -> createFilterNode(KEY_DISTINCT, null, null)
    is NeutralFilterObject -> createFilterNode(KEY_NEUTRAL, null, null)
}

@Suppress("ComplexMethod", "SpreadOperator")
internal fun FilterNode.toFilterObject(): FilterObject = when (this.filterType) {
    KEY_AND -> Filters.and(*(this.value as Iterable<FilterNode>).map(FilterNode::toFilterObject).toTypedArray())
    KEY_OR -> Filters.or(*(this.value as Iterable<FilterNode>).map(FilterNode::toFilterObject).toTypedArray())
    KEY_NOR -> Filters.nor(*(this.value as Iterable<FilterNode>).map(FilterNode::toFilterObject).toTypedArray())
    KEY_EXIST -> this.field?.let(Filters::exists) ?: Filters.neutral()
    KEY_NOT_EXIST -> this.field?.let(Filters::notExists) ?: Filters.neutral()
    KEY_EQUALS -> Filters.eq(this.field ?: "", this.value ?: false)
    KEY_NOT_EQUALS -> Filters.ne(this.field ?: "", this.value ?: false)
    KEY_CONTAINS -> Filters.contains(this.field ?: "", this.value ?: "")
    KEY_GREATER_THAN -> Filters.greaterThan(this.field ?: "", this.value ?: "")
    KEY_GREATER_THAN_OR_EQUALS -> Filters.greaterThanEquals(this.field ?: "", this.value ?: "")
    KEY_LESS_THAN -> Filters.lessThan(this.field ?: "", this.value ?: "")
    KEY_LESS_THAN_OR_EQUALS -> Filters.lessThanEquals(this.field ?: "", this.value ?: "")
    KEY_IN -> Filters.`in`(this.field ?: "", (this.value as Iterable<out Any>).toList())
    KEY_NOT_IN -> Filters.nin(this.field ?: "", (this.value as Iterable<out Any>).toList())
    KEY_AUTOCOMPLETE -> Filters.autocomplete(this.field ?: "", this.value as String)
    else -> Filters.neutral()
}

private fun createCompositeFilterNode(filterType: String, value: Any): FilterNode =
    FilterNode(filterType = filterType).apply {
        this.value = value
    }

private fun createFilterNode(filterType: String, field: String?, value: Any?): FilterNode =
    FilterNode(filterType = filterType).apply {
        this.field = field
        this.value = value
    }

internal const val KEY_EXIST: String = "exists"
internal const val KEY_NOT_EXIST: String = "not_exists"
internal const val KEY_CONTAINS: String = "contains"
internal const val KEY_AND: String = "and"
internal const val KEY_OR: String = "or"
internal const val KEY_NOR: String = "nor"
internal const val KEY_NOT_EQUALS: String = "ne"
internal const val KEY_EQUALS: String = "equals"
internal const val KEY_GREATER_THAN: String = "gt"
internal const val KEY_GREATER_THAN_OR_EQUALS: String = "gte"
internal const val KEY_LESS_THAN: String = "lt"
internal const val KEY_LESS_THAN_OR_EQUALS: String = "lte"
internal const val KEY_IN: String = "in"
internal const val KEY_NOT_IN: String = "nin"
internal const val KEY_AUTOCOMPLETE: String = "autocomplete"
internal const val KEY_NEUTRAL: String = "neutral"
internal const val KEY_DISTINCT: String = "distinct"
