package io.getstream.chat.android.client.utils


data class FilterObject(val data: HashMap<String, Any> = HashMap()) {

    constructor(key: String, value: Any) : this() {
        data[key] = value
    }

    fun put(key: String, value: Any): FilterObject {
        data[key] = value
        return this
    }

    @Suppress("UNCHECKED_CAST")
    internal fun toMap(): HashMap<String, Any> {
        val data: HashMap<String, Any> = HashMap()

        for ((key, value) in this.data.entries) {


            if (value is FilterObject) {
                data[key] = value.toMap()
            } else if (value is Array<*> && value.isArrayOf<FilterObject>()) {
                val listOfMaps: ArrayList<HashMap<String, Any>> = ArrayList()
                val values = value as Array<FilterObject>
                for (subVal in values) {
                    listOfMaps.add(subVal.toMap())
                }
                data[key] = listOfMaps
            } else {
                data[key] = value
            }

        }
        return data
    }

}
