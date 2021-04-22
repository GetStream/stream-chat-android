package io.getstream.chat.android.ui.message.input.attachment.internal

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable

public data class AttachmentDialogStyle(
    val pictureAttachmentIcon: Drawable,
    val pictureAttachmentIconTint: ColorStateList,
    val fileAttachmentIcon: Drawable,
    val fileAttachmentIconTint: ColorStateList,
    val cameraAttachmentIcon: Drawable,
    val cameraAttachmentIconTint: ColorStateList,
)
