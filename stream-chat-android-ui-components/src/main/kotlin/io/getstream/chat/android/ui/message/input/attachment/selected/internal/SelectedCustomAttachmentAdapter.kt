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

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
internal class SelectedCustomAttachmentAdapter(
    var onAttachmentCancelled: (Attachment) -> Unit = {},
) : RecyclerView.Adapter<BaseSelectedCustomAttachmentViewHolder>() {
    private val attachments: MutableList<Attachment> = mutableListOf()
    lateinit var viewHolderFactory: SelectedCustomAttachmentViewHolderFactory

    internal fun setAttachments(attachments: List<Attachment>) {
        this.attachments.apply {
            clear()
            addAll(attachments)
            notifyDataSetChanged()
        }
    }

    fun clear() {
        attachments.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseSelectedCustomAttachmentViewHolder {
        return viewHolderFactory.createAttachmentViewHolder(attachments, parent)
    }

    override fun onBindViewHolder(holder: BaseSelectedCustomAttachmentViewHolder, position: Int) {
        val attachment = attachments.get(position)
        holder.bind(attachment, onAttachmentCancelled)
    }

    override fun getItemCount(): Int = attachments.size
    fun removeItem(attachment: Attachment) {
        val position = attachments.indexOf(attachment)
        attachments -= attachment
        notifyItemRemoved(position)
    }
}

/**
 * An interface of factory responsible for providing instance of [BaseSelectedCustomAttachmentViewHolder].
 * It's used to provide support for displaying custom attachments previews in [io.getstream.chat.android.ui.message.input.MessageInputView] using
 * [io.getstream.chat.android.ui.message.input.MessageInputView.submitCustomAttachments] function.
 */
@ExperimentalStreamChatApi
public interface SelectedCustomAttachmentViewHolderFactory {
    /**
     * Implementation of this function should return instance of [BaseSelectedCustomAttachmentViewHolder] class.
     */
    public fun createAttachmentViewHolder(attachments: List<Attachment>, parent: ViewGroup): BaseSelectedCustomAttachmentViewHolder
}

/**
 * [RecyclerView.ViewHolder] instance. It can be extended in order to provide custom attachment ViewHolder in MessageInputView.
 * Its instance is provided by [SelectedCustomAttachmentViewHolderFactory].
 */
@ExperimentalStreamChatApi
public abstract class BaseSelectedCustomAttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    public abstract fun bind(attachment: Attachment, onAttachmentCancelled: (Attachment) -> Unit)
}
