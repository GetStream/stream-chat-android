package com.getstream.sdk.chat.model

import android.net.Uri
import com.getstream.sdk.chat.utils.Utils
import io.getstream.chat.android.client.models.Attachment
import java.io.File

class AttachmentMetaData {
    var isSelected = false
    var videoLength: Long = 0
    var size: Long = 0
    var uri: Uri? = null
    var type: String? = null
    var mimeType: String?
    var title: String? = null
    var file: File? = null

    constructor(attachment: Attachment) {
        this.type = attachment.type
        this.mimeType = attachment.mimeType
        this.title = attachment.title
    }

    constructor(file: File) {
        this.file = file
        this.uri = Uri.fromFile(file)
        this.mimeType = Utils.getMimeType(file)
    }

    constructor(uri: Uri, mimeType: String?) {
        this.uri = uri
        this.mimeType = mimeType
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AttachmentMetaData) return false

        if (uri != other.uri) return false
        if (type != other.type) return false
        if (mimeType != other.mimeType) return false
        if (title != other.title) return false
        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uri?.hashCode() ?: 0
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (file?.hashCode() ?: 0)
        return result
    }
}
