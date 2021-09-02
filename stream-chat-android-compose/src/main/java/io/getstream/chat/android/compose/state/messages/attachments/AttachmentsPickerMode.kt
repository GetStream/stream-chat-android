package io.getstream.chat.android.compose.state.messages.attachments

/**
 * Represents the currently active Attachment picker mode.
 *
 * [Images] - Picking media items from the device.
 * [Files] - Picking files from the device.
 * [MediaCapture] - Capturing images/videos.
 */
public sealed class AttachmentsPickerMode

public object Images : AttachmentsPickerMode()

public object Files : AttachmentsPickerMode()

public object MediaCapture : AttachmentsPickerMode()
