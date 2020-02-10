package com.getstream.sdk.chat.model;

import android.text.TextUtils;

public class ModelType {
    // Channel Type
    public static final String channel_unknown = "unknown";
    public static final String channel_livestream = "livestream";
    public static final String channel_messaging = "messaging";
    public static final String channel_team = "team";
    public static final String channel_gaming = "gaming";
    public static final String channel_commerce = "commerce";

    // Message Type
    public static final String message_regular = "regular";
    public static final String message_ephemeral = "ephemeral";
    public static final String message_error = "error";
    public static final String message_failed = "failed";
    public static final String message_reply = "reply";
    public static final String message_system = "system";

    // Attachment Type
    public static final String attach_image = "image";
    public static final String attach_imgur = "imgur";
    public static final String attach_giphy = "giphy";
    public static final String attach_video = "video";
    public static final String attach_product = "product";
    public static final String attach_file = "file";
    public static final String attach_link = "link";
    public static final String attach_unknown = "unknown";

    // File Mime Type
    public static final String attach_mime_tar = "application/tar";
    public static final String attach_mime_zip = "application/zip";

    public static final String attach_mime_txt = "text/plain";
    public static final String attach_mime_doc = "application/msword";
    public static final String attach_mime_docx = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    //    public static final String file_xls = "application/vnd.ms-excel";
    public static final String attach_mime_xlsx = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String attach_mime_csv = "application/vnd.ms-excel";
    public static final String attach_mime_ppt = "application/vnd.ms-powerpoint";
    public static final String attach_mime_pdf = "application/pdf";

    public static final String attach_mime_mov = "video/mov";
    public static final String attach_mime_mp4 = "video/mp4";
    public static final String attach_mime_mp3 = "audio/mp3";
    public static final String attach_mime_m4a = "audio/m4a";
    // Action Type
    public static final String action_send = "send";
    public static final String action_shuffle = "shuffle";
    public static final String action_cancel = "cancel";

    public static String getAssetUrl(Attachment attachment){
        switch (attachment.getType()) {
            case ModelType.attach_image:
                return attachment.getImageURL();
            case ModelType.attach_giphy:
            case ModelType.attach_video:
                return attachment.getThumbURL();
            default:
                return TextUtils.isEmpty(attachment.getImageURL()) ? attachment.getImage() : attachment.getImageURL();
        }
    }
}
