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

package io.getstream.chat.docs.kotlin.ui.guides.realm.entities

import io.getstream.chat.android.models.Attachment
import io.getstream.result.Error.GenericError
import io.realm.kotlin.types.RealmObject
import java.io.File

private const val DEFAULT_UPLOAD_SUCCESS = 1

internal class UploadStateEntityRealm : RealmObject {
    var statusCode: Int = DEFAULT_UPLOAD_SUCCESS
    var errorMessage: String? = null

    internal companion object {
        internal const val UPLOAD_STATE_SUCCESS = 1
        internal const val UPLOAD_STATE_IN_PROGRESS = 2
        internal const val UPLOAD_STATE_FAILED = 3
    }
}

internal fun UploadStateEntityRealm.toDomain(uploadFile: File?): Attachment.UploadState = when (this.statusCode) {
    UploadStateEntityRealm.UPLOAD_STATE_SUCCESS -> Attachment.UploadState.Success
    UploadStateEntityRealm.UPLOAD_STATE_IN_PROGRESS -> Attachment.UploadState.InProgress(0, uploadFile?.length() ?: 0)
    UploadStateEntityRealm.UPLOAD_STATE_FAILED -> Attachment.UploadState.Failed(GenericError(message = ""))
    else -> error("Integer value of $statusCode can't be mapped to UploadState")
}

internal fun Attachment.UploadState.toRealm(): UploadStateEntityRealm {
    val (statusCode, errorMessage) = when (this) {
        Attachment.UploadState.Success -> UploadStateEntityRealm.UPLOAD_STATE_SUCCESS to null
        Attachment.UploadState.Idle -> UploadStateEntityRealm.UPLOAD_STATE_IN_PROGRESS to null
        is Attachment.UploadState.InProgress -> UploadStateEntityRealm.UPLOAD_STATE_IN_PROGRESS to null
        is Attachment.UploadState.Failed -> UploadStateEntityRealm.UPLOAD_STATE_FAILED to (this.error.message)
    }

    return UploadStateEntityRealm().apply {
        this.statusCode = statusCode
        this.errorMessage = errorMessage
    }
}
