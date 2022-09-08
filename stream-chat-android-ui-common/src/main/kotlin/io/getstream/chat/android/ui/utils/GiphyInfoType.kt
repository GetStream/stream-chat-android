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

package io.getstream.chat.android.ui.utils

import androidx.annotation.Px
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.Utils
import io.getstream.chat.android.client.models.Attachment

/**
 * Enum class that holds a value used to obtain Giphy images of different quality level.
 *
 * @param value The name of the type.
 */
public enum class GiphyInfoType(public val value: String) {
    /**
     * Original quality giphy, the largest size to load.
     */
    ORIGINAL("original"),

    /**
     * Lower quality with a fixed height, adjusts width according to the Giphy aspect ratio. Lower size than [ORIGINAL].
     */
    FIXED_HEIGHT("fixed_height"),

    /**
     * Lower quality with a fixed height with width adjusted according to the aspect ratio
     * and played at a lower frame rate. Significantly lower size, but visually less appealing.
     */
    FIXED_HEIGHT_DOWNSAMPLED("fixed_height_downsampled")
}

/**
 * Default width used for Giphy Images if no width metadata is available.
 */
internal const val GIPHY_INFO_DEFAULT_WIDTH_DP: Int = 200

/**
 * Default height used for Giphy Images if no width metadata is available.
 */
internal const val GIPHY_INFO_DEFAULT_HEIGHT_DP: Int = 200

/**
 * Returns an object containing extra information about the Giphy image based
 * on its type.
 *
 * @see GiphyInfoType
 * @see GiphyInfo
 */
public fun Attachment.giphyInfo(field: GiphyInfoType): GiphyInfo? {
    val giphyInfoMap =
        (extraData[ModelType.attach_giphy] as? Map<String, Any>?)?.get(field.value) as? Map<String, String>?

    return giphyInfoMap?.let { map ->
        GiphyInfo(
            url = map["url"] ?: "",
            width = map["width"]?.toInt() ?: Utils.dpToPx(GIPHY_INFO_DEFAULT_WIDTH_DP),
            height = map["height"]?.toInt() ?: Utils.dpToPx(GIPHY_INFO_DEFAULT_HEIGHT_DP)
        )
    }
}

/**
 * Contains extra information about Giphy attachments.
 *
 * @param url Url for the Giphy image.
 * @param width The width of the Giphy image.
 * @param height The height of the Giphy image.
 */
public data class GiphyInfo(
    val url: String,
    @Px val width: Int,
    @Px val height: Int,
)
