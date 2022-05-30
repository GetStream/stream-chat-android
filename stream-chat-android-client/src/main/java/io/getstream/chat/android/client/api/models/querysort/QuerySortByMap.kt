package io.getstream.chat.android.client.api.models.querysort

public class QuerySortByMap<T : QueryableByMap> : QuerySort<T> {

    private var sortSpecifications: List<SortSpecification<T>> = emptyList()

    override val comparator: Comparator<in T>
        get() = CompositeComparator(sortSpecifications.map { it.comparator })

    override fun toDto(): List<Map<String, Any>> {
        TODO("Not yet implemented")
    }

    private val SortSpecification<T>.comparator: Comparator<T>
        get() {
            return when (this.sortAttribute) {
                is SortAttribute.FieldSortAttribute<T> -> throw IllegalArgumentException(
                    "FieldSortAttribute can't be used with QuerySortByMap"
                )

                is SortAttribute.FieldNameSortAttribute<T> -> this.sortAttribute.name.comparator(this.sortDirection)
            }
        }

    private fun String.comparator(sortDirection: SortDirection): Comparator<T> =
        Comparator { o1, o2 ->
            val fieldOne = o1.toMap()[this] as? Comparable<Any>
            val fieldTwo = o2.toMap()[this] as? Comparable<Any>

            compare(fieldOne, fieldTwo, sortDirection)
        }

    override fun hashCode(): Int {
        return sortSpecifications.hashCode()
    }

    override fun toString(): String {
        return sortSpecifications.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuerySortByMap<*>

        if (sortSpecifications != other.sortSpecifications) return false

        return true
    }

    private fun add(sortSpecification: SortSpecification<T>): QuerySortByMap<T> {
        sortSpecifications = sortSpecifications + sortSpecification
        return this
    }

    public fun asc(fieldName: String): QuerySortByMap<T> {
        return add(SortSpecification(SortAttribute.FieldNameSortAttribute(fieldName), SortDirection.ASC))
    }

    public fun desc(fieldName: String): QuerySortByMap<T> {
        return add(SortSpecification(SortAttribute.FieldNameSortAttribute(fieldName), SortDirection.DESC))
    }

    public fun toList(): List<Pair<String, SortDirection>> =
        sortSpecifications.map { it.sortAttribute.name to it.sortDirection }

    public companion object {
        public fun <R : QueryableByMap> ascByName(fieldName: String): QuerySortByMap<R> =
            QuerySortByMap<R>().asc(fieldName)

        public fun <R : QueryableByMap> descByName(fieldName: String): QuerySortByMap<R> =
            QuerySortByMap<R>().desc(fieldName)
    }
}
