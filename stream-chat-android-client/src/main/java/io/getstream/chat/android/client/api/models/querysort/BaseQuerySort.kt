package io.getstream.chat.android.client.api.models.querysort

import io.getstream.chat.android.client.api.models.querysort.internal.CompositeComparator
import io.getstream.chat.android.client.api.models.querysort.internal.SortAttribute
import io.getstream.chat.android.client.api.models.querysort.internal.SortSpecification

public abstract class BaseQuerySort<T : Any> : QuerySorter<T> {

    internal var sortSpecifications: List<SortSpecification<T>> = emptyList()

    override val comparator: Comparator<in T>
        get() = CompositeComparator(sortSpecifications.map { it.comparator })

    private val SortSpecification<T>.comparator: Comparator<T>
        get() {
            return when (this.sortAttribute) {
                is SortAttribute.FieldSortAttribute<T> -> comparatorFromFieldSort(this.sortAttribute, sortDirection)

                is SortAttribute.FieldNameSortAttribute<T> ->
                    comparatorFromNameAttribute(this.sortAttribute, sortDirection)
            }
        }


    public abstract fun comparatorFromFieldSort(
        firstSort: SortAttribute.FieldSortAttribute<T>,
        sortDirection: SortDirection,
    ): Comparator<T>

    public abstract fun comparatorFromNameAttribute(
        name: SortAttribute.FieldNameSortAttribute<T>,
        sortDirection: SortDirection,
    ): Comparator<T>

    public override fun toDto(): List<Map<String, Any>> = sortSpecifications.map { sortSpec ->
        listOf(
            QuerySorter.KEY_FIELD_NAME to sortSpec.sortAttribute.name,
            QuerySorter.KEY_DIRECTION to sortSpec.sortDirection.value
        ).toMap()
    }

    override fun hashCode(): Int = sortSpecifications.hashCode()

    override fun toString(): String = sortSpecifications.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseQuerySort<*>

        if (sortSpecifications != other.sortSpecifications) return false

        return true
    }
}
