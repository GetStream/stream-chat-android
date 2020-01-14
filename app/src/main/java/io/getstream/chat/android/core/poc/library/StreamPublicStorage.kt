package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.socket.ErrorResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import java.io.File


class StreamPublicStorage(
    val client: StreamChatClient,
    val mCDNService: RetrofitApi
) : BaseStorage() {

    override fun sendFile(
        channel: Channel,
        file: File,
        mimeType: String,
        callback: UploadFileCallback<File, Int>
    ) {
        val fileReqBody = ProgressRequestBody(file, mimeType, callback)
        val part =
            MultipartBody.Part.createFormData("file", file.name, fileReqBody)

        val callbackWrapper: Callback<UploadFileResponse> =
            object : Callback<UploadFileResponse> {
                override fun onFailure(call: Call<UploadFileResponse>, t: Throwable) {
                    if (t is ErrorResponse) {
                        callback.onError(t.message, t.code)
                    } else {
                        var errorMsg = t.localizedMessage
                        if (t.localizedMessage.toLowerCase().equals("timeout")) errorMsg =
                            "The file is too large to upload!"
                        callback.onError(errorMsg, -1)
                    }
                }

                override fun onResponse(
                    call: Call<UploadFileResponse>,
                    response: retrofit2.Response<UploadFileResponse>
                ) {
                    callback.onSuccess(response.body() as File)
                }

            }

        if (mimeType.contains("image")) {
            mCDNService.sendImage(
                channel.type,
                channel.id,
                part,
                client.apiKey,
                client.getUserId(),
                client.getClientID()
            ).enqueue(callbackWrapper)
        } else {
            mCDNService.sendFile(
                channel.type,
                channel.id,
                part,
                client.apiKey,
                client.getUserId(),
                client.getClientID()
            ).enqueue(callbackWrapper)
        }
    }

    override fun deleteFile(channel: Channel?, url: String?, callback: CompletableCallback?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteImage(channel: Channel?, url: String?, callback: CompletableCallback?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun signFileUrl(url: String?): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}