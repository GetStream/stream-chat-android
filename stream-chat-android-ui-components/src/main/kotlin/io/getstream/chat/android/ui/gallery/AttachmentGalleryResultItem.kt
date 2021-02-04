package io.getstream.chat.android.ui.gallery

import android.os.Parcelable
import io.getstream.chat.android.client.models.Attachment
import kotlinx.parcelize.Parcelize

@Parcelize
public data class AttachmentGalleryResultItem(
    val messageId: String,
    val cid: String,
    val userName: String,
    val isMine: Boolean = false,
    val authorName: String? = null,
    val imageUrl: String? = null,
    val assetUrl: String? = null,
    val mimeType: String? = null,
    val fileSize: Int = 0,
    val title: String? = null,
    val text: String? = null,
    val type: String? = null,
    val image: String? = null,
    val url: String? = null,
    val name: String? = null,
) : Parcelable

internal fun AttachmentGalleryResultItem.toAttachment(): Attachment {
    return Attachment(
        authorName = authorName,
        imageUrl = imageUrl,
        assetUrl = assetUrl,
        url = url,
        name = name,
        image = image,
        type = type,
        text = text,
        title = title,
        fileSize = fileSize,
        mimeType = mimeType,
    )
}

internal fun Attachment.toAttachmentGalleryResultItem(
    messageId: String,
    cid: String,
    userName: String,
    isMine: Boolean,
): AttachmentGalleryResultItem {
    return AttachmentGalleryResultItem(
        messageId = messageId,
        cid = cid,
        userName = userName,
        isMine = isMine,
        imageUrl = this.imageUrl,
        assetUrl = this.assetUrl,
        name = this.name
    )
}
