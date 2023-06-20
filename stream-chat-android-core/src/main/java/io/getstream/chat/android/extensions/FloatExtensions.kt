package io.getstream.chat.android.extensions

public fun Float.limitTo(min: Float, max: Float): Float {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}