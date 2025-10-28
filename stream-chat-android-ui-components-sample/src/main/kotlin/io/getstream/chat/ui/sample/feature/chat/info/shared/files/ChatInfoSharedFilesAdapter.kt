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

package io.getstream.chat.ui.sample.feature.chat.info.shared.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.ui.sample.common.appThemeContext
import io.getstream.chat.ui.sample.databinding.ChatInfoSharedFileDateDividerBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoSharedFileItemBinding
import io.getstream.chat.ui.sample.feature.chat.info.shared.SharedAttachment
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ChatInfoSharedFilesAdapter :
    ListAdapter<SharedAttachment, BaseViewHolder<*>>(
        object : DiffUtil.ItemCallback<SharedAttachment>() {
            override fun areItemsTheSame(oldItem: SharedAttachment, newItem: SharedAttachment): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: SharedAttachment, newItem: SharedAttachment): Boolean = oldItem == newItem
        },
    ) {

    private val dateFormat: DateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    private var attachmentClickListener: AttachmentClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> = when (viewType) {
        TYPE_FILE ->
            ChatInfoSharedFileItemBinding
                .inflate(LayoutInflater.from(parent.context.appThemeContext), parent, false)
                .let { ChatInfoSharedFileViewHolder(it, attachmentClickListener) }
        TYPE_FILE_DATE_DIVIDER ->
            ChatInfoSharedFileDateDividerBinding
                .inflate(LayoutInflater.from(parent.context.appThemeContext), parent, false)
                .let { ChatInfoSharedFileDateDividerViewHolder(it, dateFormat) }

        else -> throw IllegalArgumentException("Unhandled view type ($viewType)")
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        holder.bindListItem(getItem(position))
    }

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)) {
        is SharedAttachment.AttachmentItem -> TYPE_FILE
        is SharedAttachment.DateDivider -> TYPE_FILE_DATE_DIVIDER
        else -> throw IllegalStateException("ChatInfoSharedAttachmentsAdapter doesn't support view type: $item")
    }

    fun setAttachmentClickListener(listener: AttachmentClickListener?) {
        attachmentClickListener = listener
    }

    companion object {
        private const val TYPE_FILE = 0
        private const val TYPE_FILE_DATE_DIVIDER = 1
    }

    fun interface AttachmentClickListener {
        fun onClick(attachmentItem: SharedAttachment.AttachmentItem)
    }
}
