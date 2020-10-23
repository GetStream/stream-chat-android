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

class UsersListAdapter(val listener: User.() -> Unit) : RecyclerView.Adapter<UsersListViewHolder>() {
    private var items = mutableListOf<User>()

    fun setUsers(users: List<User>) {
        items.clear()
        items.addAll(users)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UsersListViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: UsersListViewHolder, position: Int) {
        val user = items[position]
        holder.bindUser(user)
        holder.itemView.setOnClickListener {
            listener(user)
        }
    }
}

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
