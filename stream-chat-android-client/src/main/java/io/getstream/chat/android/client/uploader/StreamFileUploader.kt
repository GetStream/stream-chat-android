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
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.UploadedImage
import io.getstream.result.Result
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

internal class StreamFileUploader(
    private val retrofitCdnApi: RetrofitCdnApi,
) : FileUploader {

    private val filenameSanitizer = FilenameSanitizer()

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile> {
        val body = file.asRequestBody(file.getMediaType())
        val filename = filenameSanitizer.sanitize(file.name)
        val part = MultipartBody.Part.createFormData("file", filename, body)

        return retrofitCdnApi.sendFile(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = callback,
        ).execute().map {
            it.toUploadedFile()
        }
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile> {
        val body = file.asRequestBody(file.getMediaType())
        val filename = filenameSanitizer.sanitize(file.name)
        val part = MultipartBody.Part.createFormData("file", filename, body)

        return retrofitCdnApi.sendFile(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = null,
        ).execute().map {
            it.toUploadedFile()
        }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedImage> {
        val body = file.asRequestBody(file.getMediaType())
        val filename = filenameSanitizer.sanitize(file.name)
        val part = MultipartBody.Part.createFormData("file", filename, body)

        return retrofitCdnApi.sendImage(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = callback,
        ).execute().map {
            UploadedImage(file = it.file)
        }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedImage> {
        val body = file.asRequestBody(file.getMediaType())
        val filename = filenameSanitizer.sanitize(file.name)
        val part = MultipartBody.Part.createFormData("file", filename, body)

        return retrofitCdnApi.sendImage(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = null,
        ).execute().map {
            UploadedImage(file = it.file)
        }
    }

    override fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> {
        return retrofitCdnApi.deleteFile(
            channelType = channelType,
            channelId = channelId,
            url = url,
        ).execute().toUnitResult()
    }

    override fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> {
        return retrofitCdnApi.deleteImage(
            channelType = channelType,
            channelId = channelId,
            url = url,
        ).execute().toUnitResult()
    }
}

private class FilenameSanitizer {
    companion object {
        private const val MAX_NAME_LEN = 255
        private const val EMPTY = ""
    }

    private val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + '-' + '_'

    fun sanitize(filename: String): String = try {
        sanitizeInternal(filename)
    } catch (_: Throwable) {
        filename
    }

    private fun sanitizeInternal(filename: String): String {
        // Separate the extension and the base name
        val extension = filename.substringAfterLast(delimiter = '.', missingDelimiterValue = EMPTY)
        val baseName = if (extension.isNotEmpty()) filename.removeSuffix(suffix = ".$extension") else filename

        // Replace invalid characters in the base name
        var sanitizedBaseName = baseName.map { if (it in allowedChars) it else '_' }.joinToString(EMPTY)

        // Truncate the base name if it is too long
        val maxBaseNameLength = MAX_NAME_LEN - extension.length - 1 // Adjust for the extension and dot
        if (sanitizedBaseName.length > maxBaseNameLength) {
            sanitizedBaseName = sanitizedBaseName.substring(0, maxBaseNameLength)
        }

        // Reconstruct the filename with the extension
        return if (extension.isNotEmpty()) "$sanitizedBaseName.$extension" else sanitizedBaseName
    }
}
