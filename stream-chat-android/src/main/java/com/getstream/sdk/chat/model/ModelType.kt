package com.getstream.sdk.chat.model

import io.getstream.chat.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public object ModelType {
    // Channel Type
    public const val channel_unknown: String = "unknown"
    public const val channel_livestream: String = "livestream"
    public const val channel_messaging: String = "messaging"
    public const val channel_team: String = "team"
    public const val channel_gaming: String = "gaming"
    public const val channel_commerce: String = "commerce"

    // Message Type
    public const val message_regular: String = "regular"
    public const val message_ephemeral: String = "ephemeral"
    public const val message_error: String = "error"
    public const val message_failed: String = "failed"
    public const val message_reply: String = "reply"
    public const val message_system: String = "system"

    // Attachment Type
    public const val attach_image: String = "image"
    public const val attach_imgur: String = "imgur"
    public const val attach_giphy: String = "giphy"
    public const val attach_video: String = "video"
    public const val attach_product: String = "product"
    public const val attach_file: String = "file"
    public const val attach_link: String = "link"
    public const val attach_unknown: String = "unknown"

    // File Mime Type
    public const val attach_mime_tar: String = "application/tar"
    public const val attach_mime_zip: String = "application/zip"
    public const val attach_mime_txt: String = "text/plain"
    public const val attach_mime_doc: String = "application/msword"
    public const val attach_mime_docx: String =
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

    // const val attach_mine_xls = "application/vnd.ms-excel"
    public const val attach_mime_xlsx: String = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    public const val attach_mime_csv: String = "application/vnd.ms-excel"
    public const val attach_mime_ppt: String = "application/vnd.ms-powerpoint"
    public const val attach_mime_pdf: String = "application/pdf"
    public const val attach_mime_mov: String = "video/mov"
    public const val attach_mime_mp4: String = "video/mp4"
    public const val attach_mime_mp3: String = "audio/mp3"
    public const val attach_mime_m4a: String = "audio/m4a"
    public const val attach_mime_gif: String = "image/gif"

    // Action Type
    public const val action_send: String = "send"
    public const val action_shuffle: String = "shuffle"
    public const val action_cancel: String = "cancel"
}
