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

import android.graphics.Color
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.MediaStringUtil
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentMediaAddMoreBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentMediaBinding
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.load
import io.getstream.chat.android.ui.utils.loadVideoThumbnail

/**
 * A [RecyclerView.Adapter] implementation for rendering [MediaAttachmentListItem]s.
 *
 * @param style The style for the attachments picker dialog.
 * @param onAttachmentSelected The action to be invoked when an attachment is selected.
 * @param onAddMoreClick The action to be invoked when the "add more" button is clicked.
 */
internal class MediaAttachmentAdapter(
    private val style: AttachmentsPickerDialogStyle,
    private val onAttachmentSelected: (attachmentMetaData: AttachmentMetaData) -> Unit,
    private val onAddMoreClick: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val attachments: MutableList<MediaAttachmentListItem> = mutableListOf()

    override fun getItemViewType(position: Int): Int {
        return when (attachments[position]) {
            is MediaAttachmentListItem.AddMoreItem -> VIEW_TYPE_ADD_MORE
            is MediaAttachmentListItem.MediaAttachmentItem -> VIEW_TYPE_MEDIA_ATTACHMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_MEDIA_ATTACHMENT) {
            StreamUiItemAttachmentMediaBinding
                .inflate(parent.streamThemeInflater, parent, false)
                .let { MediaAttachmentViewHolder(it, onAttachmentSelected, style) }
        } else {
            // No other possible item types
            StreamUiItemAttachmentMediaAddMoreBinding
                .inflate(parent.streamThemeInflater, parent, false)
                .let { AddMoreViewHolder(it, onAddMoreClick) }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = attachments[position]
        if (item is MediaAttachmentListItem.MediaAttachmentItem && holder is MediaAttachmentViewHolder) {
            holder.bind(item.attachment)
        }
    }

    override fun getItemCount(): Int = attachments.size

    /**
     * Sets the list of [MediaAttachmentListItem]s to be displayed.
     */
    fun setItems(attachments: List<MediaAttachmentListItem>) {
        this.attachments.clear()
        this.attachments.addAll(attachments)
        notifyDataSetChanged()
    }

    /**
     * Marks the given [AttachmentMetaData] as selected.
     */
    fun selectAttachment(attachment: AttachmentMetaData) = toggleAttachmentSelection(attachment, true)

    /**
     * Marks the given [AttachmentMetaData] as not selected.
     */
    fun deselectAttachment(attachment: AttachmentMetaData) = toggleAttachmentSelection(attachment, false)

    /**
     * Clears the list of attachments.
     */
    fun clearAttachments() {
        attachments.clear()
        notifyDataSetChanged()
    }

    private fun toggleAttachmentSelection(attachment: AttachmentMetaData, isSelected: Boolean) {
        val index = attachments.indexOfFirst {
            it is MediaAttachmentListItem.MediaAttachmentItem && it.attachment == attachment
        }
        if (index != -1) {
            (attachments[index] as MediaAttachmentListItem.MediaAttachmentItem).attachment.isSelected = isSelected
            notifyItemChanged(index)
        }
    }

    /**
     * A [RecyclerView.ViewHolder] implementation for rendering the "add more" button.
     */
    class AddMoreViewHolder(
        binding: StreamUiItemAttachmentMediaAddMoreBinding,
        private val onClick: () -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { onClick() }
        }
    }

    /**
     * A [RecyclerView.ViewHolder] implementation for rendering a media attachment.
     */
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
                    style.videoIconDrawable.applyTint(style.videoIconDrawableTint),
                )
                binding.videoLengthTextView.setTextStyle(style.videoLengthTextStyle)
                binding.videoLengthTextView.text = MediaStringUtil.convertVideoLength(attachment.videoLength)
            } else {
                binding.videoInformationConstraintLayout.isVisible = false
                binding.videoLengthTextView.text = ""
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_ADD_MORE = 0
        private const val VIEW_TYPE_MEDIA_ATTACHMENT = 1
    }
}
