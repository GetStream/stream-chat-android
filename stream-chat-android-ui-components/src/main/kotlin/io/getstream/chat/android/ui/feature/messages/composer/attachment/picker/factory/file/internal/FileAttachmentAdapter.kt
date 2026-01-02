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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.file.internal

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.MediaStringUtil
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentFileBinding
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.loadAttachmentThumb

internal class FileAttachmentAdapter(
    private val style: AttachmentsPickerDialogStyle,
    private val onAttachmentSelected: (AttachmentMetaData) -> Unit,
) : RecyclerView.Adapter<FileAttachmentAdapter.FileAttachmentViewHolder>() {

    private var attachments: List<AttachmentMetaData> = emptyList()

    override fun onBindViewHolder(holder: FileAttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    override fun getItemCount(): Int = attachments.size

    fun selectAttachment(attachment: AttachmentMetaData) = toggleSelection(attachment, true)

    fun deselectAttachment(attachment: AttachmentMetaData) = toggleSelection(attachment, false)

    fun setAttachments(attachments: List<AttachmentMetaData>) {
        this.attachments = attachments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileAttachmentViewHolder {
        return StreamUiItemAttachmentFileBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { FileAttachmentViewHolder(it, onAttachmentSelected, style) }
    }

    private fun toggleSelection(attachment: AttachmentMetaData, isSelected: Boolean) {
        val index = attachments.indexOf(attachment)
        if (index != -1) {
            attachments[index].isSelected = isSelected

            if (isSelected) {
                attachments[index].selectedPosition = attachments.count { it.isSelected }
                notifyItemChanged(index)
            } else {
                val prevSelectedPosition = attachments[index].selectedPosition
                attachments[index].selectedPosition = 0
                attachments.filter { it.selectedPosition > prevSelectedPosition }.forEach {
                    it.selectedPosition = it.selectedPosition - 1
                }

                notifyDataSetChanged()
            }
        }
    }

    class FileAttachmentViewHolder(
        private val binding: StreamUiItemAttachmentFileBinding,
        private val onAttachmentClick: (AttachmentMetaData) -> Unit,
        private val style: AttachmentsPickerDialogStyle,
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var attachment: AttachmentMetaData

        init {
            binding.root.setOnClickListener {
                onAttachmentClick(attachment)
            }

            binding.selectionIndicator.setTextColor(style.fileAttachmentItemCheckboxTextColor)
            binding.fileNameTextView.setTextStyle(style.fileAttachmentItemNameTextStyle)
            binding.fileSizeTextView.setTextStyle(style.fileAttachmentItemSizeTextStyle)
        }

        fun bind(attachment: AttachmentMetaData) {
            this.attachment = attachment

            binding.fileTypeImageView.loadAttachmentThumb(attachment)
            binding.fileNameTextView.text = attachment.title
            binding.fileSizeTextView.text = MediaStringUtil.convertFileSizeByteCount(attachment.size)

            binding.selectionIndicator.background = getSelectionIndicatorBackground(attachment.isSelected, style)
            binding.selectionIndicator.isChecked = attachment.isSelected
            binding.selectionIndicator.text = attachment.selectedPosition.takeIf { it > 0 }?.toString() ?: ""
        }

        private fun getSelectionIndicatorBackground(selected: Boolean, style: AttachmentsPickerDialogStyle): Drawable {
            return if (selected) {
                style.fileAttachmentItemCheckboxSelectedDrawable
            } else {
                style.fileAttachmentItemCheckboxDeselectedDrawable
            }
        }
    }
}
