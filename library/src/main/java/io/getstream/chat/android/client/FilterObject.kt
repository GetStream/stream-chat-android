package io.getstream.chat.android.client

class FilterObject {
    private var data: HashMap<String, Any>

    constructor() {
        data = HashMap()
    }

    constructor(data: HashMap<String, Any>) {
        this.data = data
    }

    constructor(key: String, v: Any) {
        data = HashMap()
        data[key] = v
    }

    fun getData(): HashMap<String, Any> {
        val data: HashMap<String, Any> = HashMap()

        for ((key, value) in this.data.entries) {


            if (value is FilterObject) {
                data[key] = value.getData()
            } else if (value is Array<*> && value.isArrayOf<FilterObject>()) {
                val listOfMaps: ArrayList<HashMap<String, Any>> = ArrayList()
                val values = value as Array<FilterObject>
                for (subVal in values) {
                    listOfMaps.add(subVal.getData())
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
