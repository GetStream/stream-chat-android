package io.getstream.chat.android.core.poc.library

interface ClientConnectionCallback {
    fun onSuccess(user: User?)
    /**
     *
     * @param errMsg Human readable message
     * @param errCode http status code or [ClientErrorCode]
     */
    fun onError(errMsg: String?, errCode: Int)
}
