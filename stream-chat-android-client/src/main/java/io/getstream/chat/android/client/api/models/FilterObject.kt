package io.getstream.chat.android.client.api.models

public sealed class FilterObject

public data class AndFilterObject(val filterObjects: List<FilterObject>) : FilterObject()

public data class OrFilterObject(val filterObjects: List<FilterObject>) : FilterObject()

public data class NorFilterObject(val filterObjects: List<FilterObject>) : FilterObject()

public data class ContainsFilterObject(val fieldName: String, val value: Any) : FilterObject()

public data class AutocompleteFilterObject(val fieldName: String, val value: String) : FilterObject()

public data class ExistsFilterObject(val fieldName: String) : FilterObject()

public data class NonExistsFilterObject(val fieldName: String) : FilterObject()

public data class EqualsFilterObject(val fieldName: String, val value: Any) : FilterObject()

public data class NotEqualsFilterObject(val fieldName: String, val value: Any) : FilterObject()

public data class GreaterThanFilterObject(val fieldName: String, val value: Any) : FilterObject()

public data class GreaterThanOrEqualsFilterObject(val fieldName: String, val value: Any) : FilterObject()

public data class LessThanFilterObject(val fieldName: String, val value: Any) : FilterObject()

public data class LessThanOrEqualsFilterObject(val fieldName: String, val value: Any) : FilterObject()

public data class InFilterObject(val fieldName: String, val values: List<Any>) : FilterObject()

public data class NotInFilterObject(val fieldName: String, val values: List<Any>) : FilterObject()

public data class DistinctFilterObject(val membeerIds: List<String>) : FilterObject()

public object NeutralFilterObject : FilterObject()
