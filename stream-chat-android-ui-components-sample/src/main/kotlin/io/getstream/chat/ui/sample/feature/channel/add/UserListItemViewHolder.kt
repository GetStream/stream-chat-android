package io.getstream.chat.ui.sample.feature.channel.add

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.common.extensions.getLastSeenText
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.AddChannelSeparatorItemBinding
import io.getstream.chat.ui.sample.databinding.AddChannelUserItemBinding

abstract class BaseViewHolder<T : UserListItem>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    /**
     * Workaround to allow a downcast of the UserListItem to T
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
    private val userClickListener: AddChannelUsersAdapter.UserClickListener
) : BaseViewHolder<UserListItem.UserItem>(binding.root) {

    private val context: Context
        get() = itemView.context

    override fun bind(item: UserListItem.UserItem) {
        binding.userContainer.setOnClickListener { userClickListener.onUserClick(item.userInfo) }
        with(item.userInfo) {
            binding.userAvatar.setUserData(user)
            binding.nameTextView.text = user.name
            binding.onlineTextView.text = user.getLastSeenText(context)
            binding.checkboxImageView.isVisible = isSelected
        }
    }
}
