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

package io.getstream.chat.ui.sample.feature.chat.info.shared.files

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.utils.MediaStringUtil
import io.getstream.chat.ui.sample.databinding.ChatInfoSharedFileDateDividerBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoSharedFileItemBinding
import io.getstream.chat.ui.sample.feature.chat.info.shared.SharedAttachment
import java.text.DateFormat

abstract class BaseViewHolder<T : SharedAttachment>(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {

    /**
     * Workaround to allow a downcast of the SharedAttachment to T.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindListItem(item: SharedAttachment) = bind(item as T)

    protected abstract fun bind(item: T)
}

class ChatInfoSharedFileViewHolder(
    private val binding: ChatInfoSharedFileItemBinding,
    attachmentClickListener: ChatInfoSharedFilesAdapter.AttachmentClickListener?,
) : BaseViewHolder<SharedAttachment.AttachmentItem>(binding.root) {

    private lateinit var attachmentItem: SharedAttachment.AttachmentItem

    init {
        binding.fileItemContainer.setOnClickListener { attachmentClickListener?.onClick(attachmentItem) }
    }

    override fun bind(item: SharedAttachment.AttachmentItem) {
        attachmentItem = item
        with(item.attachment) {
            binding.fileTypeImageView.setImageResource(ChatUI.mimeTypeIconProvider.getIconRes(mimeType))
            binding.fileNameTextView.text = name
            binding.fileSizeTextView.text = MediaStringUtil.convertFileSizeByteCount(fileSize.toLong())
        }
    }
}

class ChatInfoSharedFileDateDividerViewHolder(
    private val binding: ChatInfoSharedFileDateDividerBinding,
    private val dateFormat: DateFormat,
) : BaseViewHolder<SharedAttachment.DateDivider>(binding.root) {

    override fun bind(item: SharedAttachment.DateDivider) {
        binding.dateLabel.text = dateFormat.format(item.date)
    }
}
