package io.getstream.chat.android.ui.message.input.attachment.internal

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable

public data class AttachmentDialogStyle(
    val pictureAttachmentIcon: Drawable,
    @Deprecated(message = "Use pictureAttachmentIcon instead", level = DeprecationLevel.WARNING)
    val pictureAttachmentIconTint: ColorStateList?,
    val fileAttachmentIcon: Drawable,
    @Deprecated(message = "Use fileAttachmentIcon instead", level = DeprecationLevel.WARNING)
    val fileAttachmentIconTint: ColorStateList?,
    val cameraAttachmentIcon: Drawable,
    @Deprecated(message = "Use cameraAttachmentIcon instead", level = DeprecationLevel.WARNING)
    val cameraAttachmentIconTint: ColorStateList?,
)
