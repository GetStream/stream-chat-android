package io.getstream.chat.android.core.poc.library.requests

sealed class Direction(val value: Int) {

    object ASC : Direction(1)
    object DESC : Direction(-1)

    override fun toString(): String {
        return value.toString()
    }
}