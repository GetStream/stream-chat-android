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
import com.getstream.sdk.chat.model.ModelType
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.databinding.StreamUiImageAttachmentPreviewBinding
import io.getstream.chat.android.ui.message.composer.attachment.AttachmentPreviewViewHolder

/**
 * The default [AttachmentPreviewFactory] for image attachments.
 */
@ExperimentalStreamChatApi
public class ImageAttachmentPreviewFactory : AttachmentPreviewFactory {
    /**
     * Checks if the factory can create a preview ViewHolder for this attachment.
     *
     * @param attachment The attachment we want to show a preview for.
     * @return True if the factory is able to provide a preview for the given [Attachment].
     */
    public override fun canHandle(attachment: Attachment): Boolean {
        return attachment.type in mediaAttachmentTypes
    }

    /**
     * Creates and instantiates a new instance of [ImageAttachmentPreviewFactory].
     *
     * @param parentView The parent container.
     * @param attachmentRemovalListener Click listener for the remove attachment button.
     * @return An instance of attachment preview ViewHolder.
     */
    override fun onCreateViewHolder(
        parentView: ViewGroup,
        attachmentRemovalListener: (Attachment) -> Unit,
    ): AttachmentPreviewViewHolder {
        return StreamUiImageAttachmentPreviewBinding
            .inflate(parentView.context.streamThemeInflater, parentView, false)
            .let { ImageAttachmentPreviewViewHolder(it, attachmentRemovalListener) }
    }

    /**
     * A ViewHolder for image attachment preview.
     *
     * @param binding Binding generated for the layout.
     * @param attachmentRemovalListener Click listener for the remove attachment button.
     */
    private class ImageAttachmentPreviewViewHolder(
        private val binding: StreamUiImageAttachmentPreviewBinding,
        attachmentRemovalListener: (Attachment) -> Unit,
    ) : AttachmentPreviewViewHolder(binding.root) {

        private lateinit var attachment: Attachment

        init {
            val cornerRadius = context.getDimension(R.dimen.stream_ui_selected_attachment_corner_radius)
                .toFloat()
            binding.thumbImageView.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCornerSizes(cornerRadius)
                .build()
            binding.removeButton.setOnClickListener { attachmentRemovalListener(attachment) }
        }

        override fun bind(attachment: Attachment) {
            this.attachment = attachment

            binding.thumbImageView.loadAttachmentThumb(attachment)
        }
    }

    private companion object {
        /**
         * Attachment types that represent media content.
         */
        private val mediaAttachmentTypes: Collection<String> = listOf(
            ModelType.attach_image,
            ModelType.attach_giphy
        )
    }
}
