package io.getstream.chat.android.test

import androidx.lifecycle.LiveData

public fun <T> LiveData<T>.observeAll(): List<T> = mutableListOf<T>().apply {
    observeForever { add(it) }
}
