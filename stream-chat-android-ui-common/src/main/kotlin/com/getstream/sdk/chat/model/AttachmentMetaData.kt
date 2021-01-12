package com.getstream.sdk.chat.model

import android.content.Context
import android.net.Uri
import com.getstream.sdk.chat.StreamFileProvider
import com.getstream.sdk.chat.utils.Utils
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.io.File

@InternalStreamChatApi
public data class AttachmentMetaData(
    var uri: Uri? = null,
    var type: String? = null,
    var mimeType: String? = null,
    var title: String? = null,
    var file: File? = null,
) {
    var size: Long = 0
    var isSelected: Boolean = false
    var selectedPosition: Int = 0
    var videoLength: Long = 0

    public constructor(attachment: Attachment) : this(
        type = attachment.type,
        mimeType = attachment.mimeType,
        title = attachment.title
    )

    public constructor(
        context: Context,
        file: File
    ) : this(file = file, uri = StreamFileProvider.getUriForFile(context, file)) {
        mimeType = Utils.getMimeType(file)
        type = getTypeFromMimeType(mimeType)
        size = file.length()
    }

    private fun getTypeFromMimeType(mimeType: String?): String = mimeType?.let { type ->
        when {
            type.contains("image") -> {
                ModelType.attach_image
            }
            type.contains("video") -> {
                ModelType.attach_video
            }
            else -> {
                ModelType.attach_file
            }
        }
    } ?: ModelType.attach_file
}
