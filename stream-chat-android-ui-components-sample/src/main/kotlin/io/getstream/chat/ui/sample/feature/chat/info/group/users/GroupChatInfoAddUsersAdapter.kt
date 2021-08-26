package io.getstream.chat.ui.sample.feature.chat.info.group.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupAddUsersItemBinding

class GroupChatInfoAddUsersAdapter : ListAdapter<User, GroupChatInfoAddUsersAdapter.UserViewHolder>(
    object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
) {

    private var userClickListener: UserClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return ChatInfoGroupAddUsersItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::UserViewHolder)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setUserClickListener(listener: UserClickListener?) {
        userClickListener = listener
    }

    inner class UserViewHolder(private val binding: ChatInfoGroupAddUsersItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var user: User

        init {
            binding.userContainer.setOnClickListener { userClickListener?.onClick(user) }
        }

        fun bind(user: User) {
            this.user = user
            binding.userAvatarView.setUserData(user)
            binding.userNameTextView.text = user.name
        }
    }

    fun interface UserClickListener {
        fun onClick(user: User)
    }
}
