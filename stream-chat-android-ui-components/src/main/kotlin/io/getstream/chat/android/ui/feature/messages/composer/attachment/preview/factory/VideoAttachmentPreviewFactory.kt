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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory

import android.view.ViewGroup
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.AttachmentType
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiVideoAttachmentPreviewBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewViewHolder
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.load
import io.getstream.chat.android.ui.utils.loadAttachmentThumb

/**
 * The default [AttachmentPreviewFactory] for video attachments.
 */
public class VideoAttachmentPreviewFactory : AttachmentPreviewFactory {

    /**
     * Checks if the factory can create a preview ViewHolder for this attachment.
     *
     * @param attachment The attachment we want to show a preview for.
     * @return True if the factory is able to provide a preview for the given [Attachment].
     */
    override fun canHandle(attachment: Attachment): Boolean {
        return attachment.type == AttachmentType.VIDEO
    }

    /**
     * Creates and instantiates a new instance of [AttachmentPreviewViewHolder]
     * able to preview videos.
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
        return StreamUiVideoAttachmentPreviewBinding
            .inflate(parentView.context.streamThemeInflater, parentView, false)
            .let { VideoAttachmentPreviewViewHolder(it, attachmentRemovalListener, style) }
    }

    /**
     * A ViewHolder for video attachment previews.
     *
     * @param binding Binding generated for the layout.
     * @param attachmentRemovalListener Click listener for the remove attachment button.
     */
    private class VideoAttachmentPreviewViewHolder(
        private val binding: StreamUiVideoAttachmentPreviewBinding,
        attachmentRemovalListener: (Attachment) -> Unit,
        private val style: MessageComposerViewStyle?,
    ) : AttachmentPreviewViewHolder(binding.root) {

        private lateinit var attachment: Attachment

        init {
            val cornerRadius = context.getDimension(R.dimen.stream_ui_selected_attachment_corner_radius)
                .toFloat()
            binding.thumbImageView.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCornerSizes(cornerRadius)
                .build()
            binding.removeButton.setOnClickListener { attachmentRemovalListener(attachment) }
            setupIconImageView()
            setupIconCard()
        }

        private fun setupIconImageView() {
            if (style != null) {
                with(binding.playIconImageView) {
                    val iconDrawable =
                        style.messageInputVideoAttachmentIconDrawable.applyTint(
                            style.messageInputVideoAttachmentIconDrawableTint
                        )

                    load(iconDrawable)
                    setPaddingRelative(
                        style.messageInputVideoAttachmentIconDrawablePaddingStart,
                        style.messageInputVideoAttachmentIconDrawablePaddingTop,
                        style.messageInputVideoAttachmentIconDrawablePaddingEnd,
                        style.messageInputVideoAttachmentIconDrawablePaddingBottom
                    )
                }
            }
        }

        private fun setupIconCard() {
            if (style != null) {
                with(binding.playIconCardView) {
                    style.messageInputVideoAttachmentIconBackgroundColor?.also { backgroundColor ->
                        setCardBackgroundColor(backgroundColor)
                    }
                    elevation = style.messageInputVideoAttachmentIconElevation
                }
            }
        }

        override fun bind(attachment: Attachment) {
            this.attachment = attachment
            val upload = attachment.upload

            if (upload != null) {
                binding.thumbImageView.load(upload)
            } else {
                binding.thumbImageView.loadAttachmentThumb(attachment)
            }
        }
    }
}
