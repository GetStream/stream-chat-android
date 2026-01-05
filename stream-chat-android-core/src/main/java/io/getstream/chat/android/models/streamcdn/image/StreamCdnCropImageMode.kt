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

package io.getstream.chat.android.models.streamcdn.image

/**
 * Sets the cropping strategy when added as the 'crop' query parameter to
 * a Stream CDN hosted image URL. The default crop mode when resizing is
 * [StreamCdnCropImageMode.CENTER].
 *
 * note: Used when the resize strategy is set to [StreamCdnResizeImageMode.CROP].
 *
 * @param queryParameterName The value used to form a query parameter.
 */
public enum class StreamCdnCropImageMode(public val queryParameterName: String) {

    /**
     * Keeps the top area of the image and crops out the rest.
     */
    TOP("top"),

    /**
     * Keeps the bottom area of the image and crops out the rest.
     */
    BOTTOM("bottom"),

    /**
     * Keeps the right area of the image and crops out the rest.
     */
    RIGHT("right"),

    /**
     * Keeps the left area of the image and crops out the rest.
     */
    LEFT("left"),

    /**
     * Keeps the center area of the image and crops out the rest.
     */
    CENTER("center"),
}
