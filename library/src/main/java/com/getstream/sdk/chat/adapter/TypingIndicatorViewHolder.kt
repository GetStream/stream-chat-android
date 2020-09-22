package com.getstream.sdk.chat.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.updateMargins
import com.bumptech.glide.Glide
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.TypingItem
import com.getstream.sdk.chat.view.AvatarView
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel

class TypingIndicatorViewHolder(
    resId: Int,
    viewGroup: ViewGroup,
    private val style: MessageListViewStyle
) : BaseMessageListItemViewHolder<TypingItem>(resId, viewGroup) {

    private val iv_typing_indicator: ImageView = itemView.findViewById(R.id.iv_typing_indicator)
    private val ll_typingusers: LinearLayout = itemView.findViewById(R.id.ll_typing_indicator)

    override fun bind(
        channel: Channel,
        messageListItem: TypingItem,
        bubbleHelper: BubbleHelper,
        position: Int
    ) {
        ll_typingusers.visibility = View.VISIBLE
        iv_typing_indicator.visibility = View.VISIBLE
        ll_typingusers.removeAllViews()

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
            ll_typingusers.addView(avatarView)
        }

        Glide.with(context)
            .asGif()
            .load(R.raw.stream_typing)
            .into(iv_typing_indicator)
    }
}
