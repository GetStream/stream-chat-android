package io.getstream.chat.ui.sample.feature.channel.add.group.header

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.ui.sample.databinding.AddGroupChannelMemberItemBinding
import io.getstream.chat.ui.sample.feature.channel.add.header.MemberClickListener

class AddGroupChannelMembersAdapter :
    ListAdapter<User, AddGroupChannelMembersAdapter.MemberViewHolder>(
        object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    var memberClickListener: MemberClickListener = MemberClickListener { }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        return AddGroupChannelMemberItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { MemberViewHolder(it, memberClickListener) }
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MemberViewHolder(
        private val binding: AddGroupChannelMemberItemBinding,
        private val memberClickListener: MemberClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.deleteMemberButton.setOnClickListener { memberClickListener.onMemberClicked(user) }
            binding.memberAvatar.setUserData(user)
            binding.memberNameTextView.text = user.name
        }
    }
}
