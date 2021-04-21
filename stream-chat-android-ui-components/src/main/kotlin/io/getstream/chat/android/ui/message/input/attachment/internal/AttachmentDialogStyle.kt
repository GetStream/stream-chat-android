package io.getstream.chat.android.ui.message.input.attachment.internal

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle

internal data class AttachmentDialogStyle(
    val pictureAttachmentIcon: Drawable,
    val pictureAttachmentIconTint: ColorStateList?,
    val fileAttachmentIcon: Drawable,
    val fileAttachmentIconTint: ColorStateList?,
    val cameraAttachmentIcon: Drawable,
    val cameraAttachmentIconTint: ColorStateList?,
)

internal fun MessageInputViewStyle.toAttachmentDialogStyle(): AttachmentDialogStyle {
    return AttachmentDialogStyle(
        pictureAttachmentIcon = pictureAttachmentIcon,
        pictureAttachmentIconTint = pictureAttachmentIconTint,
        fileAttachmentIcon = fileAttachmentIcon,
        fileAttachmentIconTint = fileAttachmentIconTint,
        cameraAttachmentIcon = cameraAttachmentIcon,
        cameraAttachmentIconTint = cameraAttachmentIconTint,
    )
}