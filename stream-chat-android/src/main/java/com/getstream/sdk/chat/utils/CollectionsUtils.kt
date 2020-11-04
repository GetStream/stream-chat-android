package com.getstream.sdk.chat.utils

// Use when it is known that the element is always close to the end
internal fun <T> List<T>.lastIndexOfBiPredicate(element: T, biPredicate: (T, T) -> Boolean): Int {
    for (i in (this.size - 1) downTo 0) {
        if (biPredicate(this[i], element)) {
            return i
        }
    }

    return -1
}
