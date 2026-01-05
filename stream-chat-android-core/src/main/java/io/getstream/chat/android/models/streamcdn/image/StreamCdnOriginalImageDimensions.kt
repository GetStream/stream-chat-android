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

import androidx.compose.runtime.Immutable

/**
 * Holds the original width and height information for images
 * hosted by Stream CDN which declare said properties in their URL.
 *
 * @param originalWidth The width of the original image.
 * @param originalHeight The height of the original image.
 */
@Immutable
public data class StreamCdnOriginalImageDimensions(
    val originalWidth: Int,
    val originalHeight: Int,
)
