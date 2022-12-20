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
