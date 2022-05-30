package io.getstream.chat.android.client.api.models.querysort

import kotlin.reflect.KProperty1

/** Inner representation of sorting feature specification. */
public sealed class SortAttribute<T> {
    /** Name of attribute */
    public abstract val name: String

    /** KProperty referenced attribute. */
    public data class FieldSortAttribute<T>(val field: KProperty1<T, Comparable<*>?>, override val name: String) :
        SortAttribute<T>()

    /** Referenced by name attribute. */
    public data class FieldNameSortAttribute<T>(override val name: String) : SortAttribute<T>()
}
