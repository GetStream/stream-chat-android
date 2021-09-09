package io.getstream.chat.ui.sample.feature.user_login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.chat.ui.sample.databinding.ItemOptionsBinding
import io.getstream.chat.ui.sample.databinding.ItemUserBinding

class UserLoginAdapter(
    val userClickListener: (SampleUser) -> Unit,
    val optionsClickListener: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<SampleUser>()

    fun setUsers(users: List<SampleUser>) {
        items.clear()
        items.addAll(users)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_OPTIONS) {
            OptionsViewHolder(ItemOptionsBinding.inflate(inflater, parent, false))
        } else {
            UserViewHolder(ItemUserBinding.inflate(inflater, parent, false))
        }
    }

    override fun getItemCount(): Int = items.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            val user = items[position]
            holder.bindUser(user)
            holder.itemView.setOnClickListener {
                userClickListener(user)
            }
        } else {
            holder.itemView.setOnClickListener {
                optionsClickListener()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size) {
            VIEW_TYPE_OPTIONS
        } else {
            super.getItemViewType(position)
        }
    }

    companion object {
        private const val VIEW_TYPE_OPTIONS = 1
    }
}

class OptionsViewHolder(binding: ItemOptionsBinding) : RecyclerView.ViewHolder(binding.root)

class UserViewHolder(
    private val binding: ItemUserBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bindUser(user: SampleUser) {
        itemView.apply {
            binding.nameTextView.text = user.name
            binding.avatarImageView.setUserData(
                User().apply {
                    image = user.image
                    name = user.name
                    id = user.id
                }
            )
        }
    }
}
