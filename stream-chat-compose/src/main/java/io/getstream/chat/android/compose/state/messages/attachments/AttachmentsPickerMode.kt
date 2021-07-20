package io.getstream.chat.android.compose.state.messages.attachments

/**
 * Represents the currently active Attachment picker mode.
 *
 * [Images] - Picking media items from the device.
 * [Files] - Picking files from the device.
 * [MediaCapture] - Capturing images/videos.
 * */
sealed class AttachmentsPickerMode

object Images : AttachmentsPickerMode()

object Files : AttachmentsPickerMode()

object MediaCapture : AttachmentsPickerMode()