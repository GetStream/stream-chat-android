package io.getstream.chat.docs.kotlin.helpers

import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import java.io.File

class MyFileUploader : FileUploader {
    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback,
    ): String? {
        TODO("Not yet implemented")
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): String? {
        TODO("Not yet implemented")
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback,
    ): String? {
        TODO("Not yet implemented")
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): String? {
        TODO("Not yet implemented")
    }

    override fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    ) {
        TODO("Not yet implemented")
    }
}
