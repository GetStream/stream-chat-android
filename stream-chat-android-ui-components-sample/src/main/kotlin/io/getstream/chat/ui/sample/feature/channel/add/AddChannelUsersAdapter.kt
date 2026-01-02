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

package io.getstream.chat.ui.sample.feature.channel.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.ui.sample.common.appThemeContext
import io.getstream.chat.ui.sample.databinding.AddChannelSeparatorItemBinding
import io.getstream.chat.ui.sample.databinding.AddChannelUserItemBinding
import io.getstream.chat.ui.sample.feature.channel.add.AddChannelUsersAdapter.UserClickListener

class AddChannelUsersAdapter : ListAdapter<UserListItem, BaseViewHolder<*>>(
    object : DiffUtil.ItemCallback<UserListItem>() {
        override fun areItemsTheSame(oldItem: UserListItem, newItem: UserListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserListItem, newItem: UserListItem): Boolean {
            return oldItem == newItem
        }
    },
) {

    var userClickListener: UserClickListener = UserClickListener { }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_SEPARATOR ->
                AddChannelSeparatorItemBinding
                    .inflate(LayoutInflater.from(parent.context.appThemeContext), parent, false)
                    .let(::SeparatorViewHolder)
            TYPE_USER_ITEM ->
                AddChannelUserItemBinding
                    .inflate(LayoutInflater.from(parent.context.appThemeContext), parent, false)
                    .let { UserItemViewHolder(it, userClickListener) }
            else -> throw IllegalArgumentException("Unhandled add channel user list view type ($viewType)")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        holder.bindListItem(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is UserListItem.Separator -> TYPE_SEPARATOR
            is UserListItem.UserItem -> TYPE_USER_ITEM
        }
    }

    fun interface UserClickListener {
        fun onUserClick(userInfo: UserInfo)
    }

    companion object {
        private const val TYPE_SEPARATOR = 0
        private const val TYPE_USER_ITEM = 1
    }
}
