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

import io.getstream.chat.android.ui.message.list.adapter.view.GiphyMediaAttachmentViewStyle

/**
 * Sets the way in which Giphy container
 * sizing is determined.
 */
public enum class GiphySizingMode {

    /**
     * Automatically resizes Giphy containers
     * while respecting the original Giphy image dimension ratio.
     *
     * Setting [GiphyMediaAttachmentViewStyle.sizingMode] to this value will make the
     * container ignore [GiphyMediaAttachmentViewStyle.width], [GiphyMediaAttachmentViewStyle.height]
     * and [GiphyMediaAttachmentViewStyle.dimensionRatio].
     */
    AUTOMATIC_RESIZING,

    /**
     * Sets a fixed size to Giphy containers.
     *
     * You can adjust the size by changing [GiphyMediaAttachmentViewStyle.width],
     * [GiphyMediaAttachmentViewStyle.height] and [GiphyMediaAttachmentViewStyle.dimensionRatio].
     */
    FIXED_SIZE
}
