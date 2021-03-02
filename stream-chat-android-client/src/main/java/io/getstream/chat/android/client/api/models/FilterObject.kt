package io.getstream.chat.android.client.api.models

internal sealed class FilterObject

internal data class AndFilterObject(val filterObjects: List<FilterObject>) : FilterObject()

internal data class OrFilterObject(val filterObjects: List<FilterObject>) : FilterObject()

internal data class NorFilterObject(val filterObjects: List<FilterObject>) : FilterObject()

internal data class ContainsFilterObject(val fieldName: String, val value: Any) : FilterObject()

internal data class AutocompleteFilterObject(val fieldName: String, val value: String) : FilterObject()

internal data class ExistsFilterObject(val fieldName: String) : FilterObject()

internal data class NonExistsFilterObject(val fieldName: String) : FilterObject()

internal data class EqualsFilterObject(val fieldName: String, val value: Any) : FilterObject()

internal data class NotEqualsFilterObject(val fieldName: String, val value: Any) : FilterObject()

internal data class GreaterThanFilterObject(val fieldName: String, val value: Any) : FilterObject()

internal data class GreaterThanOrEqualsFilterObject(val fieldName: String, val value: Any) : FilterObject()

internal data class LessThanFilterObject(val fieldName: String, val value: Any) : FilterObject()

internal data class LessThanOrEqualsFilterObject(val fieldName: String, val value: Any) : FilterObject()

internal data class InFilterObject(val fieldName: String, val values: List<Any>) : FilterObject()

internal data class NotInFilterObject(val fieldName: String, val values: List<Any>) : FilterObject()
