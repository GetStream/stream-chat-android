package io.getstream.chat.android.client

interface CompletableCallback {
    fun onSuccess(response: CompletableResponse)
    fun onError(errMsg: String, errCode: Int)
}
