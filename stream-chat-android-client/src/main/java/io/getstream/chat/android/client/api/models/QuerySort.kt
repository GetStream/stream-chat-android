package io.getstream.chat.android.client.api.models

public data class QuerySort(
    public val data: MutableList<Map<String, Any>> = mutableListOf()
) {

    private fun add(fieldName: String, direction: Int): QuerySort {
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
    }

    private companion object {
        const val DESC = -1
        const val ASC = 1
    }
}
