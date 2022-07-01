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

package io.getstream.chat.android.ui.message.composer.attachment.factory

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.message.composer.attachment.AttachmentPreviewViewHolder

/**
 * A factory responsible for creating attachment preview ViewHolders.
 */
@ExperimentalStreamChatApi
public interface AttachmentPreviewFactory {
    /**
     * Checks if the factory can create a preview ViewHolder for this attachment.
     *
     * @param attachment The attachment we want to show a preview for.
     * @return True if the factory is able to provide a preview for the given [Attachment].
     */
    public fun canHandle(attachment: Attachment): Boolean

    /**
     * Creates and instantiates a new instance of [AttachmentPreviewViewHolder].
     *
     * @param parentView The parent container.
     * @param attachmentRemovalListener Click listener for the remove attachment button.
     * @return An instance of attachment preview ViewHolder.
     */
    public fun onCreateViewHolder(
        parentView: ViewGroup,
        attachmentRemovalListener: (Attachment) -> Unit,
    ): AttachmentPreviewViewHolder
}
