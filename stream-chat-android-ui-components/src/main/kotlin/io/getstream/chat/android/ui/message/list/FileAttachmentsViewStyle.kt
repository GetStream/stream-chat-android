package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle

public data class FileAttachmentsViewStyle(
    @ColorInt public val backgroundColor: Int,
    public val progressIcon: Drawable,
    public val actionButtonIcon: Drawable,
    val titleTextStyle: TextStyle,
    val fileSizeTextStyle: TextStyle,
) {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): FileAttachmentsViewStyle {
            context.obtainStyledAttributes(attrs, R.styleable.FileAttachmentView).use { attrsArray ->
                val progressIcon =
                    attrsArray.getDrawable(R.styleable.FileAttachmentView_streamUiFileAttachmentProgressIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)!!

                val bgColor = attrsArray.getColor(
                    R.styleable.FileAttachmentView_streamUiFileAttachmentBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_grey_whisper)
                )

                val actionIcon =
                    attrsArray.getDrawable(R.styleable.FileAttachmentView_streamUiFileAttachmentActionButton)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_icon_download)!!

                val titleTextStyle = TextStyle.Builder(attrsArray)
                    .size(R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextSize)
                    .color(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleFontAssets,
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextFont
                    )
                    .style(R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextStyle, Typeface.NORMAL)
                    .build()

                val fileSizeTextStyle = TextStyle.Builder(attrsArray)
                    .size(R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeTextSize)
                    .color(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeFontAssets,
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeTextFont
                    )
                    .style(R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextStyle, Typeface.NORMAL)
                    .build()

                return FileAttachmentsViewStyle(
                    backgroundColor = bgColor,
                    progressIcon = progressIcon,
                    actionButtonIcon = actionIcon,
                    titleTextStyle = titleTextStyle,
                    fileSizeTextStyle = fileSizeTextStyle,
                )
            }
        }
    }
}
