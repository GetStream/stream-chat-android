package io.getstream.chat.android.ui.channel.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.databinding.StreamAddChannelMemberItemBinding

internal class AddChannelMembersAdapter :
    ListAdapter<User, AddChannelMembersAdapter.MemberViewHolder>(
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
        return StreamAddChannelMemberItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { MemberViewHolder(it, memberClickListener) }
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MemberViewHolder(
        private val binding: StreamAddChannelMemberItemBinding,
        private val memberClickListener: MemberClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.memberContainer.setOnClickListener { memberClickListener.onMemberClicked(user) }
            binding.memberAvatar.setUserData(user)
            binding.memberNameTextView.text = user.name
        }
    }

    fun interface MemberClickListener {
        fun onMemberClicked(user: User)
    }
}
