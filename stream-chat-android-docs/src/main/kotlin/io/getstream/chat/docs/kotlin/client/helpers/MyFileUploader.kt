package io.getstream.chat.docs.kotlin.client.helpers

import io.getstream.chat.android.client.models.UploadedFile
import io.getstream.chat.android.client.models.UploadedImage
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import java.io.File

class MyFileUploader : FileUploader {
    override suspend fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile> {
        return try {
            Result.success(UploadedFile(file = "file url", thumbUrl = "thumb url"))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile> {
        return try {
            Result.success(UploadedFile(file = "file url", thumbUrl = "thumb url"))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedImage> {
        return try {
            Result.success(UploadedImage(file = "url"))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedImage> {
        return try {
            Result.success(UploadedImage(file = "url"))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}
