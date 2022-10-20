package io.getstream.realm.entity

import io.realm.kotlin.types.RealmObject

internal class FilterNodeEntity: RealmObject {
    var filter_type: String? = null
    var field: String? = null
    var value: Any? = null
}
