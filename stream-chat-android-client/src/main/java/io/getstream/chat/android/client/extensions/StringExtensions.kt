/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.extensions

import android.net.Uri
import androidx.annotation.FloatRange
import androidx.core.net.toUri
import io.getstream.chat.android.client.streamcdn.StreamCdnResizeImageQueryParameterKeys
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.streamcdn.image.StreamCdnCropImageMode
import io.getstream.chat.android.models.streamcdn.image.StreamCdnOriginalImageDimensions
import io.getstream.chat.android.models.streamcdn.image.StreamCdnResizeImageMode
import io.getstream.log.StreamLog

private val snakeRegex = "_[a-zA-Z]".toRegex()
private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
private val baseUrlRegex = "^(?:https?://)?(.*?)/*$".toRegex()

/**
 * Converts string written in snake case to String in camel case with the first symbol in lower case.
 * For example string "created_at_some_time" is converted to "createdAtSomeTime".
 */
@InternalStreamChatApi
public fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) { matchResult ->
        matchResult.value.replace("_", "").uppercase()
    }
}

/**
 * Converts a string written in lower camel case to a getter method name.
 * Ex: "camelCase" -> "getCamelCase".
 */
@InternalStreamChatApi
public fun String.lowerCamelCaseToGetter(): String = "get${this[0].uppercase()}${this.substring(1)}"

/**
 * Converts String written in camel case to String in snake case.
 * For example string "createdAtSomeTime" is converted to "created_at_some_time".
 */
internal fun String.camelCaseToSnakeCase(): String {
    return camelRegex.replace(this) { "_${it.value}" }.lowercase()
}

/**
 * Checks if the string is a channel id of an anonymous channel.
 * Checks if the string contains "!members".
 */
internal fun String.isAnonymousChannelId(): Boolean = contains("!members")

/**
 * Parses CID of channel to channelType and channelId.
 *
 * @return Pair<String, String> Pair with channelType and channelId.
 * @throws IllegalStateException Throws an exception if format of cid is incorrect.
 */
@Throws(IllegalStateException::class)
public fun String.cidToTypeAndId(): Pair<String, String> {
    check(isNotEmpty()) { "cid can not be empty" }
    check(':' in this) { "cid needs to be in the format channelType:channelId. For example, messaging:123" }
    return checkNotNull(split(":").takeIf { it.size >= 2 }?.let { it.first() to it.last() })
}

/**
 * Returns [StreamCdnOriginalImageDimensions] if the image is hosted by Stream's CDN and is resizable,
 * otherwise returns null.
 *
 * @return Class containing the original width and height dimensions of the image or null.
 */
public fun String.getStreamCdnHostedImageDimensions(): StreamCdnOriginalImageDimensions? {
    return try {
        val imageUri = this.toUri()

        val width = imageUri.getQueryParameter("ow")
            ?.toInt()

        val height = imageUri.getQueryParameter("oh")
            ?.toInt()

        if (height != null && width != null) {
            StreamCdnOriginalImageDimensions(
                originalWidth = width,
                originalHeight = height,
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
    cropMode: StreamCdnCropImageMode? = null,
): String {
    val logger = StreamLog.getLogger("Chat:resizedStreamCdnImageUrl")

    val streamCdnImageDimensions = this.getStreamCdnHostedImageDimensions()

    return if (streamCdnImageDimensions != null) {
        val imageLinkUri = this.toUri()

        if (imageLinkUri.wasImagePreviouslyResized()) {
            logger.w {
                "Image URL already contains resizing parameters. Please apply resizing parameters only to " +
                    "original image URLs."
            }

            return this@createResizedStreamCdnImageUrl
        }

        val resizedWidth: Int = (streamCdnImageDimensions.originalWidth * resizedWidthPercentage).toInt()
        val resizedHeight: Int = (streamCdnImageDimensions.originalHeight * resizedHeightPercentage).toInt()

        val resizedImageUrl = imageLinkUri
            .buildUpon()
            .appendValueAsQueryParameterIfNotNull(
                key = StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZED_WIDTH,
                value = resizedWidth,
            )
            .appendValueAsQueryParameterIfNotNull(
                key = StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZED_HEIGHT,
                value = resizedHeight,
            )
            .appendValueAsQueryParameterIfNotNull(
                key = StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZE_MODE,
                value = resizeMode?.queryParameterName,
            )
            .appendValueAsQueryParameterIfNotNull(
                key = StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_CROP_MODE,
                value = cropMode?.queryParameterName,
            )
            .build()
            .toString()

        logger.i {
            "Resized Stream CDN hosted image URL: $resizedImageUrl"
        }

        resizedImageUrl
    } else {
        logger.i {
            "Image not hosted by Stream's CDN or not containing original width and height query parameters " +
                "was not resized"
        }
        this
    }
}

/**
 * Checks if a string contains resizing related query parameters.
 *
 * @return true if the URL contains resizing parameters, false otherwise.
 */
private fun Uri.wasImagePreviouslyResized(): Boolean =
    queryParameterNames.intersect(
        listOf(
            StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZED_WIDTH,
            StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZED_HEIGHT,
            StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZE_MODE,
            StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_CROP_MODE,
        ),
    ).isNotEmpty()

/**
 * A convenience method which evaluates if [value] is null or not and appends
 * it with the accompanying parameter key name in the form of a query parameter.
 *
 * @param key Query parameter key.
 * @param value Query parameter value.
 */
private fun Uri.Builder.appendValueAsQueryParameterIfNotNull(key: String, value: String?): Uri.Builder {
    return if (value != null) {
        this.appendQueryParameter(key, value)
    } else {
        this
    }
}

/**
 * A convenience method which evaluates if [value] is null or not and appends
 * it with the accompanying parameter key name in the form of a query parameter.
 *
 * @param key Query parameter key.
 * @param value Query parameter value.
 */
private fun Uri.Builder.appendValueAsQueryParameterIfNotNull(key: String, value: Int?): Uri.Builder {
    return if (value != null) {
        this.appendQueryParameter(key, value.toString())
    } else {
        this
    }
}

/**
 * Extracts the base URL from a string.
 * For example, "https://domain.lan/" will return "domain.lan".
 *
 * @return The base URL.
 */
internal fun String.extractBaseUrl(): String =
    baseUrlRegex.matchEntire(this)?.groupValues?.get(1) ?: this
