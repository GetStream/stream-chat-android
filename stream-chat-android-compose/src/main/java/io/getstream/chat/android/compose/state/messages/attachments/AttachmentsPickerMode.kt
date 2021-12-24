package io.getstream.chat.android.compose.state.messages.attachments

/**
 * Represents the currently active attachment picker mode.
 */
public sealed class AttachmentsPickerMode

/**
 * Represents the mode with media files from the device.
 */
public object Images : AttachmentsPickerMode()

/**
 * Represents the mode with files from the device.
 */
public object Files : AttachmentsPickerMode()

/**
 * Represents the mode with media capture.
 */
public object MediaCapture : AttachmentsPickerMode()
