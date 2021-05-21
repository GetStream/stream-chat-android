package io.getstream.chat.docs.kotlin.helpers

import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import java.io.File

class MyFileUploader : FileUploader {
    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<String> {
        return try {
            Result.success("url")
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): Result<String> {
        return try {
            Result.success("url")
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<String> {
        return try {
            Result.success("url")
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): Result<String> {
        return try {
            Result.success("url")
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    ): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    ): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}
