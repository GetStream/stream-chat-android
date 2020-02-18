package io.getstream.chat.android.client.utils

data class FilterObject(val data: HashMap<String, Any> = HashMap()) {

    constructor(key: String, v: Any) : this() {
        data[key] = v
    }

    fun getMap(): HashMap<String, Any> {
        val data: HashMap<String, Any> = HashMap()

        for ((key, value) in this.data.entries) {


            if (value is FilterObject) {
                data[key] = value.getMap()
            } else if (value is Array<*> && value.isArrayOf<FilterObject>()) {
                val listOfMaps: ArrayList<HashMap<String, Any>> = ArrayList()
                val values = value as Array<FilterObject>
                for (subVal in values) {
                    listOfMaps.add(subVal.getMap())
                }
                data[key] = listOfMaps
            } else {
                data[key] = value
            }

        }
        return data
    }

    fun put(key: String, v: Any): FilterObject {
        val clone = HashMap(data)
        clone[key] = v
        return FilterObject(clone)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilterObject

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }


}
