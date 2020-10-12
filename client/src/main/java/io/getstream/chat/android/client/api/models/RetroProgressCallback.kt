package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.ProgressCallback
import retrofit2.Call
import retrofit2.Callback

internal class RetroProgressCallback(val callback: ProgressCallback) : Callback<UploadFileResponse> {

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
