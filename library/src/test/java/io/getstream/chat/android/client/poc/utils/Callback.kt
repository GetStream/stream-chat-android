package io.getstream.chat.android.client.poc.utils

interface Callback<T> {
    fun call(t: T)
}