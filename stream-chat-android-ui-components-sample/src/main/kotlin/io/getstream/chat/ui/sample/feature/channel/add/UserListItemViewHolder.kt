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

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.utils.extensions.getLastSeenText
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.AddChannelSeparatorItemBinding
import io.getstream.chat.ui.sample.databinding.AddChannelUserItemBinding

abstract class BaseViewHolder<T : UserListItem>(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {

    /**
     * Workaround to allow a downcast of the UserListItem to T.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindListItem(userListItem: UserListItem) = bind(userListItem as T)

    protected abstract fun bind(item: T)
}

class SeparatorViewHolder(private val binding: AddChannelSeparatorItemBinding) :
    BaseViewHolder<UserListItem.Separator>(binding.root) {

    override fun bind(item: UserListItem.Separator) {
        binding.titleTextView.text = if (item.letter == AddChannelViewController.EMPTY_NAME_SYMBOL) {
            itemView.context.getString(R.string.add_channel_empty_user_name_separator)
        } else {
            item.letter.toString()
        }
    }
}

class UserItemViewHolder(
    private val binding: AddChannelUserItemBinding,
    private val userClickListener: AddChannelUsersAdapter.UserClickListener,
) : BaseViewHolder<UserListItem.UserItem>(binding.root) {

    private val context: Context
        get() = itemView.context

    override fun bind(item: UserListItem.UserItem) {
        binding.userContainer.setOnClickListener { userClickListener.onUserClick(item.userInfo) }
        with(item.userInfo) {
            binding.userAvatarView.setUser(user)
            binding.nameTextView.text = user.name
            binding.onlineTextView.text = user.getLastSeenText(context)
            binding.checkboxImageView.isVisible = isSelected
        }
    }
}
