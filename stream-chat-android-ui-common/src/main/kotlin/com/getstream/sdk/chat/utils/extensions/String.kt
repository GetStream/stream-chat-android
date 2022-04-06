package com.getstream.sdk.chat.utils.extensions

internal fun String.containsLinks(): Boolean {
    val regex = """(?:\s|^)((?:https?:)?(?:[a-z0-9-]+(?:\.[a-z0-9-]+)+)(?::[0-9]+)?(?:(?:[^\s]+)?)?)""".toRegex()
    return this.contains(regex = regex)
}
