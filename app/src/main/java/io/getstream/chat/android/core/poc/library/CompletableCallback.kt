package io.getstream.chat.android.core.poc.library

interface CompletableCallback {
    fun onSuccess(response: CompletableResponse)
    fun onError(errMsg: String, errCode: Int)
}
