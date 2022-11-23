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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.media.internal

import android.graphics.Color
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.MediaStringUtil
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentMediaBinding
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.load
import io.getstream.chat.android.ui.utils.loadVideoThumbnail

internal class MediaAttachmentAdapter(
    private val style: AttachmentsPickerDialogStyle,
    private val onAttachmentSelected: (attachmentMetaData: AttachmentMetaData) -> Unit,
) : RecyclerView.Adapter<MediaAttachmentAdapter.MediaAttachmentViewHolder>() {

    private val attachments: MutableList<AttachmentMetaData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaAttachmentViewHolder {
        return StreamUiItemAttachmentMediaBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { MediaAttachmentViewHolder(it, onAttachmentSelected, style) }
    }

    override fun onBindViewHolder(holder: MediaAttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    override fun getItemCount(): Int = attachments.size

    fun setAttachments(attachments: List<AttachmentMetaData>) {
        this.attachments.clear()
        this.attachments.addAll(attachments)
        notifyDataSetChanged()
    }

    fun selectAttachment(attachment: AttachmentMetaData) = toggleAttachmentSelection(attachment, true)

    fun deselectAttachment(attachment: AttachmentMetaData) = toggleAttachmentSelection(attachment, false)

    fun clearAttachments() {
        attachments.clear()
        notifyDataSetChanged()
    }

    private fun toggleAttachmentSelection(attachment: AttachmentMetaData, isSelected: Boolean) {
        val index = attachments.indexOf(attachment)
        if (index != -1) {
            attachments[index].isSelected = isSelected
            notifyItemChanged(index)
        }
    }

    class MediaAttachmentViewHolder(
        private val binding: StreamUiItemAttachmentMediaBinding,
        private val onAttachmentSelected: (attachmentMetaData: AttachmentMetaData) -> Unit,
        private val style: AttachmentsPickerDialogStyle,
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var attachment: AttachmentMetaData

        init {
            binding.root.setOnClickListener { onAttachmentSelected(attachment) }
        }

        fun bind(attachment: AttachmentMetaData) {
            this.attachment = attachment

            bindMediaImage(attachment)
            bindSelectionMark(attachment)
            bindSelectionOverlay(attachment)
            bindAttachmentType(attachment)
        }

        private fun bindMediaImage(attachment: AttachmentMetaData) {
            if (attachment.type == AttachmentType.VIDEO) {
                binding.mediaThumbnailImageView.loadVideoThumbnail(
                    uri = attachment.uri,
                    placeholderResId = R.drawable.stream_ui_placeholder,
                )
                val color = ContextCompat.getColor(itemView.context, R.color.stream_ui_white_smoke)
                binding.mediaThumbnailImageView.setBackgroundColor(color)
            } else {
                binding.mediaThumbnailImageView.load(data = attachment.uri)
                binding.mediaThumbnailImageView.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        private fun bindSelectionMark(attachment: AttachmentMetaData) {
            binding.selectionMarkImageView.isVisible = attachment.isSelected
        }

        private fun bindSelectionOverlay(attachment: AttachmentMetaData) {
            binding.selectionOverlayView.isVisible = attachment.isSelected
        }

        private fun bindAttachmentType(attachment: AttachmentMetaData) {
            if (attachment.type == AttachmentType.VIDEO) {
                binding.videoInformationConstraintLayout.isVisible =
                    style.videoLengthTextVisible || style.videoIconVisible
                binding.videoLengthTextView.isVisible = style.videoLengthTextVisible
                binding.videoLogoImageView.isVisible = style.videoIconVisible
                binding.videoLogoImageView.setImageDrawable(
                    style.videoIconDrawable.applyTint(style.videoIconDrawableTint)
                )
                binding.videoLengthTextView.setTextStyle(style.videoLengthTextStyle)
                binding.videoLengthTextView.text = MediaStringUtil.convertVideoLength(attachment.videoLength)
            } else {
                binding.videoInformationConstraintLayout.isVisible = false
                binding.videoLengthTextView.text = ""
            }
        }
    }
}
