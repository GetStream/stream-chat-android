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

package io.getstream.chat.ui.sample.feature.channel.add.group.selectname

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.User
import io.getstream.chat.ui.sample.common.appThemeContext
import io.getstream.chat.ui.sample.databinding.AddGroupChannelSelectNameMemberItemBinding
import io.getstream.chat.ui.sample.feature.channel.add.group.selectname.AddGroupChannelSelectNameMembersAdapter.DeleteMemberClickListener

class AddGroupChannelSelectNameMembersAdapter :
    ListAdapter<User, AddGroupChannelSelectNameMembersAdapter.MemberViewHolder>(
        object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        },
    ) {

    var deleteMemberClickListener: DeleteMemberClickListener = DeleteMemberClickListener { }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        return AddGroupChannelSelectNameMemberItemBinding
            .inflate(LayoutInflater.from(parent.context.appThemeContext), parent, false)
            .let(::MemberViewHolder)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemberViewHolder(
        private val binding: AddGroupChannelSelectNameMemberItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.deleteMemberButton.setOnClickListener { deleteMemberClickListener.onDeleteMember(user) }
            binding.userAvatarView.setUser(user)
            binding.memberNameTextView.text = user.name
        }
    }

    fun interface DeleteMemberClickListener {
        fun onDeleteMember(member: User)
    }
}
