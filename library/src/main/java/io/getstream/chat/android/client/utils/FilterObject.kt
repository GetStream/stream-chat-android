package io.getstream.chat.android.client.utils


data class FilterObject(val data: HashMap<String, Any> = HashMap()) {

    constructor(key: String, v: Any) : this() {
        data[key] = v
    }

    @Suppress("UNCHECKED_CAST")
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

}
