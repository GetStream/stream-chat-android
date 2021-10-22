package io.getstream.chat.android.ui.message.input.attachment

/**
 * Represents source of attachment.
 */
public enum class AttachmentSource {
    /**
     * Attachments from gallery.
     */
    MEDIA,

    /**
     * Attachments from file picker.
     */
    FILE,

    /**
     * Image or video captured attachment from camera.
     */
    CAMERA,
}
