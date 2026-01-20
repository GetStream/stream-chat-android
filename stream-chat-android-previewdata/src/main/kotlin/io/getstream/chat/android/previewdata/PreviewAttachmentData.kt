package io.getstream.chat.android.previewdata

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment

@InternalStreamChatApi
public object PreviewAttachmentData {

    public val attachmentImage1: Attachment = Attachment(
        name = "image1.jpg",
        fileSize = 2000000,
        type = "image",
        mimeType = "image/jpeg",
        imageUrl = "https://example.com/image1.jpg",
    )

    public val attachmentImage2: Attachment = Attachment(
        name = "image2.jpg",
        fileSize = 3000000,
        type = "image",
        mimeType = "image/jpeg",
        imageUrl = "https://example.com/image2.jpg",
    )

    public val attachmentImage3: Attachment = Attachment(
        name = "image3.jpg",
        fileSize = 4000000,
        type = "image",
        mimeType = "image/jpeg",
        imageUrl = "https://example.com/image3.jpg",
    )

    public val attachmentVideo1: Attachment = Attachment(
        name = "video1.mp4",
        fileSize = 10000000,
        type = "video",
        mimeType = "video/mp4",
        thumbUrl = "https://example.com/thumb1.jpg"
    )

    public val attachmentVideo2: Attachment = Attachment(
        name = "video2.mp4",
        fileSize = 20000000,
        type = "video",
        mimeType = "video/mp4",
        thumbUrl = "https://example.com/thumb2.jpg"
    )
}
