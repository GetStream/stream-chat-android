package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.utils.ProgressCallback
import java.io.File

public interface FileUploader {

    public fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    )

    public fun sendFile(channelType: String, channelId: String, file: File): Call<String>

    public fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    )

    public fun sendImage(channelType: String, channelId: String, file: File): Call<String>

    public fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit>

    public fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit>
}
