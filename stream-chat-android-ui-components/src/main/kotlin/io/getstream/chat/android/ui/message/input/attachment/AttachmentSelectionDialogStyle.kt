package io.getstream.chat.android.ui.message.input.attachment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat

public data class AttachmentSelectionDialogStyle(
    val pictureAttachmentIcon: Drawable,
    @Deprecated(message = "Use pictureAttachmentIcon instead", level = DeprecationLevel.WARNING)
    val pictureAttachmentIconTint: ColorStateList?,
    val fileAttachmentIcon: Drawable,
    @Deprecated(message = "Use fileAttachmentIcon instead", level = DeprecationLevel.WARNING)
    val fileAttachmentIconTint: ColorStateList?,
    val cameraAttachmentIcon: Drawable,
    @Deprecated(message = "Use cameraAttachmentIcon instead", level = DeprecationLevel.WARNING)
    val cameraAttachmentIconTint: ColorStateList?,
) {
    public companion object {
        /**
         * Creates an [AttachmentSelectionDialogStyle] instance with the default Stream Chat icons.
         */
        public fun createDefault(context: Context): AttachmentSelectionDialogStyle {
            return AttachmentSelectionDialogStyle(
                pictureAttachmentIcon = context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_media)!!,
                pictureAttachmentIconTint = null,
                fileAttachmentIcon = context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_file)!!,
                fileAttachmentIconTint = null,
                cameraAttachmentIcon = context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_camera)!!,
                cameraAttachmentIconTint = null,
            )
        }
    }
}
