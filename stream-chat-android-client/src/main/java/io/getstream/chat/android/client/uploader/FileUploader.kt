package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.utils.ProgressCallback
import java.io.File

public interface FileUploader {

    public fun setConnection(userId: String, connectionId: String)

    public fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    )

    public fun sendFile(channelType: String, channelId: String, file: File): String?

    public fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    )

    public fun sendImage(channelType: String, channelId: String, file: File): String?

    public fun deleteFile(channelType: String, channelId: String, url: String)

    public fun deleteImage(channelType: String, channelId: String, url: String)
}
