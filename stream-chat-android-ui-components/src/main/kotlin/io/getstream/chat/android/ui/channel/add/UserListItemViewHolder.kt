package io.getstream.chat.android.ui.channel.add

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.databinding.StreamAddChannelSeparatorItemBinding
import io.getstream.chat.android.ui.databinding.StreamAddChannelUserItemBinding

internal abstract class BaseViewHolder<T : UserListItem>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    /**
     * Workaround to allow a downcast of the UserListItem to T
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindListItem(userListItem: UserListItem) = bind(userListItem as T)

    protected abstract fun bind(item: T)
}

internal class SeparatorViewHolder(private val binding: StreamAddChannelSeparatorItemBinding) :
    BaseViewHolder<UserListItem.Separator>(binding.root) {

    override fun bind(item: UserListItem.Separator) {
        binding.titleTextView.text = item.letter.toString()
    }
}

internal class UserItemViewHolder(
    private val binding: StreamAddChannelUserItemBinding,
    private val userClickListener: AddChannelUsersAdapter.UserClickListener
) : BaseViewHolder<UserListItem.UserItem>(binding.root) {

    override fun bind(item: UserListItem.UserItem) {
        binding.userContainer.setOnClickListener { userClickListener.onUserClick(item.userInfo) }
        with(item.userInfo) {
            binding.userAvatar.setUserData(user)
            binding.nameTextView.text = user.name
            // Placeholder for now
            binding.onlineTextView.text = "Offline"
            binding.checkboxImageView.isVisible = isSelected
        }
    }
}
