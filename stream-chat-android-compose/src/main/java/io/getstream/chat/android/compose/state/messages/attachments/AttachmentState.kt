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

package io.getstream.chat.android.compose.state.messages.attachments

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult

/**
 * Represents the state of Attachment items, used to render and add handlers required for the attachment to work.
 *
 * @param message Data that represents the message information.
 * @param onLongItemClick Handler for a long click on the message item.
 * @param onImagePreviewResult Handler when the user selects an action to scroll to and focus an image.
 * @param onMediaGalleryPreviewResult Handler used when the user selects an action to perform from
 * [io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewActivity].
 */
public data class AttachmentState
@Deprecated(
    message = "Constructor containing parameter 'onImagePreviewResult' has been deprecated, " +
        "please use the constructor that does not have said parameter. Parameter 'onMediaPreviewResult' " +
        "is the replacement for the parameter 'onImagePreviewResult' and is functionally the same.",
    level = DeprecationLevel.WARNING
)
constructor(
    val message: Message,
    val onLongItemClick: (Message) -> Unit = {},
    @Deprecated(
        message = "This parameter has been deprecated. Replace it with" +
            "'AttachmentState.onMediaPreview'.",
        level = DeprecationLevel.WARNING
    )
    val onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
    val onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit,
) {

    /**
     * Represents the state of Attachment items, used to render and add handlers required for the attachment to work.
     *
     * @param message Data that represents the message information.
     * @param onLongItemClick Handler for a long click on the message item.
     * @param onMediaGalleryPreviewResult Handler used when the user selects an action to perform from
     * [io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewActivity].
     */
    public constructor(
        message: Message,
        onLongItemClick: (Message) -> Unit = {},
        onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit,
    ) : this(
        message = message,
        onLongItemClick = onLongItemClick,
        onImagePreviewResult = {},
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
    )
}
