package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.view.MessageListViewStyle

internal class MarginConfigurator(
    private val binding: StreamItemMessageBinding,
    private val style: MessageListViewStyle
) : Configurator {

    override fun configure(messageItem: MessageItem) {
        configMarginStartEnd(binding.tvText)
        configMarginStartEnd(binding.attachmentview)
        configMarginStartEnd(binding.ivReply)
        configMarginStartEnd(binding.tvUsername)
        configMarginStartEnd(binding.tvMessagedate)
    }

    private fun configMarginStartEnd(view: View) {
        val avatarWidth = style.avatarStyle.avatarWidth

        view.updateLayoutParams<ConstraintLayout.LayoutParams> {
            when (view) {
                binding.tvUsername -> {
                    leftMargin = Utils.dpToPx(10 + 5) + avatarWidth
                }
                binding.tvMessagedate -> {
                    rightMargin = Utils.dpToPx(15 + 5) + avatarWidth
                }
                else -> {
                    leftMargin = Utils.dpToPx(10 + 5) + avatarWidth
                    rightMargin = Utils.dpToPx(15 + 5) + avatarWidth
                }
            }
        }
    }
}
