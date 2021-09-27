package io.getstream.chat.android.ui.message.list

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.getstream.chat.android.ui.common.style.TextStyle

public data class FileAttachmentViewStyle(
    @ColorInt val backgroundColor: Int,
    @ColorInt val strokeColor: Int,
    @Px val strokeWidth: Int,
    @Px val cornerRadius: Int,
    val progressBarDrawable: Drawable,
    public val actionButtonIcon: Drawable,
    public val failedAttachmentIcon: Drawable,
    val titleTextStyle: TextStyle,
    val fileSizeTextStyle: TextStyle,
)
