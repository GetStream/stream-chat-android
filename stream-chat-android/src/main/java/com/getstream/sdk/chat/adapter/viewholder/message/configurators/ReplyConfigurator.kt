package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.viewholder.message.getActiveContentViewResId
import com.getstream.sdk.chat.adapter.viewholder.message.isDeleted
import com.getstream.sdk.chat.adapter.viewholder.message.isFailed
import com.getstream.sdk.chat.adapter.viewholder.message.isInThread
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel

internal class ReplyConfigurator(
    private val binding: StreamItemMessageBinding,
    private val context: Context,
    private val style: MessageListViewStyle,
    private val channel: Channel,
    private val messageClickListener: MessageListView.MessageClickListener,
    private val bindingAdapterPosition: () -> Int
) : Configurator {

    override fun configure(messageItem: MessageItem) {
        configReplyView(messageItem)
        configParamsReply(messageItem)
    }

    private fun configReplyView(messageItem: MessageItem) {
        val message = messageItem.message

        val replyCount = message.replyCount

        if (!style.isThreadEnabled ||
            !channel.config.isRepliesEnabled ||
            message.isDeleted() ||
            message.isFailed() ||
            replyCount == 0 ||
            message.isInThread() ||
            (message.id.isEmpty() && bindingAdapterPosition() == 0)
        ) {
            binding.ivReply.isVisible = false
            binding.tvReply.isVisible = false
            return
        }
        binding.ivReply.isVisible = true
        binding.tvReply.isVisible = true
        binding.tvReply.text = context.resources.getQuantityString(
            R.plurals.stream_reply_count,
            replyCount,
            replyCount
        )

        val clickListener: (View) -> Unit = { messageClickListener.onMessageClick(message) }
        binding.ivReply.setOnClickListener(clickListener)
        binding.tvReply.setOnClickListener(clickListener)
    }

    private fun configParamsReply(messageItem: MessageItem) {
        if (binding.ivReply.isGone) {
            return
        }

        // Clear Constraint
        binding.root.updateConstraints {
            clear(R.id.tvReply, ConstraintSet.START)
            clear(R.id.tvReply, ConstraintSet.END)
            clear(R.id.ivReply, ConstraintSet.START)
            clear(R.id.ivReply, ConstraintSet.END)
        }

        val paramsArrow = binding.ivReply.layoutParams as ConstraintLayout.LayoutParams
        val paramsText = binding.tvReply.layoutParams as ConstraintLayout.LayoutParams

        // Set Constraint
        if (messageItem.isTheirs) {
            binding.ivReply.setBackgroundResource(R.drawable.stream_ic_reply_incoming)
            paramsArrow.horizontalBias = 0f
            paramsArrow.startToStart = getActiveContentViewResId(messageItem.message, binding)
            paramsText.startToEnd = binding.ivReply.id
        } else {
            binding.ivReply.setBackgroundResource(R.drawable.stream_ic_reply_outgoing)
            paramsArrow.horizontalBias = 1f
            paramsArrow.endToEnd = getActiveContentViewResId(messageItem.message, binding)
            paramsText.endToStart = binding.ivReply.id
        }
        binding.ivReply.layoutParams = paramsArrow
        binding.tvReply.layoutParams = paramsText
    }
}
