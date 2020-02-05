package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.utils.FilterObject

object Filters {
    fun and(vararg filters: FilterObject): FilterObject {
        return FilterObject(
            "\$and",
            filters
        )
    }

    fun or(vararg filters: FilterObject): FilterObject {
        return FilterObject(
            "\$or",
            filters
        )
    }

    fun nor(vararg filters: FilterObject): FilterObject {
        return FilterObject(
            "\$nor",
            filters
        )
    }

    fun eq(field: String, value: Any): FilterObject {
        return FilterObject(field, value)
    }

    fun ne(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$ne", value)
        )
    }

    fun greaterThan(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$gt", value)
        )
    }

    fun greaterThanEquals(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$gte", value)
        )
    }

    fun lessThan(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$lt", value)
        )
    }

    fun lessThanEquals(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$lte", value)
        )
    }

    fun `in`(field: String, vararg values: String): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$in", values)
        )
    }

    fun `in`(field: String, values: List<*>): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$in", values)
        )
    }

    fun `in`(field: String, vararg values: Number): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$in", values)
        )
    }

    fun nin(field: String, vararg values: String): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$nin", values)
        )
    }

    fun nin(field: String, values: List<*>): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$nin", values)
        )
    }

    fun nin(field: String, vararg values: Number): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$nin", values)
        )
    }
}
