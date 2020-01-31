package io.getstream.chat.android.client

interface UploadFileCallback<RESPONSE, PROGRESS> {
    fun onSuccess(response: RESPONSE)
    fun onError(errMsg: String?, errCode: Int)
    fun onProgress(progress: PROGRESS)
}
