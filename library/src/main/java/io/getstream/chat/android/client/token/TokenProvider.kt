package io.getstream.chat.android.client.token

import androidx.annotation.WorkerThread

interface TokenProvider {
    @WorkerThread
    fun loadToken(): String
}