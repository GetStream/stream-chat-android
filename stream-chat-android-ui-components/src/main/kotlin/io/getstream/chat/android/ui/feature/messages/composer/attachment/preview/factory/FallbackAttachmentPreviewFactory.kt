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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory

import android.view.ViewGroup
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.databinding.StreamUiUnsupportedAttachmentPreviewBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewViewHolder
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.log.taggedLogger

/**
 * A fallback [AttachmentPreviewFactory] for attachments unhandled by other factories.
 */
public class FallbackAttachmentPreviewFactory : AttachmentPreviewFactory {

    private val logger by taggedLogger("AttachFallbackPreviewFactory")

    /**
     * Checks if the factory can create a preview ViewHolder for this attachment.
     *
     * @param attachment The attachment we want to show a preview for.
     * @return True if the factory is able to provide a preview for the given [Attachment].
     */
    override fun canHandle(attachment: Attachment): Boolean {
        logger.i { "[canHandle] isAudioRecording: ${attachment.isAudioRecording()}; $attachment" }
        return true
    }

    /**
     * Creates and instantiates a new instance of [FallbackAttachmentPreviewViewHolder].
     *
     * @param parentView The parent container.
     * @param attachmentRemovalListener Click listener for the remove attachment button.
     * @param style Used to style the factory. If null, the factory will retain
     * the default appearance.
     *
     * @return An instance of attachment preview ViewHolder.
     */
    override fun onCreateViewHolder(
        parentView: ViewGroup,
        attachmentRemovalListener: (Attachment) -> Unit,
        style: MessageComposerViewStyle?,
    ): AttachmentPreviewViewHolder {
        return StreamUiUnsupportedAttachmentPreviewBinding
            .inflate(parentView.context.streamThemeInflater, parentView, false)
            .let { binding ->
                FallbackAttachmentPreviewViewHolder(binding, attachmentRemovalListener)
            }
    }

    /**
     * An empty ViewHolder as we don't display unsupported attachment types.
     *
     * @param binding [StreamUiUnsupportedAttachmentPreviewBinding] generated for the layout.
     * @param attachmentRemovalListener Click listener for the remove attachment button.
     */
    private class FallbackAttachmentPreviewViewHolder(
        private val binding: StreamUiUnsupportedAttachmentPreviewBinding,
        private val attachmentRemovalListener: (Attachment) -> Unit,
    ) : AttachmentPreviewViewHolder(binding.root) {
        override fun bind(attachment: Attachment) {
            binding.titleImageView.text = attachment.title
            binding.removeButton.setOnClickListener { attachmentRemovalListener(attachment) }
        }
    }
}
