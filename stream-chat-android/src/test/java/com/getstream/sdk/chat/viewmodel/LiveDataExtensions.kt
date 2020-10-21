package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData

internal fun <T> LiveData<T>.observeAll(): List<T> = mutableListOf<T>().apply {
    observeForever { add(it) }
}
