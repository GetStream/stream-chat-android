package io.getstream.chat.android.ui.suggestion.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionBinding

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
        return StreamUiItemMentionBinding
            .inflate(parent.inflater, parent, false)
            .let { MentionViewHolder(it, onMentionSelected) }
    }

    override fun onBindViewHolder(holder: MentionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MentionViewHolder(
        private val binding: StreamUiItemMentionBinding,
        private val onUserClicked: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var user: User

        init {
            binding.root.setOnClickListener { onUserClicked(user) }
        }

        fun bind(user: User) {
            this.user = user

            binding.apply {
                avatarView.setUserData(user)
                usernameTextView.text = user.name
                mentionNameTextView.text = itemView.context.getString(
                    R.string.stream_ui_mention_user_name_template,
                    user.name.toLowerCase()
                )
            }
        }
    }
}
