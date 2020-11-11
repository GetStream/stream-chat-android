package io.getstream.chat.android.ui.suggestions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamItemMentionBinding

internal class MentionsAdapter(
    private val onMentionSelected: (User) -> Unit
) : ListAdapter<User, MentionsAdapter.MentionViewHolder>(
    object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionViewHolder {
        return StreamItemMentionBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { MentionViewHolder(it, onMentionSelected) }
    }

    override fun onBindViewHolder(holder: MentionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MentionViewHolder(
        private val binding: StreamItemMentionBinding,
        private val onUserClicked: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.avatarView.setUserData(user)
            binding.usernameTextView.text = user.name
            binding.mentionNameTextView.text = itemView.context.getString(
                R.string.stream_mention_user_name_template,
                user.name.toLowerCase()
            )
            binding.root.setOnClickListener { onUserClicked(user) }
        }
    }
}
