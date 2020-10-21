package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible
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
import java.util.Locale

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
            binding.tvUsername.isVisible = false
            binding.tvMessagedate.isVisible = false
            return
        }

        if (style.isUserNameShow && messageItem.isTheirs) {
            binding.tvUsername.isVisible = true
            binding.tvUsername.text = messageItem.message.user.getExtraValue("name", "")
        } else {
            binding.tvUsername.isVisible = false
        }

        val date = messageItem.message.createdAt ?: messageItem.message.createdLocallyAt

        if (style.isMessageDateShow && date != null) {
            binding.tvMessagedate.isVisible = true
            binding.tvMessagedate.text = TIME_DATEFORMAT.format(date)
        } else {
            binding.tvMessagedate.isVisible = false
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
            horizontalBias = if (messageItem.isTheirs) 0f else 1f
        }
    }

    private companion object {
        val TIME_DATEFORMAT: DateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    }
}
