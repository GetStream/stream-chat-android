package io.getstream.chat.android.client.rest

import io.getstream.chat.android.client.ProgressCallback
import io.getstream.chat.android.client.UploadFileResponse
import io.getstream.chat.android.client.errors.ChatError
import retrofit2.Call
import retrofit2.Callback

class RetroProgressCallback(val callback: ProgressCallback) : Callback<UploadFileResponse> {

    override fun onFailure(call: Call<UploadFileResponse>, t: Throwable) {
        callback.onError(ChatError(t))
    }

    override fun onResponse(
        call: Call<UploadFileResponse>,
        response: retrofit2.Response<UploadFileResponse>
    ) {
        val body = response.body()
        if (body == null) {
            onFailure(call, RuntimeException("file response is null"))
        } else {
            val file = body.file
            callback.onSuccess(file)
        }
    }

}