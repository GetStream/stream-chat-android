package io.getstream.chat.android.ui.message.list.adapter.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.getEnum
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.utils.GiphyInfoType

/**
 * Sets the style for [io.getstream.chat.android.ui.message.list.adapter.view.internal.GiphyMediaAttachmentView] by obtaining
 * styled attributes.
 *
 * @param progressIcon Displayed while the Giphy is Loading.
 * @param giphyIcon Displays the Giphy logo over the Giphy image.
 * @param placeholderIcon Displayed while the Giphy is Loading.
 * @param imageBackgroundColor Sets the background colour for the Giphy container.
 * @param giphyType Sets the Giphy type which directly affects image quality and if the container is resized or not.
 * @param scaleType Sets the scaling type for loading the image. E.g. 'centerCrop', 'fitCenter', etc...
 */
public class GiphyMediaAttachmentViewStyle(
    public val progressIcon: Drawable,
    public val giphyIcon: Drawable,
    public val placeholderIcon: Drawable,
    @ColorInt public val imageBackgroundColor: Int,
    public val giphyType: GiphyInfoType,
    public val scaleType: ImageView.ScaleType,
) {
    internal companion object {
        /**
         * Fetches styled attributes and returns them wrapped inside of [GiphyMediaAttachmentViewStyle].
         */
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

                val giphyType =
                    attributes.getEnum(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentGiphyType,
                        GiphyInfoType.FIXED_HEIGHT)

                val scaleType =
                    attributes.getEnum(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentScaleType,
                        ImageView.ScaleType.FIT_CENTER)

                return GiphyMediaAttachmentViewStyle(
                    progressIcon = progressIcon,
                    giphyIcon = giphyIcon,
                    placeholderIcon = placeholderIcon,
                    imageBackgroundColor = imageBackgroundColor,
                    giphyType = giphyType,
                    scaleType = scaleType
                )
            }
        }
    }
}