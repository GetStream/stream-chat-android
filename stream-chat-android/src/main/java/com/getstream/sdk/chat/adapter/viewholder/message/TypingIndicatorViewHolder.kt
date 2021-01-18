package com.getstream.sdk.chat.adapter.viewholder.message

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.updateMargins
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.TypingItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff
import com.getstream.sdk.chat.databinding.StreamItemTypeIndicatorBinding
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.view.AvatarView
import com.getstream.sdk.chat.view.MessageListViewStyle

internal class TypingIndicatorViewHolder(
    parent: ViewGroup,
    private val style: MessageListViewStyle,
    private val binding: StreamItemTypeIndicatorBinding =
        StreamItemTypeIndicatorBinding.inflate(parent.inflater, parent, false),
) : BaseMessageListItemViewHolder<TypingItem>(binding.root) {

    override fun bind(messageListItem: TypingItem, diff: MessageListItemPayloadDiff) {
        binding.llTypingIndicator.visibility = View.VISIBLE
        binding.ivTypingIndicator.visibility = View.VISIBLE
        binding.llTypingIndicator.removeAllViews()

        for ((index, user) in messageListItem.users.withIndex()) {
            val avatarView = AvatarView(context)
            avatarView.setUser(user, style.avatarStyle)

            val height = style.avatarStyle.avatarHeight
            val width = style.avatarStyle.avatarWidth
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

        binding.ivTypingIndicator.load(data = R.raw.stream_typing)
    }
}
