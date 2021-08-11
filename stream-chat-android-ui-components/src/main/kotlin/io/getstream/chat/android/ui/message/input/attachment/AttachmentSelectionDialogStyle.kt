package io.getstream.chat.android.ui.message.input.attachment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.style.TextStyle

public data class AttachmentSelectionDialogStyle(
    val pictureAttachmentIcon: Drawable,
    @Deprecated(message = "Use pictureAttachmentIcon instead", level = DeprecationLevel.ERROR)
    val pictureAttachmentIconTint: ColorStateList?,
    val fileAttachmentIcon: Drawable,
    @Deprecated(message = "Use fileAttachmentIcon instead", level = DeprecationLevel.ERROR)
    val fileAttachmentIconTint: ColorStateList?,
    val cameraAttachmentIcon: Drawable,
    @Deprecated(message = "Use cameraAttachmentIcon instead", level = DeprecationLevel.ERROR)
    val cameraAttachmentIconTint: ColorStateList?,
    val allowAccessToGalleryText: String,
    val allowAccessToFilesText: String,
    val allowAccessToCameraText: String,
    val allowAccessToGalleryIcon: Drawable,
    val allowAccessToFilesIcon: Drawable,
    val allowAccessToCameraIcon: Drawable,
    val grantPermissionsTextStyle: TextStyle,
    val recentFilesTextStyle: TextStyle,
    val recentFilesText: String,
    val fileManagerIcon: Drawable,
    val videoDurationTextStyle: TextStyle,
    val videoIconDrawable: Drawable,
    val videoIconVisible: Boolean,
    val videoLengthLabelVisible: Boolean,
    @ColorInt val backgroundColor: Int,
    val attachButtonIcon: Drawable,
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
                allowAccessToGalleryIcon = context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_media)!!,
                allowAccessToCameraIcon = context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_camera)!!,
                allowAccessToFilesIcon = context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_file)!!,
                allowAccessToGalleryText = context.getString(R.string.stream_ui_message_input_gallery_access),
                allowAccessToFilesText = context.getString(R.string.stream_ui_message_input_files_access),
                allowAccessToCameraText = context.getString(R.string.stream_ui_message_input_camera_access),
                grantPermissionsTextStyle = TextStyle(
                    size = context.resources.getDimensionPixelSize(R.dimen.stream_ui_spacing_medium),
                    color = context.getColorCompat(R.color.stream_ui_accent_blue),
                    style = Typeface.BOLD,
                ),
                recentFilesTextStyle = TextStyle(
                    size = context.resources.getDimensionPixelSize(R.dimen.stream_ui_spacing_medium),
                    color = context.getColorCompat(R.color.stream_ui_black),
                    style = Typeface.BOLD,
                ),
                recentFilesText = context.getString(R.string.stream_ui_message_input_recent_files),
                fileManagerIcon = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_file_manager)!!,
                videoDurationTextStyle = TextStyle(
                    size = context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_small),
                    color = context.getColorCompat(R.color.stream_ui_white),
                ),
                videoIconDrawable = context.getDrawableCompat(R.drawable.stream_ui_ic_video)!!,
                videoIconVisible = true,
                videoLengthLabelVisible = true,
                backgroundColor = context.getColorCompat(R.color.stream_ui_white_smoke),
                attachButtonIcon = context.getDrawableCompat(R.drawable.stream_ui_ic_next)!!,
            )
        }
    }
}
