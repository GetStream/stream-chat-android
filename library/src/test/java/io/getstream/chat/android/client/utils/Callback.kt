package io.getstream.chat.android.client.utils

interface Callback<T> {
    fun call(t: T)
}