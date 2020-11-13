package io.getstream.chat.android.ui.channel.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.ui.databinding.StreamAddChannelSeparatorItemBinding
import io.getstream.chat.android.ui.databinding.StreamAddChannelUserItemBinding

internal class AddChannelUsersAdapter : ListAdapter<UserListItem, BaseViewHolder<*>>(
    object : DiffUtil.ItemCallback<UserListItem>() {
        override fun areItemsTheSame(oldItem: UserListItem, newItem: UserListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserListItem, newItem: UserListItem): Boolean {
            return oldItem == newItem
        }
    }
) {

    var userClickListener: UserClickListener = UserClickListener { }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_SEPARATOR ->
                StreamAddChannelSeparatorItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                    .let(::SeparatorViewHolder)
            TYPE_USER_ITEM ->
                StreamAddChannelUserItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
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
