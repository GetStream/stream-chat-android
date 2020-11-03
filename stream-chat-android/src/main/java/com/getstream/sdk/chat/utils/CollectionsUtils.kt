package com.getstream.sdk.chat.utils

// Use when it is known that the element is always close to the end
internal fun <T> List<T>.reverseFind(predicate: (T) -> Boolean): T? {
    for (i in (this.size - 1) downTo 0) {
        if (predicate(this[i])) {
            return this[i]
        }
    }

    return null
}

// Use when it is known that the element is always close to the end
internal fun <T> List<T>.reverseIndexOf(element: T): Int {
    for (i in (this.size - 1) downTo 0) {
        if (this[i] == element) {
            return i
        }
    }

    return -1
}

// Use when it is known that the element is always close to the end
internal fun <T> List<T>.reverseIndexOf(element: T, biPredicate: (T, T) -> Boolean): Int {
    for (i in (this.size - 1) downTo 0) {
        if (biPredicate(this[i], element)) {
            return i
        }
    }

    return -1
}
