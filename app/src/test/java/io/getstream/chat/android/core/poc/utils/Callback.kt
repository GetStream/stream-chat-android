package io.getstream.chat.android.core.poc.utils

interface Callback<T> {
    fun call(t: T)
}