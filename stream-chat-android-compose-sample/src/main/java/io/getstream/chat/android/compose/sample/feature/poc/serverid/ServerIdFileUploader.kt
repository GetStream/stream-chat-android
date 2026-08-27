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

package io.getstream.chat.android.compose.sample.feature.poc.serverid

import android.net.Uri
import android.util.Log
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.models.UploadedFile
import io.getstream.result.Result
import java.io.File

/**
 * A [FileUploader] simulating an app that uploads attachments to its own CDN.
 *
 * For each file it asks [FakeCustomerBackend] for upload info — which includes the id the message must be
 * sent with — and returns it inside [UploadedFile.extraData], from where the SDK merges it into the
 * attachment's extra data. [ServerIdMessageTransformer] later picks it up to rewrite the message id.
 *
 * Nothing is actually uploaded: the returned file URL is the local `file://` URI, which the sample can still
 * render on-device. Other clients would not be able to load it, which is fine for this PoC.
 */
class ServerIdFileUploader : FileUploader {

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile> = upload(file)

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile> = upload(file)

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile> = upload(file)

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile> = upload(file)

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

    private fun upload(file: File): Result<UploadedFile> {
        val uploadInfo = FakeCustomerBackend.requestUploadInfo()
        Log.d(ServerIdPoc.TAG, "Uploaded ${file.name}, remoteMessageId: ${uploadInfo.remoteMessageId}")
        return Result.Success(
            UploadedFile(
                file = Uri.fromFile(file).toString(),
                extraData = mapOf(ServerIdPoc.KEY_REMOTE_MESSAGE_ID to uploadInfo.remoteMessageId),
            ),
        )
    }
}
