package io.getstream.chat.android.client.api.models

public sealed class FilterObject

public data class AndFilterObject internal constructor(val filterObjects: Set<FilterObject>) : FilterObject()
public data class OrFilterObject internal constructor(val filterObjects: Set<FilterObject>) : FilterObject()
public data class NorFilterObject internal constructor(val filterObjects: Set<FilterObject>) : FilterObject()
public data class ContainsFilterObject internal constructor(val fieldName: String, val value: Any) : FilterObject()
public data class AutocompleteFilterObject internal constructor(val fieldName: String, val value: String) : FilterObject()
public data class ExistsFilterObject internal constructor(val fieldName: String) : FilterObject()
public data class NotExistsFilterObject internal constructor(val fieldName: String) : FilterObject()
public data class EqualsFilterObject internal constructor(val fieldName: String, val value: Any) : FilterObject()
public data class NotEqualsFilterObject internal constructor(val fieldName: String, val value: Any) : FilterObject()
public data class GreaterThanFilterObject internal constructor(val fieldName: String, val value: Any) : FilterObject()
public data class GreaterThanOrEqualsFilterObject internal constructor(val fieldName: String, val value: Any) : FilterObject()
public data class LessThanFilterObject internal constructor(val fieldName: String, val value: Any) : FilterObject()
public data class LessThanOrEqualsFilterObject internal constructor(val fieldName: String, val value: Any) : FilterObject()
public data class InFilterObject internal constructor(val fieldName: String, val values: Set<Any>) : FilterObject()
public data class NotInFilterObject internal constructor(val fieldName: String, val values: Set<Any>) : FilterObject()
public data class DistinctFilterObject internal constructor(val memberIds: Set<String>) : FilterObject()
public object NeutralFilterObject : FilterObject()
public object NoneFilterObject : FilterObject()
