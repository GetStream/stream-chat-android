package io.getstream.chat.android.core.poc.library

interface UploadFileCallback<RESPONSE, PROGRESS> {
    fun onSuccess(response: RESPONSE)
    fun onError(errMsg: String?, errCode: Int)
    fun onProgress(progress: PROGRESS)
}
