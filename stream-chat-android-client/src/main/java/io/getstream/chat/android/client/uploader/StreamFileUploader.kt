/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.api.RetrofitCdnApi
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
        val body = file.asRequestBody(file.getMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        return retrofitCdnApi.sendFile(
            channelType = channelType,
            channelId = channelId,
            file = part,
            connectionId = connectionId,
            progressCallback = callback,
        ).execute().map { it.file }
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): Result<String> {
        val body = file.asRequestBody(file.getMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        return retrofitCdnApi.sendFile(
            channelType = channelType,
            channelId = channelId,
            file = part,
            connectionId = connectionId,
            progressCallback = null,
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
        val body = file.asRequestBody(file.getMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        return retrofitCdnApi.sendImage(
            channelType = channelType,
            channelId = channelId,
            file = part,
            connectionId = connectionId,
            progressCallback = callback,
        ).execute().map { it.file }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): Result<String> {
        val body = file.asRequestBody(file.getMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        return retrofitCdnApi.sendImage(
            channelType = channelType,
            channelId = channelId,
            file = part,
            connectionId = connectionId,
            progressCallback = null,
        ).execute().map { it.file }
    }

    override fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    ): Result<Unit> {
        return retrofitCdnApi.deleteFile(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            url = url
        ).execute().toUnitResult()
    }

    override fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    ): Result<Unit> {
        return retrofitCdnApi.deleteImage(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            url = url
        ).execute().toUnitResult()
    }
}
