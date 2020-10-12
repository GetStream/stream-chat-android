package io.getstream.chat.android.client.utils

public class FilterObject(key: String, value: Any) {

    private var data: MutableMap<String, Any> = mutableMapOf()

    init {
        data[key] = (normalizeValue(value))
    }

    init {
        // cleanup references to prevent serialization issues
        data = toMap()
    }

    public fun put(key: String, value: Any): FilterObject {
        data[key] = normalizeValue(value)

        return this
    }

    // cleanup references to prevent serialization issues
    private fun normalizeValue(value: Any): Any {
        return if (value is FilterObject) {
            value.toMap()
        } else if (value is Array<*> && value.isArrayOf<FilterObject>()) {
            value.map { (it as FilterObject).toMap() }
        } else {
            return value
        }
    }

    @Suppress("UNCHECKED_CAST")
    public fun toMap(): HashMap<String, Any> {
        val data: HashMap<String, Any> = HashMap()

        for ((key, value) in this.data.entries) {
            data[key] = normalizeValue(value)
        }
        return data
    }
}
