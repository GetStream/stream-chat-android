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
import io.getstream.chat.android.client.api2.mapping.toUploadedFile
import io.getstream.chat.android.client.extensions.getMediaType
import io.getstream.chat.android.client.models.UploadedFile
import io.getstream.chat.android.client.models.UploadedImage
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

    override suspend fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile> {
        val body = file.asRequestBody(file.getMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        return retrofitCdnApi.sendFile(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = callback,
        ).await().map {
            it.toUploadedFile()
        }
    }

    override suspend fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile> {
        val body = file.asRequestBody(file.getMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        return retrofitCdnApi.sendFile(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = null,
        ).await().map {
            it.toUploadedFile()
        }
    }

    override suspend fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedImage> {
        val body = file.asRequestBody(file.getMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        return retrofitCdnApi.sendImage(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = callback,
        ).await().map {
            UploadedImage(file = it.file)
        }
    }

    override suspend fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedImage> {
        val body = file.asRequestBody(file.getMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        return retrofitCdnApi.sendImage(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = null,
        ).await().map {
            UploadedImage(file = it.file)
        }
    }

    override suspend fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> {
        return retrofitCdnApi.deleteFile(
            channelType = channelType,
            channelId = channelId,
            url = url
        ).await().toUnitResult()
    }

    override suspend fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> {
        return retrofitCdnApi.deleteImage(
            channelType = channelType,
            channelId = channelId,
            url = url
        ).await().toUnitResult()
    }
}
