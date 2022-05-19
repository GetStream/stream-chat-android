package io.getstream.chat.android.client.socket

/**
 * A wrapper that contains timestamp along with the [value].
 */
internal data class Timed<T>(val value: T, val time: Long)