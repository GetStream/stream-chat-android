package io.getstream.chat.android.client.utils

internal fun <K, V> Map<K, V>.containsKeys(vararg keys: K): Boolean {
    for (it in keys)
        if (!this.containsKey(it)) return false
    return true
}

internal fun isNullOrEmpty(vararg strings: String?): Boolean {
    strings.forEach { s ->
        if (s.isNullOrEmpty()) return false
    }
    return true
}

internal fun <K, V> Map<K, V>.getOr(key: K, default: V): V {
    return if (containsKey(key)) {
        get(key)!!
    } else {
        default
    }
}
