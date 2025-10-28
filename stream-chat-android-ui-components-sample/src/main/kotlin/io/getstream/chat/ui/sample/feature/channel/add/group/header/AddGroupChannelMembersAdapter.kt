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

package io.getstream.chat.ui.sample.feature.channel.add.group.header

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.User
import io.getstream.chat.ui.sample.common.appThemeContext
import io.getstream.chat.ui.sample.databinding.AddGroupChannelMemberItemBinding
import io.getstream.chat.ui.sample.feature.channel.add.header.MemberClickListener

class AddGroupChannelMembersAdapter :
    ListAdapter<User, AddGroupChannelMembersAdapter.MemberViewHolder>(
        object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
        },
    ) {

    var memberClickListener: MemberClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder = AddGroupChannelMemberItemBinding
        .inflate(LayoutInflater.from(parent.context.appThemeContext), parent, false)
        .let(::MemberViewHolder)

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemberViewHolder(private val binding: AddGroupChannelMemberItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var member: User

        init {
            binding.deleteMemberButton.setOnClickListener { memberClickListener?.onMemberClicked(member) }
        }

        fun bind(user: User) {
            member = user
            binding.userAvatarView.setUser(user)
            binding.memberNameTextView.text = user.name
        }
    }
}
