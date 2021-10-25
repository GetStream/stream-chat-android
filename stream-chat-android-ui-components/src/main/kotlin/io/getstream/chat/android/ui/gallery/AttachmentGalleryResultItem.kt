package io.getstream.chat.android.ui.gallery

import android.os.Parcelable
import io.getstream.chat.android.client.models.Attachment
import kotlinx.parcelize.Parcelize

/**
 * Parcelable data class that represents [Attachment] in [AttachmentGalleryActivity] as result some operation. See click
 * listeners of [AttachmentGalleryActivity].
 */
@Parcelize
public data class AttachmentGalleryResultItem(
    val messageId: String,
    val cid: String,
    val userName: String,
    val isMine: Boolean = false,
    val authorName: String? = null,
    val authorLink: String? = null,
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

/**
 * Extension to convert instance of [AttachmentGalleryResultItem] to [Attachment] type.
 */
public fun AttachmentGalleryResultItem.toAttachment(): Attachment {
    return Attachment(
        authorName = authorName,
        authorLink = authorLink,
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

/**
 * Extension to convert instance of [Attachment] to [AttachmentGalleryResultItem] type.
 */
public fun Attachment.toAttachmentGalleryResultItem(
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
        name = this.name,
        authorLink = authorLink
    )
}
