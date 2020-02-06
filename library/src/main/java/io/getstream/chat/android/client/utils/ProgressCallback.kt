package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError

interface ProgressCallback {
    fun onSuccess(file: String)
    fun onError(error: ChatError)
    fun onProgress(progress: Long)
}