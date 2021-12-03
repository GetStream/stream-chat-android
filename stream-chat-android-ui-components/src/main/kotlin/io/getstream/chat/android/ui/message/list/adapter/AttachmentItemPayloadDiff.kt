package io.getstream.chat.android.ui.message.list.adapter

public data class AttachmentItemPayloadDiff(
    val authorName: Boolean,
    val authorLink: Boolean,
    val titleLink: Boolean,
    val thumbUrl: Boolean,
    val imageUrl: Boolean,
    val assetUrl: Boolean,
    val ogUrl: Boolean,
    val mimeType: Boolean,
    val fileSize: Boolean,
    val title: Boolean,
    val text: Boolean,
    val type: Boolean,
    val image: Boolean,
    val url: Boolean,
    val name: Boolean,
    val upload: Boolean,
    val uploadState: Boolean,
) {
    public operator fun plus(other: AttachmentItemPayloadDiff): AttachmentItemPayloadDiff {
        return AttachmentItemPayloadDiff(
            authorName = authorName || other.authorName,
            authorLink = authorLink || other.authorLink,
            titleLink = titleLink || other.titleLink,
            thumbUrl = thumbUrl || other.thumbUrl,
            imageUrl = imageUrl || other.imageUrl,
            assetUrl = assetUrl || other.assetUrl,
            ogUrl = ogUrl || other.ogUrl,
            mimeType = mimeType || other.mimeType,
            fileSize = fileSize || other.fileSize,
            title = title || other.title,
            text = text || other.text,
            type = type || other.type,
            image = image || other.image,
            url = url || other.url,
            name = name || other.name,
            upload = upload || other.upload,
            uploadState = uploadState || other.uploadState,
        )
    }
}
