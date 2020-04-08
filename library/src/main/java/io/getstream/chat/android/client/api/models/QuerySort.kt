package io.getstream.chat.android.client.api.models


data class QuerySort(val data: MutableList<Map<String, Any>> = mutableListOf()) {

    private fun add(fieldName: String, direction: Int): QuerySort {
        val map = mutableMapOf<String, Any>()
        map["field"] = fieldName
        map["direction"] = direction
        data.add(map)
        return this
    }

    fun asc(field: String): QuerySort {
        return add(field, ASC)
    }

    fun desc(field: String): QuerySort {
        return add(field, DESC)
    }

    companion object {
        private const val DESC = -1
        private const val ASC = 1
    }
}



