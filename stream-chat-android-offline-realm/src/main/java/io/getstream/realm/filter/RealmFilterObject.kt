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

import io.getstream.chat.android.client.api.models.AndFilterObject
import io.getstream.chat.android.client.api.models.AutocompleteFilterObject
import io.getstream.chat.android.client.api.models.ContainsFilterObject
import io.getstream.chat.android.client.api.models.DistinctFilterObject
import io.getstream.chat.android.client.api.models.EqualsFilterObject
import io.getstream.chat.android.client.api.models.ExistsFilterObject
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.GreaterThanFilterObject
import io.getstream.chat.android.client.api.models.GreaterThanOrEqualsFilterObject
import io.getstream.chat.android.client.api.models.InFilterObject
import io.getstream.chat.android.client.api.models.LessThanFilterObject
import io.getstream.chat.android.client.api.models.LessThanOrEqualsFilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.NorFilterObject
import io.getstream.chat.android.client.api.models.NotEqualsFilterObject
import io.getstream.chat.android.client.api.models.NotExistsFilterObject
import io.getstream.chat.android.client.api.models.NotInFilterObject
import io.getstream.chat.android.client.api.models.OrFilterObject
import io.getstream.chat.android.client.models.Filters
import io.getstream.realm.entity.FilterNode

@Suppress("ComplexMethod")
internal fun FilterObject.toFilterNode(): FilterNode = when (this) {
    is AndFilterObject -> createBooleanLogicFilterNode(
        KEY_AND,
        this.filterObjects.map(FilterObject::toFilterNode)
    )
    is OrFilterObject -> createBooleanLogicFilterNode(KEY_OR, this.filterObjects.map(FilterObject::toFilterNode))
    is NorFilterObject -> createBooleanLogicFilterNode(
        KEY_NOR,
        this.filterObjects.map(FilterObject::toFilterNode)
    )
    is ExistsFilterObject -> createFilterNodeEntity(KEY_EXIST, this.fieldName, null)
    is NotExistsFilterObject -> createFilterNodeEntity(KEY_NOT_EXIST, this.fieldName, null)
    is EqualsFilterObject -> createFilterNodeEntity(KEY_EQUALS, this.fieldName, this.value)
    is NotEqualsFilterObject -> createFilterNodeEntity(KEY_NOT_EQUALS, this.fieldName, this.value)
    is ContainsFilterObject -> createFilterNodeEntity(KEY_CONTAINS, this.fieldName, this.value)
    is GreaterThanFilterObject -> createFilterNodeEntity(KEY_GREATER_THAN, this.fieldName, this.value)
    is GreaterThanOrEqualsFilterObject ->
        createFilterNodeEntity(KEY_GREATER_THAN_OR_EQUALS, this.fieldName, this.value)
    is LessThanFilterObject -> createFilterNodeEntity(KEY_LESS_THAN, this.fieldName, this.value)
    is LessThanOrEqualsFilterObject -> createFilterNodeEntity(KEY_LESS_THAN_OR_EQUALS, this.fieldName, this.value)
    is InFilterObject -> createFilterNodeEntity(KEY_IN, this.fieldName, this.values.toSet())
    is NotInFilterObject -> createFilterNodeEntity(KEY_NOT_IN, this.fieldName, this.values.toSet())
    is AutocompleteFilterObject -> createFilterNodeEntity(KEY_AUTOCOMPLETE, this.fieldName, this.value)
    is DistinctFilterObject -> createFilterNodeEntity(null, null, null)
    is NeutralFilterObject -> createFilterNodeEntity(KEY_NEUTRAL, null, null)
}

@Suppress("ComplexMethod")
internal fun FilterNode.toFilterObject(): FilterObject = when (this.filterType) {
    KEY_AND -> Filters.and((this.value as Set<FilterNode>).map(FilterNode::toFilterObject))
    KEY_OR -> Filters.or((this.value as Set<FilterNode>).map(FilterNode::toFilterObject))
    KEY_NOR -> Filters.nor((this.value as Set<FilterNode>).map(FilterNode::toFilterObject))
    KEY_EXIST -> this.field?.let(Filters::exists) ?: Filters.neutral()
    KEY_NOT_EXIST -> this.field?.let(Filters::notExists) ?: Filters.neutral()
    KEY_EQUALS -> Filters.eq(this.field ?: "", this.value ?: false)
    KEY_NOT_EQUALS -> Filters.ne(this.field ?: "", this.value ?: false)
    KEY_CONTAINS -> Filters.contains(this.field ?: "", this.value ?: "")
    KEY_GREATER_THAN -> Filters.greaterThan(this.field ?: "", this.value ?: "")
    KEY_GREATER_THAN_OR_EQUALS -> Filters.greaterThanEquals(this.field ?: "", this.value ?: "")
    KEY_LESS_THAN -> Filters.lessThan(this.field ?: "", this.value ?: "")
    KEY_LESS_THAN_OR_EQUALS -> Filters.lessThanEquals(this.field ?: "", this.value ?: "")
    KEY_IN -> Filters.`in`(this.field ?: "", (this.value as Set<out Any>))
    KEY_NOT_IN -> Filters.nin(this.field ?: "", (this.value as Set<out Any>))
    KEY_AUTOCOMPLETE -> Filters.autocomplete(this.field ?: "", this.value as String)
    else -> Filters.neutral()
}

private fun createBooleanLogicFilterNode(filterType: String?, value: Any): FilterNode =
    FilterNode().apply {
        this.filterType = filterType
        this.value = value
    }

private fun createFilterNodeEntity(filterType: String?, field: String?, value: Any?): FilterNode =
    FilterNode().apply {
        this.filterType = filterType
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
internal const val KEY_MEMBERS: String = "members"
