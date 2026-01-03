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

package io.getstream.chat.ui.sample.feature.chat.info.group

import android.view.LayoutInflater
import android.view.ViewGroup
import io.getstream.chat.android.models.Member
import io.getstream.chat.ui.sample.common.appThemeContext
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupMemberItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupNameItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoMembersSeparatorItemBinding
import io.getstream.chat.ui.sample.feature.chat.info.BaseViewHolder
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoAdapter
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoGroupMemberViewHolder
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoGroupNameViewHolder
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoItem
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoMembersSeparatorViewHolder

class GroupChatInfoAdapter : ChatInfoAdapter() {

    private var memberClickListener: MemberClickListener? = null
    private var membersSeparatorClickListener: MembersSeparatorClickListener? = null
    private var nameChangedListener: NameChangedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_GROUP_MEMBER_ITEM ->
                ChatInfoGroupMemberItemBinding
                    .inflate(LayoutInflater.from(parent.context.appThemeContext), parent, false)
                    .let { ChatInfoGroupMemberViewHolder(it, memberClickListener) }
            TYPE_MEMBERS_SEPARATOR ->
                ChatInfoMembersSeparatorItemBinding
                    .inflate(LayoutInflater.from(parent.context.appThemeContext), parent, false)
                    .let { ChatInfoMembersSeparatorViewHolder(it, membersSeparatorClickListener) }
            TYPE_EDIT_GROUP_NAME ->
                ChatInfoGroupNameItemBinding
                    .inflate(LayoutInflater.from(parent.context.appThemeContext), parent, false)
                    .let { ChatInfoGroupNameViewHolder(it, nameChangedListener) }
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChatInfoItem.MemberItem -> TYPE_GROUP_MEMBER_ITEM
            is ChatInfoItem.MembersSeparator -> TYPE_MEMBERS_SEPARATOR
            is ChatInfoItem.ChannelName -> TYPE_EDIT_GROUP_NAME
            else -> super.getItemViewType(position)
        }
    }

    fun setMemberClickListener(listener: MemberClickListener) {
        memberClickListener = listener
    }

    fun setMembersSeparatorClickListener(listener: MembersSeparatorClickListener?) {
        membersSeparatorClickListener = listener
    }

    fun setNameChangedListener(listener: NameChangedListener?) {
        nameChangedListener = listener
    }

    companion object {
        private const val TYPE_GROUP_MEMBER_ITEM = 10
        private const val TYPE_MEMBERS_SEPARATOR = 11
        private const val TYPE_EDIT_GROUP_NAME = 12
    }

    fun interface MemberClickListener {
        fun onClick(member: Member)
    }

    fun interface MembersSeparatorClickListener {
        fun onClick()
    }

    fun interface NameChangedListener {
        fun onChange(name: String)
    }
}
