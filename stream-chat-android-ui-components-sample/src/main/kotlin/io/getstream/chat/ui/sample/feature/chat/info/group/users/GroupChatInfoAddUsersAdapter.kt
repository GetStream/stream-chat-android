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

package io.getstream.chat.ui.sample.feature.chat.info.group.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.User
import io.getstream.chat.ui.sample.common.appThemeContext
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupAddUsersItemBinding

class GroupChatInfoAddUsersAdapter :
    ListAdapter<User, GroupChatInfoAddUsersAdapter.UserViewHolder>(
        object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
        },
    ) {

    private var userClickListener: UserClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder = ChatInfoGroupAddUsersItemBinding
        .inflate(LayoutInflater.from(parent.context.appThemeContext), parent, false)
        .let(::UserViewHolder)

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setUserClickListener(listener: UserClickListener?) {
        userClickListener = listener
    }

    inner class UserViewHolder(private val binding: ChatInfoGroupAddUsersItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var user: User

        init {
            binding.userContainer.setOnClickListener { userClickListener?.onClick(user) }
        }

        fun bind(user: User) {
            this.user = user
            binding.userAvatarView.setUser(user)
            binding.userNameTextView.text = user.name
        }
    }

    fun interface UserClickListener {
        fun onClick(user: User)
    }
}
