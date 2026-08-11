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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.network.models.FileUploadResponse
import io.getstream.result.Error
import io.getstream.result.Result

/**
 * The upload endpoints omit `file` when the asset URL is empty, which leaves nothing to attach, so
 * that is reported as a failure rather than an upload with a blank URL.
 */
internal fun FileUploadResponse.toUploadedFile(): Result<UploadedFile> =
    when (val uploadedFileUrl = file) {
        null -> Result.Failure(Error.GenericError(message = "Missing file URL in the upload response"))
        else -> Result.Success(
            UploadedFile(
                file = uploadedFileUrl,
                thumbUrl = thumbUrl,
            ),
        )
    }
