package com.getstream.sdk.chat.adapter.viewholder.message

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.updateMargins
import com.getstream.sdk.chat.ImageLoader.load
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.TypingItem
import com.getstream.sdk.chat.adapter.inflater
import com.getstream.sdk.chat.databinding.StreamItemTypeIndicatorBinding
import com.getstream.sdk.chat.view.AvatarView
import com.getstream.sdk.chat.view.MessageListViewStyle

internal class TypingIndicatorViewHolder(
    parent: ViewGroup,
    private val style: MessageListViewStyle,
    private val binding: StreamItemTypeIndicatorBinding =
        StreamItemTypeIndicatorBinding.inflate(parent.inflater, parent, false)
) : BaseMessageListItemViewHolder<TypingItem>(binding.root) {

    override fun bind(messageListItem: TypingItem) {
        binding.llTypingIndicator.visibility = View.VISIBLE
        binding.ivTypingIndicator.visibility = View.VISIBLE
        binding.llTypingIndicator.removeAllViews()

        for ((index, user) in messageListItem.users.withIndex()) {
            val avatarView = AvatarView(context)
            avatarView.setUser(user, style)

            val height = style.avatarHeight
            val width = style.avatarWidth
            val params = LinearLayout.LayoutParams(width, height).apply {
                updateMargins(
                    left = if (index == 0) 0 else -width / 2,
                    top = 0,
                    right = 0,
                    bottom = 0
                )
            }

            avatarView.layoutParams = params
            binding.llTypingIndicator.addView(avatarView)
        }

        binding.ivTypingIndicator.load(R.raw.stream_typing)
    }
}
