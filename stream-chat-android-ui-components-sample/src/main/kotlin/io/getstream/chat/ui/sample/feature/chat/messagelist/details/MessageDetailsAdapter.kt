/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.ui.sample.feature.chat.messagelist.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.User
import io.getstream.chat.ui.sample.common.appThemeContext
import io.getstream.chat.ui.sample.databinding.AdapterMessageDetailsReadByBinding
import java.util.Date

class MessageDetailsAdapter : ListAdapter<MessageDetailsItem, MessageDetailsViewHolder<*>>(MessageDetailsItemDiff) {

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageDetailsViewHolder<*> {
        return AdapterMessageDetailsReadByBinding
            .inflate(LayoutInflater.from(parent.context.appThemeContext), parent, false)
            .let(::ReadByViewHolder)
    }

    override fun onBindViewHolder(holder: MessageDetailsViewHolder<*>, position: Int) {
        when (holder) {
            is ReadByViewHolder -> holder.bind(getItem(position) as ReadByItem)
        }
    }
}

sealed class MessageDetailsItem {
    abstract val id: String
}

sealed class MessageDetailsViewHolder<T : MessageDetailsItem>(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: T)
}

class ReadByViewHolder(
    private val binding: AdapterMessageDetailsReadByBinding,
) : MessageDetailsViewHolder<ReadByItem>(binding.root) {
    override fun bind(item: ReadByItem) {
        binding.userAvatarView.setUser(item.user)
        binding.nameTextView.text = item.user.name
        binding.readAtTextView.text = item.lastReadAt.toString()
    }
}

data class ReadByItem(
    val user: User,
    val lastReadAt: Date,
    val lastReadMessageId: String,
) : MessageDetailsItem() {
    override val id: String get() = user.id
}

private object MessageDetailsItemDiff : DiffUtil.ItemCallback<MessageDetailsItem>() {
    override fun areItemsTheSame(oldItem: MessageDetailsItem, newItem: MessageDetailsItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MessageDetailsItem, newItem: MessageDetailsItem): Boolean {
        return oldItem == newItem
    }
}
