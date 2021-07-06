package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.api.models.QuerySort.SortAttribute.FieldSortAttribute
import io.getstream.chat.android.client.extensions.camelCaseToSnakeCase
import io.getstream.chat.android.client.extensions.snakeToLowerCamelCase
import io.getstream.chat.android.client.models.CustomObject
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

public class QuerySort<T : Any> {
    private var sortSpecifications: List<SortSpecification<T>> = emptyList()

    public val comparator: Comparator<in T>
        get() = CompositeComparator(sortSpecifications.mapNotNull { it.comparator })

    private val SortSpecification<T>.comparator: Comparator<T>?
        get() {
            return when (this.sortAttribute) {
                is FieldSortAttribute<T> -> this.sortAttribute.field?.comparator(this.sortDirection)
                is SortAttribute.FieldNameSortAttribute -> this.sortAttribute.name.comparator(this.sortDirection)
            }
        }

    @Suppress("UNCHECKED_CAST")
    private fun KProperty1<T, Comparable<*>?>.comparator(sortDirection: SortDirection): Comparator<T>? =
        this.let { compareProperty ->
            Comparator { c0, c1 ->
                compare(
                    (compareProperty.getter.call(c0) as? Comparable<Any>),
                    (compareProperty.getter.call(c1) as? Comparable<Any>),
                    sortDirection
                )
            }
        }

    @Suppress("UNCHECKED_CAST")
    private fun String.comparator(sortDirection: SortDirection): Comparator<T> =
        Comparator { o1, o2 ->
            compare(
                (o1.getMemberPropertyOrExtra(this) as? Comparable<Any>),
                (o2.getMemberPropertyOrExtra(this) as? Comparable<Any>),
                sortDirection
            )
        }

    private fun compare(first: Comparable<Any>?, second: Comparable<Any>?, sortDirection: SortDirection): Int {
        return when {
            first == null && second == null -> EQUAL_ON_COMPARISON
            first == null && second != null -> LESS_ON_COMPARISON * sortDirection.value
            first != null && second == null -> MORE_ON_COMPARISON * sortDirection.value
            first != null && second != null -> first.compareTo(second) * sortDirection.value
            else -> error("Impossible case!")
        }
    }

    private fun Any.getMemberPropertyOrExtra(name: String): Any? =
        name.snakeToLowerCamelCase().let { fieldName ->
            this::class.memberProperties
                .firstOrNull { it.name == fieldName }
                ?.getter
                ?.call(this)
                ?: (this as? CustomObject)?.extraData?.get(name)
        }

    private fun add(sortSpecification: SortSpecification<T>): QuerySort<T> {
        sortSpecifications = sortSpecifications + sortSpecification
        return this
    }

    public fun asc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> {
        return add(SortSpecification(FieldSortAttribute(field, field.name.camelCaseToSnakeCase()), SortDirection.ASC))
    }

    public fun desc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> {
        return add(SortSpecification(FieldSortAttribute(field, field.name.camelCaseToSnakeCase()), SortDirection.DESC))
    }

    public fun asc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.ASC))
    }

    public fun desc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.DESC))
    }

    public fun asc(fieldName: String): QuerySort<T> {
        return add(SortSpecification(SortAttribute.FieldNameSortAttribute(fieldName), SortDirection.ASC))
    }

    public fun desc(fieldName: String): QuerySort<T> {
        return add(SortSpecification(SortAttribute.FieldNameSortAttribute(fieldName), SortDirection.DESC))
    }

    public fun asc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.ASC))
    }

    public fun desc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.DESC))
    }

    public fun toDto(): List<Map<String, Any>> = sortSpecifications.map { sortSpec ->
        listOf(KEY_FIELD_NAME to sortSpec.sortAttribute.name, KEY_DIRECTION to sortSpec.sortDirection.value).toMap()
    }

    public fun toList(): List<Pair<String, SortDirection>> =
        sortSpecifications.map { it.sortAttribute.name to it.sortDirection }

    private fun getSortFeature(fieldName: String, javaClass: Class<T>): SortAttribute<T> {
        @Suppress("UNCHECKED_CAST")
        val kClass = Reflection.createKotlinClass(javaClass) as KClass<T>
        return getSortFeature(fieldName, kClass)
    }

    private fun getSortFeature(fieldName: String, kClass: KClass<T>): SortAttribute<T> {
        return kClass.members.filterIsInstance<KProperty1<T, Comparable<*>?>>()
            .firstOrNull { it.name == fieldName.snakeToLowerCamelCase() }?.let { FieldSortAttribute(it, fieldName) }
            ?: SortAttribute.FieldNameSortAttribute(fieldName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuerySort<*>

        if (sortSpecifications != other.sortSpecifications) return false

        return true
    }

    override fun hashCode(): Int {
        return sortSpecifications.hashCode()
    }

    private data class SortSpecification<T>(
        val sortAttribute: SortAttribute<T>,
        val sortDirection: SortDirection,
    )

    private sealed class SortAttribute<T> {
        abstract val name: String

        data class FieldSortAttribute<T>(val field: KProperty1<T, Comparable<*>?>, override val name: String) :
            SortAttribute<T>()

        data class FieldNameSortAttribute<T>(override val name: String) : SortAttribute<T>()
    }

    public enum class SortDirection(public val value: Int) {
        DESC(-1), ASC(1)
    }

    internal class CompositeComparator<T>(private val comparators: List<Comparator<T>>) : Comparator<T> {
        override fun compare(o1: T, o2: T): Int =
            comparators.fold(EQUAL_ON_COMPARISON) { currentComparisonValue, comparator ->
                when (currentComparisonValue) {
                    EQUAL_ON_COMPARISON -> comparator.compare(o1, o2)
                    else -> currentComparisonValue
                }
            }
    }

    public companion object {
        public const val KEY_DIRECTION: String = "direction"
        public const val KEY_FIELD_NAME: String = "field"
        private const val MORE_ON_COMPARISON = 1
        private const val EQUAL_ON_COMPARISON = 0
        private const val LESS_ON_COMPARISON = 1

        public inline fun <reified T : Any> QuerySort<T>.ascByName(fieldName: String): QuerySort<T> =
            asc(fieldName, T::class)

        public inline fun <reified T : Any> QuerySort<T>.descByName(fieldName: String): QuerySort<T> =
            desc(fieldName, T::class)

        public inline fun <reified T : Any> asc(fieldName: String): QuerySort<T> = QuerySort<T>().ascByName(fieldName)
        public inline fun <reified T : Any> desc(fieldName: String): QuerySort<T> = QuerySort<T>().descByName(fieldName)
        public fun <T : Any> asc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> = QuerySort<T>().asc(field)
        public fun <T : Any> desc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> = QuerySort<T>().desc(field)
    }
}
