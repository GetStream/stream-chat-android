package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.socket.InitConnectionListener

class TestInitListener : InitConnectionListener() {

    private var data: ConnectionData? = null
    private var error: ChatError? = null

    fun onSuccessIsCalled(): Boolean {
        return data != null
    }

    fun onErrorIsCalled(): Boolean {
        return error != null
    }

    override fun onSuccess(data: ConnectionData) {
        this.data = data
    }

    override fun onError(error: ChatError) {
        this.error = error
    }
}
