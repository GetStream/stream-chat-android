package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.api.RetrofitCdnApi
import io.getstream.chat.android.client.api.models.ProgressRequestBody
import io.getstream.chat.android.client.extensions.getMediaType
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.client.utils.toUnitResult
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

internal class StreamFileUploader(
    private val retrofitCdnApi: RetrofitCdnApi,
) : FileUploader {

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<String> {
        val body = ProgressRequestBody(file, callback)
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        return retrofitCdnApi.sendFile(
            channelType,
            channelId,
            part,
            connectionId
        ).execute().map { it.file }
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): Result<String> {
        val part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody(file.getMediaType())
        )

        return retrofitCdnApi.sendFile(
            channelType,
            channelId,
            part,
            connectionId
        ).execute().map { it.file }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<String> {
        val body = ProgressRequestBody(file, callback)
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        return retrofitCdnApi
            .sendImage(
                channelType,
                channelId,
                part,
                connectionId
            ).execute().map { it.file }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): Result<String> {
        val part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody(file.getMediaType())
        )

        return retrofitCdnApi.sendImage(
            channelType,
            channelId,
            part,
            connectionId
        ).execute().map { it.file }
    }

    override fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    ): Result<Unit> {
        return retrofitCdnApi.deleteFile(channelType, channelId, connectionId, url)
            .execute()
            .toUnitResult()
    }

    override fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    ): Result<Unit> {
        return retrofitCdnApi.deleteImage(channelType, channelId, connectionId, url)
            .execute()
            .toUnitResult()
    }
}
