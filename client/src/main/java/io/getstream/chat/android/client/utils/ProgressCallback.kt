package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError

public interface ProgressCallback {
    public fun onSuccess(file: String)
    public fun onError(error: ChatError)
    public fun onProgress(progress: Long)
}
