package io.getstream.chat.android.client.token

import androidx.annotation.WorkerThread

public interface TokenProvider {
    @WorkerThread
    public fun loadToken(): String
}
