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
 * Sets the resizing mode when added as the 'resize' query parameter to
 * a Stream CDN hosted image URL. The default CDN resizing mode is [CLIP].
 *
 * @param queryParameterName The value used to form a query parameter.
 */
public enum class StreamCdnResizeImageMode(public val queryParameterName: String) {

    /**
     * Make the image as large as possible, while maintaining the aspect ratio and
     * keeping the height and width less than or equal to the given height and width.
     */
    CLIP("clip"),

    /**
     * Crop the image to the given dimensions.
     * Use [StreamCdnCropImageMode] to give focus to a specific portion of the image.
     */
    CROP("crop"),

    /**
     * Make the image as large as possible, while maintaining the aspect ratio and keeping the height and width
     * less than or equal to the given height and width. Fill any leftover space with a black background.
     */
    FILL("fill"),

    /**
     * Ignore the aspect ratio, and resize the image to the given height and width.
     */
    SCALE("scale"),
}
