package io.getstream.chat.android.client.utils


data class FilterObject(var data: MutableMap<String, Any> = mutableMapOf()) {

    constructor(key: String, value: Any) : this() {
        data[key] = (normalizeValue(value))
    }

    init {
        // cleanup references to prevent serialization issues
        data = toMap()
    }

    fun put(key: String, value: Any): FilterObject {
        data[key] = normalizeValue(value)

        return this
    }

    // cleanup references to prevent serialization issues
    fun normalizeValue(value: Any): Any {
        return if (value is FilterObject) {
            (value as FilterObject).toMap()
        } else if (value is Array<*> && value.isArrayOf<FilterObject>()) {
            value.map { (it as FilterObject).toMap() }
        } else {
            return value
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun toMap(): HashMap<String, Any> {
        val data: HashMap<String, Any> = HashMap()

        for ((key, value) in this.data.entries) {
            data[key] = normalizeValue(value)
        }
        return data
    }

}
