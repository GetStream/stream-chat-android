package io.getstream.chat.android.compose.ui.messages.attachments.factory

import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract

/**
 * Defines which media type will be allowed.
 */
public enum class PickerMediaMode {
    PHOTO,
    VIDEO,
    PHOTO_AND_VIDEO,
}

/**
 * Maps [PickerMediaMode] into [CaptureMediaContract.Mode].
 */
internal val PickerMediaMode.mode: CaptureMediaContract.Mode
    get() = when (this) {
        PickerMediaMode.PHOTO -> CaptureMediaContract.Mode.PHOTO
        PickerMediaMode.VIDEO -> CaptureMediaContract.Mode.VIDEO
        PickerMediaMode.PHOTO_AND_VIDEO -> CaptureMediaContract.Mode.PHOTO_AND_VIDEO
    }
