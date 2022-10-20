package io.getstream.realm.entity

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
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
    UploadStateEntityRealm.UPLOAD_STATE_FAILED -> Attachment.UploadState.Failed(ChatError(message = this.errorMessage))
    else -> error("Integer value of $statusCode can't be mapped to UploadState")
}

internal fun Attachment.UploadState.toRealm(): UploadStateEntityRealm {
    val (statusCode, errorMessage) = when (this) {
        Attachment.UploadState.Success -> UploadStateEntityRealm.UPLOAD_STATE_SUCCESS to null
        Attachment.UploadState.Idle -> UploadStateEntityRealm.UPLOAD_STATE_IN_PROGRESS to null
        is Attachment.UploadState.InProgress -> UploadStateEntityRealm.UPLOAD_STATE_IN_PROGRESS to null
        is Attachment.UploadState.Failed -> UploadStateEntityRealm.UPLOAD_STATE_FAILED to (
            this.error.message
                ?: this.error.cause?.localizedMessage
            )
    }

    return UploadStateEntityRealm().apply {
        this.statusCode = statusCode
        this.errorMessage = errorMessage
    }
}

