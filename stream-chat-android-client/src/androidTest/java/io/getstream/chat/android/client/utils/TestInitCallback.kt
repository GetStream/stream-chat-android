package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.ConnectionData

internal class TestInitCallback : Call.Callback<ConnectionData> {
    private var data: ConnectionData? = null
    private var error: ChatError? = null

    fun onSuccessIsCalled(): Boolean {
        return data != null
    }

    fun onErrorIsCalled(): Boolean {
        return error != null
    }

    override fun onResult(result: Result<ConnectionData>) {
        if (result.isSuccess) {
            data = result.data()
        } else {
            error = result.error()
        }
    }
}
