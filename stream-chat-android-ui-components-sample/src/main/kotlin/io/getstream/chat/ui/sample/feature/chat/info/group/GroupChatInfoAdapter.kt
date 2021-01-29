package io.getstream.chat.ui.sample.feature.chat.info.group

import android.view.LayoutInflater
import android.view.ViewGroup
import io.getstream.chat.android.client.models.Member
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
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                    .let { ChatInfoGroupMemberViewHolder(it, memberClickListener) }
            TYPE_MEMBERS_SEPARATOR ->
                ChatInfoMembersSeparatorItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                    .let { ChatInfoMembersSeparatorViewHolder(it, membersSeparatorClickListener) }
            TYPE_EDIT_GROUP_NAME ->
                ChatInfoGroupNameItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
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
