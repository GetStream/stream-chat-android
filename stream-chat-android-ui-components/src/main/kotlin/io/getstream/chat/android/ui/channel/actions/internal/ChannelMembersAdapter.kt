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

package io.getstream.chat.android.ui.channel.actions.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemChannelMemberBinding

internal class ChannelMembersAdapter(
    private val onMemberClicked: (Member) -> Unit
) : ListAdapter<Member, ChannelMembersAdapter.ChannelMemberViewHolder>(ChannelMembersDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelMemberViewHolder {
        return StreamUiItemChannelMemberBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { ChannelMemberViewHolder(it, onMemberClicked) }
    }

    override fun onBindViewHolder(holder: ChannelMemberViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    object ChannelMembersDiffCallback : DiffUtil.ItemCallback<Member>() {
        override fun areItemsTheSame(oldItem: Member, newItem: Member): Boolean {
            return oldItem.user.id == newItem.user.id
        }

        override fun areContentsTheSame(oldItem: Member, newItem: Member): Boolean {
            return oldItem == newItem
        }
    }

    class ChannelMemberViewHolder(
        private val binding: StreamUiItemChannelMemberBinding,
        private val onMemberClicked: (Member) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        lateinit var member: Member

        init {
            binding.root.setOnClickListener { onMemberClicked(member) }
        }

        fun bind(member: Member) {
            this.member = member
            val user = member.user

            binding.apply {
                avatarView.setUserData(user)
                userNameTextView.text = user.name
            }
        }
    }
}
