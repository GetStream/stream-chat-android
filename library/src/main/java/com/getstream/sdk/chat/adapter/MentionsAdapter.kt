package com.getstream.sdk.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.DataBinderMapperImpl
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.databinding.StreamItemMentionBinding
import com.getstream.sdk.chat.view.BaseStyle
import com.getstream.sdk.chat.view.MessageInputStyle
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name

class MentionsAdapter(private val style: BaseStyle) : ListAdapter<User, MentionViewHolder>(
		object : DiffUtil.ItemCallback<User>() {
			override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id
			override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
		}
) {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionViewHolder =
			MentionViewHolder(
					StreamItemMentionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
					style)
	override fun onBindViewHolder(holder: MentionViewHolder, position: Int) = holder.bind(getItem(position))
}

class MentionViewHolder(private val binding: StreamItemMentionBinding, private val style: BaseStyle)
	: RecyclerView.ViewHolder(binding.root) {

	fun bind(user: User) {
		binding.user = user
		binding.avatar.setUser(user, style)
		binding.tvUsername.text = user.name
		(style as? MessageInputStyle)?.inputBackgroundText?.apply(binding.tvUsername)
	}
}