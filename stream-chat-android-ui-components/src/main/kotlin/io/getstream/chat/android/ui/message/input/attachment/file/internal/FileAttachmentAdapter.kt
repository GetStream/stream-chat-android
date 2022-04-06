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
 
package io.getstream.chat.android.ui.message.input.attachment.file.internal

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentFileBinding
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle

internal class FileAttachmentAdapter(
    private val style: MessageInputViewStyle,
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
        private val style: MessageInputViewStyle,
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var attachment: AttachmentMetaData

        init {
            binding.run {
                selectionIndicator.setTextColor(style.fileCheckboxTextColor)

                root.setOnClickListener {
                    onAttachmentClick(attachment)
                }

                style.fileNameTextStyle.apply(fileNameTextView)
                style.fileSizeTextStyle.apply(fileSizeTextView)
            }
        }

        fun bind(attachment: AttachmentMetaData) {
            this.attachment = attachment

            binding.apply {
                fileTypeImageView.loadAttachmentThumb(attachment)
                fileNameTextView.text = attachment.title
                fileSizeTextView.text = MediaStringUtil.convertFileSizeByteCount(attachment.size)

                selectionIndicator.background = getSelectionIndicatorBackground(attachment.isSelected, style)
                selectionIndicator.isChecked = attachment.isSelected
                selectionIndicator.text = attachment.selectedPosition.takeIf { it > 0 }?.toString() ?: ""
            }
        }

        private fun getSelectionIndicatorBackground(selected: Boolean, style: MessageInputViewStyle): Drawable {
            return if (selected) {
                style.fileCheckboxSelectedDrawable
            } else {
                style.fileCheckboxDeselectedDrawable
            }
        }
    }
}
