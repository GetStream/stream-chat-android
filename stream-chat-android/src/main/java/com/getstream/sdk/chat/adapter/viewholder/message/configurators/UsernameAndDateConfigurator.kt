package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.viewholder.message.getActiveContentViewResId
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.isNotBottomPosition
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import com.getstream.sdk.chat.utils.formatDate
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.name

internal class UsernameAndDateConfigurator(
    private val binding: StreamItemMessageBinding,
    private val style: MessageListViewStyle,
    private val dateFormatter: DateFormatter
) : Configurator {

    override fun configure(messageItem: MessageItem) {
        configUserNameAndMessageDateStyle(messageItem)
        configParamsMessageDate(messageItem)
    }

    private fun configUserNameAndMessageDateStyle(messageItem: MessageItem) {
        if (messageItem.isNotBottomPosition() || (!style.isUserNameShow && !style.isMessageDateShow)) {
            binding.tvUsername.isVisible = false
            binding.tvMessagedate.isVisible = false
            return
        }

        if (style.isUserNameShow && messageItem.isTheirs) {
            binding.tvUsername.isVisible = true
            binding.tvUsername.text = messageItem.message.user.name
        } else {
            binding.tvUsername.isVisible = false
        }

        val date = messageItem.message.createdAt ?: messageItem.message.createdLocallyAt

        if (style.isMessageDateShow && date != null) {
            binding.tvMessagedate.isVisible = true
            binding.tvMessagedate.text = dateFormatter.formatDate(date)
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
                    clear(R.id.tvMessagedate, ConstraintSet.START)
                }
                startToStart = getActiveContentViewResId(messageItem.message, binding)
            }
            horizontalBias = if (messageItem.isTheirs) 0f else 1f
        }
    }
}
