package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.extensions.snakeToLowerCamelCase
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KProperty1

public class QuerySort<T> {
    public var sortSpecifications: List<FieldSortSpecification<T>> = emptyList()
        private set
    /*private fun add(fieldName: String, direction: Int): QuerySort {
        val map = mutableMapOf<String, Any>()
        map["field"] = fieldName
        map["direction"] = direction
        data.add(map)
        return this
    }

    public fun asc(field: String): QuerySort {
        return add(field, ASC)
    }

    public fun desc(field: String): QuerySort {
        return add(field, DESC)
    }*/

    private fun add(sortSpecification: FieldSortSpecification<T>): QuerySort<T> {
        sortSpecifications = sortSpecifications + sortSpecification
        return this
    }

    public fun asc(field: KProperty1<T, out Comparable<*>?>): QuerySort<T> {
        return add(FieldSortSpecification(field, SortDirection.ASC))
    }

    public fun desc(field: KProperty1<T, out Comparable<*>?>): QuerySort<T> {
        return add(FieldSortSpecification(field, SortDirection.DESC))
    }

    public fun asc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return asc(getField(fieldName, javaClass))
    }

    public fun desc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return desc(getField(fieldName, javaClass))
    }

    public fun toMap(): Map<String, Any> = sortSpecifications.flatMap { sortSpec ->
        listOf(KEY_FIELD_NAME to sortSpec.field.name, KEY_DIRECTION to sortSpec.sortDirection.value)
    }.toMap()

    private fun getField(fieldName: String, javaClass: Class<T>): KProperty1<T, Comparable<*>> {
        val kClass = Reflection.createKotlinClass(javaClass)
        return kClass.members.filterIsInstance<KProperty1<T, Comparable<*>>>()
            .firstOrNull { it.name == fieldName.snakeToLowerCamelCase() }
            ?: throw IllegalArgumentException("Couldn't find field with name \"$fieldName\" in class ${kClass.simpleName}")
    }

    public data class FieldSortSpecification<T>(
        val field: KProperty1<T, out Comparable<*>?>,
        val sortDirection: SortDirection
    )

    public companion object {
        private const val DESC = -1
        private const val ASC = 1
        public const val KEY_DIRECTION: String = "direction"
        public const val KEY_FIELD_NAME: String = "field"

        public inline fun <reified T> QuerySort<T>.asc(fieldName: String): QuerySort<T> = asc(fieldName, T::class.java)
        public inline fun <reified T> QuerySort<T>.desc(fieldName: String): QuerySort<T> = desc(fieldName, T::class.java)
    }

    public enum class SortDirection(public val value: Int) {
        DESC(-1), ASC(1)
    }
}
