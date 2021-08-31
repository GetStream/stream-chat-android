package io.getstream.chat.ui.sample.feature.channel.add.group.select_name

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.ui.sample.databinding.AddGroupChannelSelectNameMemberItemBinding
import io.getstream.chat.ui.sample.feature.channel.add.group.select_name.AddGroupChannelSelectNameMembersAdapter.DeleteMemberClickListener

class AddGroupChannelSelectNameMembersAdapter :
    ListAdapter<User, AddGroupChannelSelectNameMembersAdapter.MemberViewHolder>(
        object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    var deleteMemberClickListener: DeleteMemberClickListener = DeleteMemberClickListener { }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        return AddGroupChannelSelectNameMemberItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::MemberViewHolder)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemberViewHolder(
        private val binding: AddGroupChannelSelectNameMemberItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.deleteMemberButton.setOnClickListener { deleteMemberClickListener.onDeleteMember(user) }
            binding.memberAvatar.setUserData(user)
            binding.memberNameTextView.text = user.name
        }
    }

    fun interface DeleteMemberClickListener {
        fun onDeleteMember(member: User)
    }
}
