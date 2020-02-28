package io.getstream.chat.android.client.utils


internal fun <K, V> Map<K, V>.containsKeys(vararg keys: K): Boolean {
    for (it in keys)
        if (!this.containsKey(it)) return false
    return true
}

fun isNullOrEmpty(vararg strings: String?): Boolean {
    strings.forEach { s ->
        if (s.isNullOrEmpty()) return false
    }
    return true
}