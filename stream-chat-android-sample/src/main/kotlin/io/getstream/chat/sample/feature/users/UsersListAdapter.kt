package io.getstream.chat.sample.feature.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import io.getstream.chat.sample.R
import io.getstream.chat.sample.data.user.User
import kotlinx.android.synthetic.main.item_user.view.*

class UsersListAdapter(
    val userClickListener: (User) -> Unit,
    val optionsClickListener: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items = mutableListOf<User>()

    fun setUsers(users: List<User>) {
        items.clear()
        items.addAll(users)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_OPTIONS) {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_options, parent, false)
                .let { FooterViewHolder(it) }
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user, parent, false)
                .let { UsersListViewHolder(it) }
        }
    }

    override fun getItemCount(): Int = items.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UsersListViewHolder) {
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

class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view)

class UsersListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bindUser(user: User) {
        itemView.apply {
            name.text = user.name
            Glide.with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .fallback(R.drawable.ic_avatar_placeholder)
                .transform(CircleCrop())
                .into(avatar)
        }
    }
}
