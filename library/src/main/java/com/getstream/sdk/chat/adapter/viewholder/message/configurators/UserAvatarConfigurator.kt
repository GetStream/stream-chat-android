package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory.Position.BOTTOM
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListViewStyle

internal class UserAvatarConfigurator(
    private val binding: StreamItemMessageBinding,
    private val context: Context,
    private val style: MessageListViewStyle,
    private val userClickListener: MessageListView.UserClickListener
) : Configurator {

    override fun configure(messageItem: MessageItem) {
        configUserAvatar(messageItem)
        configParamsUserAvatar(messageItem)
    }

    private fun configUserAvatar(messageItem: MessageItem) {
        val isBottomPosition = MessageListItem.Position.Bottom in messageItem.positions
        val message = messageItem.message

        binding.avatar.apply {
            isVisible = isBottomPosition
            setUser(message.user, style)
            setOnClickListener {
                userClickListener.onUserClick(message.user)
            }
        }
    }

    private fun configParamsUserAvatar(messageItem: MessageItem) {
        if (binding.avatar.isGone) {
            return
        }

        binding.avatar.updateLayoutParams<ConstraintLayout.LayoutParams> {
            val avatarMargin =
                context.resources.getDimension(R.dimen.stream_message_avatar_margin).toInt()
            if (messageItem.isTheirs()) {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                marginStart = avatarMargin
                marginEnd = 0
                horizontalBias = 0f
            } else {
                marginStart = Utils.dpToPx(15)
                marginStart = 0
                marginEnd = avatarMargin
                horizontalBias = 1f
            }
        }
    }
}
