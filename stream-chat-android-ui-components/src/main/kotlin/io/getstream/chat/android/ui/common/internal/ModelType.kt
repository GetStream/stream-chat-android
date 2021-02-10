package io.getstream.chat.android.ui.common.internal

internal object ModelType {
    // Channel Type
    const val channel_unknown = "unknown"
    const val channel_livestream = "livestream"
    const val channel_messaging = "messaging"
    const val channel_team = "team"
    const val channel_gaming = "gaming"
    const val channel_commerce = "commerce"

    // Message Type
    const val message_regular = "regular"
    const val message_ephemeral = "ephemeral"
    const val message_error = "error"
    const val message_failed = "failed"
    const val message_reply = "reply"
    const val message_system = "system"

    // Attachment Type
    const val attach_image = "image"
    const val attach_imgur = "imgur"
    const val attach_giphy = "giphy"
    const val attach_video = "video"
    const val attach_product = "product"
    const val attach_file = "file"
    const val attach_link = "link"
    const val attach_unknown = "unknown"

    // File Mime Type
    const val attach_mime_tar = "application/tar"
    const val attach_mime_zip = "application/zip"
    const val attach_mime_txt = "text/plain"
    const val attach_mime_doc = "application/msword"
    const val attach_mime_docx =
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

    // const val attach_mine_xls = "application/vnd.ms-excel"
    const val attach_mime_xlsx = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    const val attach_mime_csv = "application/vnd.ms-excel"
    const val attach_mime_ppt = "application/vnd.ms-powerpoint"
    const val attach_mime_pdf = "application/pdf"
    const val attach_mime_mov = "video/mov"
    const val attach_mime_mp4 = "video/mp4"
    const val attach_mime_mp3 = "audio/mp3"
    const val attach_mime_m4a = "audio/m4a"
    const val attach_mime_gif = "image/gif"

    // Action Type
    const val action_send = "send"
    const val action_shuffle = "shuffle"
    const val action_cancel = "cancel"
}
