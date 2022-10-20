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
import io.realm.kotlin.ext.toRealmSet
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet

internal class FilterNode : RealmObject {
    var filter_type: String? = null
    var field: String? = null
    var value: Any? = null
}

internal fun FilterObject.toFilterNode(): FilterNode = when (this) {
    is AndFilterObject -> createBooleanLogicFilterNode(KEY_AND, this.filterObjects.map(FilterObject::toFilterNode))
    is OrFilterObject -> createBooleanLogicFilterNode(KEY_OR, this.filterObjects.map(FilterObject::toFilterNode))
    is NorFilterObject -> createBooleanLogicFilterNode(KEY_NOR, this.filterObjects.map(FilterObject::toFilterNode))
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
    is InFilterObject -> createFilterNode(KEY_IN, this.fieldName, this.values.toRealmSet())
    is NotInFilterObject -> createFilterNode(KEY_NOT_IN, this.fieldName, this.values.toRealmSet())
    is AutocompleteFilterObject -> createFilterNode(KEY_AUTOCOMPLETE, this.fieldName, this.value)
    is DistinctFilterObject -> createFilterNode(null, null, null)
    is NeutralFilterObject -> createFilterNode(null, null, null)
}

internal fun FilterNode.toFilterObject(): FilterObject = when (this.filter_type) {
    KEY_AND -> Filters.and((this.value as List<FilterNode>).map(FilterNode::toFilterObject))
    KEY_OR -> Filters.or((this.value as List<FilterNode>).map(FilterNode::toFilterObject))
    KEY_NOR -> Filters.nor((this.value as List<FilterNode>).map(FilterNode::toFilterObject))
    KEY_EXIST -> this.field?.let(Filters::exists) ?: Filters.neutral()
    KEY_NOT_EXIST -> this.field?.let(Filters::notExists) ?: Filters.neutral()
    KEY_EQUALS -> Filters.eq(this.field!!, this.value!!)
    KEY_NOT_EQUALS -> Filters.ne(this.field!!, this.value!!)
    KEY_CONTAINS -> Filters.contains(this.field!!, this.value!!)
    KEY_GREATER_THAN -> Filters.greaterThan(this.field!!, this.value!!)
    KEY_GREATER_THAN_OR_EQUALS -> Filters.greaterThanEquals(this.field!!, this.value!!)
    KEY_LESS_THAN -> Filters.lessThan(this.field!!, this.value!!)
    KEY_LESS_THAN_OR_EQUALS -> Filters.lessThanEquals(this.field!!, this.value!!)
    KEY_IN -> Filters.`in`(this.field!!, (this.value as RealmSet<Any>).toList())
    KEY_NOT_IN -> Filters.nin(this.field!!, (this.value as RealmSet<Any>).toList())
    KEY_AUTOCOMPLETE -> Filters.autocomplete(this.field!!, this.value as String)
    else -> Filters.neutral()
}

private fun createBooleanLogicFilterNode(filterType: String?, value: Any): FilterNode =
    FilterNode().apply {
        this.filter_type = filterType
        this.value = value
    }

private fun createFilterNode(filterType: String?, field: String?, value: Any?): FilterNode =
    FilterNode().apply {
        this.filter_type = filterType
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
internal const val KEY_DISTINCT: String = "distinct"
internal const val KEY_MEMBERS: String = "members"
