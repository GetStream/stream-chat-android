/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.models.streamcdn.image.extensions

import androidx.annotation.FloatRange
import io.getstream.chat.android.models.streamcdn.image.StreamCdnCropImageMode
import io.getstream.chat.android.models.streamcdn.image.StreamCdnOriginalImageDimensions
import io.getstream.chat.android.models.streamcdn.image.StreamCdnResizeImageMode
import io.getstream.log.StreamLog

/**
 * Returns [StreamCdnOriginalImageDimensions] if the image is hosted by Stream's CDN and is resizable,
 * otherwise returns null.
 *
 * @return Class containing the original width and height dimensions of the image or null.
 */
public fun String.getStreamCdnHostedImageDimensions(): StreamCdnOriginalImageDimensions? {
    return try {
        val width = this.substringAfter("ow=", "").takeIf { it.isNotBlank() }
            ?.substringBefore("&")?.toInt()
        val height = this.substringAfter("oh=", "").takeIf { it.isNotBlank() }
            ?.substringBefore("&")?.toInt()

        if (height != null && width != null) {
            StreamCdnOriginalImageDimensions(
                originalWidth = width,
                originalHeight = height
            )
        } else {
            null
        }
    } catch (e: java.lang.Exception) {
        val logger = StreamLog.getLogger("Chat: getStreamCDNHostedImageDimensions")
        logger.e { "Failed to parse Stream CDN image dimensions from the URL:\n ${e.stackTraceToString()}" }

        null
    }
}

/**
 * Generates a string URL with Stream CDN image resizing query parameters added to it. Once this URL is called, Stream's
 * CDN will generate a resized image which is accessible using the link returned by this function.
 *
 * @param resizedWidthPercentage The percentage of the original image width the resized image width will be.
 * @param resizedHeightPercentage The percentage of the original image height the resized image height will be.
 * @param resizeMode Sets the image resizing mode. The default mode is [StreamCdnResizeImageMode.CLIP].
 * @param cropMode Sets the image crop mode. The default mode is [StreamCdnCropImageMode.CENTER].
 */
public fun String.createResizedStreamCdnImageUrl(
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) resizedWidthPercentage: Float,
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) resizedHeightPercentage: Float,
    resizeMode: StreamCdnResizeImageMode? = null,
    cropMode: StreamCdnCropImageMode? = null
): String {
    val streamCdnImageDimensions = this.getStreamCdnHostedImageDimensions()

    return if (streamCdnImageDimensions != null) {

        val resizedWidth: Int = (streamCdnImageDimensions.originalWidth * resizedWidthPercentage).toInt()
        val resizedHeight: Int = (streamCdnImageDimensions.originalHeight * resizedHeightPercentage).toInt()

        this.appendValueAsQueryParameterIfNotNull(value = resizedWidth, name = "w")
            .appendValueAsQueryParameterIfNotNull(value = resizedHeight, name = "h")
            .appendValueAsQueryParameterIfNotNull(value = resizeMode?.queryParameterName, name = "resize")
            .appendValueAsQueryParameterIfNotNull(value = cropMode?.queryParameterName, name = "crop")
    } else {
        val logger = StreamLog.getLogger("Chat:resizedStreamCdnImageUrl")
        logger.w {
            "Only images hosted by Stream's CDN containing original width and height query parameters" +
                "can be resized"
        }
        this
    }
}

/**
 * A convenience method which evaluates if [value] is null or not and appends
 * it with the accompanying parameter name in the form of a query parameter.
 *
 * @param value Query parameter value.
 * @param name Query parameter value.
 */
private fun String.appendValueAsQueryParameterIfNotNull(value: Any?, name: String): String {
    val withSeparator = if (this.contains("?")) this else "$this?"

    return if (value != null) {
        "$withSeparator&$name=$value"
    } else {
        this
    }
}
