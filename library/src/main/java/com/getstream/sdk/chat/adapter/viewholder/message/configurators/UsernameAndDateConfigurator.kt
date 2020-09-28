package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory.Position.BOTTOM
import com.getstream.sdk.chat.adapter.updateConstraints
import com.getstream.sdk.chat.adapter.viewholder.message.getActiveContentViewResId
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.view.MessageListViewStyle
import java.text.DateFormat
import java.text.SimpleDateFormat

internal class UsernameAndDateConfigurator(
    private val binding: StreamItemMessageBinding,
    private val style: MessageListViewStyle
) : Configurator {

    override fun configure(messageItem: MessageItem) {
        configUserNameAndMessageDateStyle(messageItem)
        configParamsMessageDate(messageItem)
    }

    private fun configUserNameAndMessageDateStyle(messageItem: MessageItem) {
        if (BOTTOM !in messageItem.positions || (!style.isUserNameShow && !style.isMessageDateShow)) {
            binding.tvUsername.visibility = GONE
            binding.tvMessagedate.visibility = GONE
            return
        }

        if (style.isUserNameShow && messageItem.isTheirs()) {
            binding.tvUsername.visibility = VISIBLE
            binding.tvUsername.text = messageItem.message.user.getExtraValue("name", "")
        } else {
            binding.tvUsername.visibility = GONE
        }

        if (style.isMessageDateShow) {
            binding.tvMessagedate.visibility = VISIBLE
            binding.tvMessagedate.text = TIME_DATEFORMAT.format(messageItem.message.createdAt)
        } else {
            binding.tvMessagedate.visibility = GONE
        }
        style.messageUserNameText.apply(binding.tvUsername)

        if (messageItem.isMine) {
            style.messageDateTextMine.apply(binding.tvMessagedate)
        } else {
            style.messageDateTextTheirs.apply(binding.tvMessagedate)
        }
    }

    private fun configParamsMessageDate(messageItem: MessageItem) {
        if (binding.tvMessagedate.isGone) {
            return
        }

        binding.tvMessagedate.updateLayoutParams<ConstraintLayout.LayoutParams> {
            if (!style.isUserNameShow && style.isMessageDateShow) {
                binding.root.updateConstraints {
                    clear(R.id.tv_messagedate, ConstraintSet.START)
                }
                startToStart = getActiveContentViewResId(messageItem.message, binding)
            }
            horizontalBias = if (messageItem.isTheirs()) 0f else 1f
        }
    }

    private companion object {
        val TIME_DATEFORMAT: DateFormat = SimpleDateFormat("HH:mm")
    }
}
