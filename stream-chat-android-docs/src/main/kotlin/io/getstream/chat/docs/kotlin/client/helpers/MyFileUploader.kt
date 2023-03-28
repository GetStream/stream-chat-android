package io.getstream.chat.docs.kotlin.client.helpers

import io.getstream.result.StreamError
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.UploadedImage
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.result.Result
import java.io.File

class MyFileUploader : FileUploader {
    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile> {
        return try {
            Result.Success(UploadedFile(file = "file url", thumbUrl = "thumb url"))
        } catch (e: Exception) {
            Result.Failure(StreamError.ThrowableError(message = "Could not send file.", cause = e))
        }
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile> {
        return try {
            Result.Success(UploadedFile(file = "file url", thumbUrl = "thumb url"))
        } catch (e: Exception) {
            Result.Failure(StreamError.ThrowableError(message = "Could not send file.", cause = e))
        }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedImage> {
        return try {
            Result.Success(UploadedImage(file = "url"))
        } catch (e: Exception) {
            Result.Failure(StreamError.ThrowableError(message = "Could not send image.", cause = e))
        }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedImage> {
        return try {
            Result.Success(UploadedImage(file = "url"))
        } catch (e: Exception) {
            Result.Failure(StreamError.ThrowableError(message = "Could not send image.", cause = e))
        }
    }

    override fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> {
        return try {
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(StreamError.ThrowableError(message = "Could not delete file.", cause = e))
        }
    }

    override fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> {
        return try {
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(StreamError.ThrowableError(message = "Could not delete image.", cause = e))
        }
    }
}
