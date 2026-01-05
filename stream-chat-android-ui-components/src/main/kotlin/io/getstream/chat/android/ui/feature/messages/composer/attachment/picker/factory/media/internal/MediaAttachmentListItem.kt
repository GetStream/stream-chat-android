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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.media.internal

import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData

/**
 * Represents the items that can be displayed in the media attachment list.
 */
internal sealed interface MediaAttachmentListItem {

    /**
     * Represents an item which displays a media (image/video) attachment.
     *
     * @param attachment The attachment meta data required to display the image/video.
     */
    data class MediaAttachmentItem(val attachment: AttachmentMetaData) : MediaAttachmentListItem

    /**
     * Represents an item which displays a button to add more media attachments.
     */
    data object AddMoreItem : MediaAttachmentListItem
}
