package io.getstream.chat.android.ui.message.list.adapter.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.getEnum
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.message.list.adapter.view.internal.GiphyMediaAttachmentView

/**
 * Sets the style for [io.getstream.chat.android.ui.message.list.adapter.view.internal.GiphyMediaAttachmentView].
 */
public class GiphyMediaAttachmentViewStyle(
    public val progressIcon: Drawable,
    public val giphyIcon: Drawable,
    public val placeholderIcon: Drawable,
    @ColorInt public val imageBackgroundColor: Int,
    public val giphyConstantSizeEnabled: Boolean,
    @DimenRes public val giphyHeight: Int,
    public val giphyType: GiphyMediaAttachmentView.Companion.GiphyInfoType,
    public val scaleType: ImageView.ScaleType
) {
    internal companion object {
        private const val DEFAULT_HEIGHT_DP = 200

        operator fun invoke(context: Context, attrs: AttributeSet?): GiphyMediaAttachmentViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.GiphyMediaAttachmentView,
                R.attr.streamUiMessageListMediaAttachmentStyle,
                R.style.StreamUi_MessageList_GiphyMediaAttachment
            ).use { attributes ->
                val progressIcon =
                    attributes.getDrawable(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentProgressIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)!!

                val giphyIcon =
                    attributes.getDrawable(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentGiphyIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_giphy_label)!!

                val imageBackgroundColor = attributes.getColor(
                    R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentImageBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_grey)
                )

                val placeholderIcon =
                    attributes.getDrawable(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentPlaceHolderIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_picture_placeholder)!!

                val giphyHeight =
                    attributes.getDimensionPixelSize(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentGiphyMaxHeight,
                        DEFAULT_HEIGHT_DP.dpToPx())

                val constantSizeEnabled =
                    attributes.getBoolean(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentConstantSize,
                        false)

                val giphyType = attributes.getEnum(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentGiphyType,
                    GiphyMediaAttachmentView.Companion.GiphyInfoType.FIXED_HEIGHT)

                val scaleType = attributes.getEnum(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentScaleType, ImageView.ScaleType.CENTER_CROP)

                return GiphyMediaAttachmentViewStyle(
                    progressIcon = progressIcon,
                    giphyIcon = giphyIcon,
                    placeholderIcon = placeholderIcon,
                    imageBackgroundColor = imageBackgroundColor,
                    giphyConstantSizeEnabled = constantSizeEnabled,
                    giphyHeight = giphyHeight,
                    giphyType = giphyType,
                    scaleType = scaleType
                )
            }
        }
    }
}