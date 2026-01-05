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
import androidx.core.view.isVisible
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMediaAttachmentPreviewBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewViewHolder
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.load
import io.getstream.chat.android.ui.utils.loadAttachmentThumb
import io.getstream.log.taggedLogger

/**
 * The default [AttachmentPreviewFactory] for image and video attachments.
 */
public class MediaAttachmentPreviewFactory : AttachmentPreviewFactory {

    private val logger by taggedLogger("AttachMediaPreviewFactory")

    /**
     * Checks if the factory can create a preview ViewHolder for this attachment.
     *
     * @param attachment The attachment we want to show a preview for.
     * @return True if the factory is able to provide a preview for the given [Attachment].
     */
    public override fun canHandle(attachment: Attachment): Boolean {
        logger.i { "[canHandle] isImage: ${attachment.isImage()}, isVideo: ${attachment.isVideo()}; $attachment" }
        return attachment.isImage() || attachment.isVideo()
    }

    /**
     * Creates and instantiates a new instance of [MediaAttachmentPreviewFactory].
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
        return StreamUiMediaAttachmentPreviewBinding
            .inflate(parentView.context.streamThemeInflater, parentView, false)
            .let { binding ->
                MediaAttachmentPreviewViewHolder(
                    binding = binding,
                    attachmentRemovalListener = attachmentRemovalListener,
                    style = style,
                )
            }
    }

    /**
     * A ViewHolder for image and video attachment previews.
     *
     * @param binding Binding generated for the layout.
     * @param attachmentRemovalListener Click listener for the remove attachment button.
     * @param style Used to style the ViewHolder. If null, the ViewHolder will retain
     * the default appearance.
     */
    private class MediaAttachmentPreviewViewHolder(
        private val binding: StreamUiMediaAttachmentPreviewBinding,
        attachmentRemovalListener: (Attachment) -> Unit,
        private val style: MessageComposerViewStyle?,
    ) : AttachmentPreviewViewHolder(binding.root) {

        private val logger by taggedLogger("AttachMediaPreviewHolder")

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

        /**
         * Applies the style to the View displaying the play button
         * icon.
         */
        private fun setupIconImageView() {
            if (style != null) {
                with(binding.playIconImageView) {
                    val iconDrawable =
                        style.messageInputVideoAttachmentIconDrawable.applyTint(
                            style.messageInputVideoAttachmentIconDrawableTint,
                        )

                    setImageDrawable(iconDrawable)
                    setPaddingRelative(
                        style.messageInputVideoAttachmentIconDrawablePaddingStart,
                        style.messageInputVideoAttachmentIconDrawablePaddingTop,
                        style.messageInputVideoAttachmentIconDrawablePaddingEnd,
                        style.messageInputVideoAttachmentIconDrawablePaddingBottom,
                    )
                }
            }
        }

        /**
         * Applies style to the card holding the View displaying the
         * play button.
         */
        private fun setupIconCard() {
            if (style != null) {
                with(binding.playIconCardView) {
                    style.messageInputVideoAttachmentIconBackgroundColor?.also { backgroundColor ->
                        setCardBackgroundColor(backgroundColor)
                    }
                    elevation = style.messageInputVideoAttachmentIconElevation
                    radius = style.messageInputVideoAttachmentIconCornerRadius
                }
            }
        }

        override fun bind(attachment: Attachment) {
            logger.v { "[bind] isImage: ${attachment.isImage()}, isVideo: ${attachment.isVideo()}; $attachment" }
            this.attachment = attachment
            val upload = attachment.upload

            binding.playIconCardView.isVisible = attachment.isVideo()

            if (upload != null) {
                binding.thumbImageView.load(upload)
            } else {
                binding.thumbImageView.loadAttachmentThumb(attachment)
            }
        }
    }
}
