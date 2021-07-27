package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
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
) {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): FileAttachmentViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.FileAttachmentView,
                R.attr.streamUiMessageListFileAttachmentViewStyle,
                R.style.StreamUi_MessageList_FileAttachment
            ).use { a ->
                val progressBarDrawable =
                    a.getDrawable(R.styleable.FileAttachmentView_streamUiFileAttachmentProgressBarDrawable)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)!!

                val backgroundColor = a.getColor(
                    R.styleable.FileAttachmentView_streamUiFileAttachmentBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white)
                )

                val actionIcon = a.getDrawable(R.styleable.FileAttachmentView_streamUiFileAttachmentActionButton)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_icon_download)!!

                val titleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleFontAssets,
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextFont
                    )
                    .style(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val fileSizeTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeFontAssets,
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeTextFont
                    )
                    .style(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val failedAttachmentIcon =
                    a.getDrawable(R.styleable.FileAttachmentView_streamUiFileAttachmentFailedAttachmentIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_warning)!!

                val strokeColor = a.getColor(
                    R.styleable.FileAttachmentView_streamUiFileAttachmentStrokeColor,
                    context.getColorCompat(R.color.stream_ui_grey_whisper)
                )

                val strokeWidth = a.getDimensionPixelSize(
                    R.styleable.FileAttachmentView_streamUiFileAttachmentStrokeWidth,
                    1.dpToPx()
                )

                val cornerRadius = a.getDimensionPixelSize(
                    R.styleable.FileAttachmentView_streamUiFileAttachmentCornerRadius,
                    12.dpToPx()
                )

                return FileAttachmentViewStyle(
                    backgroundColor = backgroundColor,
                    progressBarDrawable = progressBarDrawable,
                    actionButtonIcon = actionIcon,
                    titleTextStyle = titleTextStyle,
                    fileSizeTextStyle = fileSizeTextStyle,
                    failedAttachmentIcon = failedAttachmentIcon,
                    strokeColor = strokeColor,
                    strokeWidth = strokeWidth,
                    cornerRadius = cornerRadius,
                ).let(TransformStyle.fileAttachmentStyleTransformer::transform)
            }
        }
    }
}
