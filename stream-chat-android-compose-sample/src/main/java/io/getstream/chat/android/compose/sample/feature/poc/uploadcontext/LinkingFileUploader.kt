/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.feature.poc.uploadcontext

import android.net.Uri
import io.getstream.chat.android.client.uploader.FileUploadContext
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.models.UploadedFile
import io.getstream.result.Result
import java.io.File

/**
 * A [FileUploader] simulating an app that uploads attachments to its own CDN and links each upload to the
 * message that will contain it, using [FileUploadContext.messageId].
 *
 * Only the context-aware overloads are overridden; the SDK no longer calls the legacy per-parameter overloads
 * once the context ones are implemented, but they are implemented here to satisfy the interface.
 *
 * Nothing is actually uploaded: the returned file URL is the local `file://` URI, which the sample can still
 * render on-device. Other clients would not be able to load it, which is fine for this PoC.
 */
class LinkingFileUploader : FileUploader {

    override fun sendFile(
        uploadContext: FileUploadContext,
        file: File,
        callback: ProgressCallback?,
    ): Result<UploadedFile> = upload(uploadContext, file)

    override fun sendImage(
        uploadContext: FileUploadContext,
        file: File,
        callback: ProgressCallback?,
    ): Result<UploadedFile> = upload(uploadContext, file)

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile> = uploadWithoutContext(file)

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile> = uploadWithoutContext(file)

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile> = uploadWithoutContext(file)

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile> = uploadWithoutContext(file)

    override fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> = Result.Success(Unit)

    override fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> = Result.Success(Unit)

    private fun upload(uploadContext: FileUploadContext, file: File): Result<UploadedFile> {
        FakeCustomerBackend.registerUpload(uploadContext.messageId, file.name)
        return Result.Success(UploadedFile(file = Uri.fromFile(file).toString()))
    }

    private fun uploadWithoutContext(file: File): Result<UploadedFile> {
        FakeCustomerBackend.registerUpload(messageId = null, fileName = file.name)
        return Result.Success(UploadedFile(file = Uri.fromFile(file).toString()))
    }
}
