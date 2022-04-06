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
 
package io.getstream.chat.android.ui.message.input.attachment.selected.internal

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.AttachmentConstants
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.databinding.StreamUiItemSelectedAttachmentFileBinding

internal class SelectedFileAttachmentAdapter(
    var onAttachmentCancelled: (AttachmentMetaData) -> Unit = {},
) : SimpleListAdapter<AttachmentMetaData, SelectedFileAttachmentAdapter.SelectedFileAttachmentViewHolder>() {

    internal var attachmentMaxFileSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFileAttachmentViewHolder {
        return StreamUiItemSelectedAttachmentFileBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { SelectedFileAttachmentViewHolder(it, onAttachmentCancelled, attachmentMaxFileSize) }
    }

    class SelectedFileAttachmentViewHolder(
        private val binding: StreamUiItemSelectedAttachmentFileBinding,
        private val onAttachmentCancelled: (AttachmentMetaData) -> Unit,
        private val attachmentMaxFileSize: Long,
    ) : SimpleListAdapter.ViewHolder<AttachmentMetaData>(binding.root) {
        lateinit var attachment: AttachmentMetaData

        init {
            binding.tvClose.setOnClickListener { onAttachmentCancelled(attachment) }
        }

        override fun bind(item: AttachmentMetaData) {
            this.attachment = item

            binding.apply {
                ivFileThumb.loadAttachmentThumb(attachment)
                tvFileSize.text = MediaStringUtil.convertFileSizeByteCount(attachment.size)
                if (item.size > attachmentMaxFileSize) {
                    tvFileTitle.text = context.getString(R.string.stream_ui_message_input_error_file_size)
                    tvFileTitle.setTextColor(ContextCompat.getColor(context, R.color.stream_ui_accent_red))
                } else {
                    tvFileTitle.text = attachment.title
                    tvFileTitle.setTextColor(ContextCompat.getColor(context, R.color.stream_ui_black))
                }
            }
        }
    }
}
