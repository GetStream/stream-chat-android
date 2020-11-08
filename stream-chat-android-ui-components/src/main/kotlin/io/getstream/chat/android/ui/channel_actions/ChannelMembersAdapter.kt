package io.getstream.chat.android.ui.channel_actions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.databinding.StreamItemChannelMemberBinding

internal class ChannelMembersAdapter(
    private val onMemberClicked: (Member) -> Unit
) : ListAdapter<Member, ChannelMemberViewHolder>(
    object : DiffUtil.ItemCallback<Member>() {
        override fun areItemsTheSame(oldItem: Member, newItem: Member): Boolean {
            return oldItem.user.id == newItem.user.id
        }

        override fun areContentsTheSame(oldItem: Member, newItem: Member): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelMemberViewHolder {
        return StreamItemChannelMemberBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { ChannelMemberViewHolder(it, onMemberClicked) }
    }

    override fun onBindViewHolder(holder: ChannelMemberViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }
}

internal class ChannelMemberViewHolder(
    private val binding: StreamItemChannelMemberBinding,
    private val onMemberClicked: (Member) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(member: Member) {
        binding.apply {
            val user = member.user
            avatarView.setUserData(user)
            avatarView.toggleOnlineIndicatorVisibility(user.online)
            userNameTextView.text = user.name
            root.setOnClickListener { onMemberClicked(member) }
        }
    }
}
