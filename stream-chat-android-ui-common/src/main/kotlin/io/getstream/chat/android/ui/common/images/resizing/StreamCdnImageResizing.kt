package io.getstream.chat.android.ui.common.images.resizing

import androidx.annotation.FloatRange
import io.getstream.chat.android.models.streamcdn.image.StreamCdnCropImageMode
import io.getstream.chat.android.models.streamcdn.image.StreamCdnResizeImageMode

/**
 * Used to adjust the request to resize images hosted on Stream's CDN.
 *
 * Note: This only affects images hosted on Stream's CDN which contain original width (ow) and height (oh)
 * query parameters.
 *
 * @param imageResizingEnabled Enables or disables image resizing.
 * @param resizedWidthPercentage The percentage of the original image width the resized image width will be.
 * @param resizedHeightPercentage The percentage of the original image height the resized image height will be.
 * @param resizeMode Sets the image resizing mode. The default mode is [StreamCdnResizeImageMode.CLIP].
 * @param cropMode Sets the image crop mode. The default mode is [StreamCdnCropImageMode.CENTER].
 */
public data class StreamCdnImageResizing(
    val imageResizingEnabled: Boolean = false,
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) val resizedWidthPercentage: Float = 0.5f,
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) val resizedHeightPercentage: Float = 0.5f,
    val resizeMode: StreamCdnResizeImageMode = StreamCdnResizeImageMode.CLIP,
    val cropMode: StreamCdnCropImageMode = StreamCdnCropImageMode.CENTER,
)
