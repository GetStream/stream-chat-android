package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageListItem.Position.MIDDLE
import com.getstream.sdk.chat.adapter.viewholder.message.isDeleted
import com.getstream.sdk.chat.adapter.viewholder.message.isFailed
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.navigation.destinations.WebLinkDestination
import com.getstream.sdk.chat.utils.StringUtility
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Message

internal class MessageTextConfigurator(
    private val binding: StreamItemMessageBinding,
    private val context: Context,
    private val style: MessageListViewStyle,
    private val bubbleHelper: MessageListView.BubbleHelper,
    private val messageClickListener: MessageListView.MessageClickListener,
    public var messageLongClickListener: MessageListView.MessageLongClickListener,
    private val messageRetryListener: MessageListView.MessageRetryListener
) : Configurator {

    override fun configure(
        messageItem: MessageItem
    ) {
        val message = messageItem.message

        if (message.text.isEmpty() && !message.isDeleted()) {
            binding.tvText.isVisible = false
            return
        }

        binding.tvText.isVisible = true
        configMessageTextViewText(message)
        configMessageTextStyle(message, messageItem)
        configMessageTextBackground(message, messageItem)
        configMessageTextClickListener(message)
        configParamsMessageText(messageItem)
    }

    private fun configMessageTextViewText(
        message: Message
    ) {
        if (message.isFailed()) {
            // Set Failed Message Title Text
            val builder = SpannableStringBuilder()
            val failedDes =
                if (message.command.isNullOrEmpty()) {
                    R.string.stream_message_failed_send
                } else {
                    R.string.stream_message_invalid_command
                }
            val str1 = SpannableString(context.resources.getText(failedDes))
            str1.setSpan(ForegroundColorSpan(Color.GRAY), 0, str1.length, 0)
            str1.setSpan(RelativeSizeSpan(0.7f), 0, str1.length, 0)
            builder.append(str1)
            builder.append("\n")

            // Set Failed Message Description Text
            val str2 = SpannableString(message.text)
            builder.append(str2)
            binding.tvText.setText(builder, TextView.BufferType.SPANNABLE)

            return
        }

        val text = StringUtility.getDeletedOrMentionedText(message)
        val markdown = ChatUI.instance().markdown
        markdown.setText(binding.tvText, text)
    }

    private fun configMessageTextStyle(
        message: Message,
        messageItem: MessageItem
    ) {
        if (message.isDeleted()) {
            binding.tvText.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources
                    .getDimensionPixelSize(R.dimen.stream_message_deleted_text_font_size)
                    .toFloat()
            )
            binding.tvText.setTextColor(ContextCompat.getColor(context, R.color.stream_gray_dark))
            return
        }

        if (messageItem.isMine) {
            style.messageTextMine.apply(binding.tvText)
        } else {
            style.messageTextTheirs.apply(binding.tvText)
        }

        val messageLinkTextColor = style.getMessageLinkTextColor(messageItem.isMine)
        if (messageLinkTextColor != 0) {
            binding.tvText.setLinkTextColor(messageLinkTextColor)
        }
    }

    private fun configMessageTextBackground(
        message: Message,
        messageItem: MessageItem
    ) {
        val background: Drawable? = when {
            message.isFailed() -> {
                bubbleHelper.getDrawableForMessage(
                    messageItem.message,
                    messageItem.isMine,
                    messageItem.positions
                )
            }
            message.isDeleted() || StringUtility.isEmoji(message.text) -> {
                null
            }
            else -> {
                if (message.attachments.isNotEmpty()) {
                    bubbleHelper.getDrawableForMessage(
                        messageItem.message,
                        messageItem.isMine,
                        positions = listOf(MIDDLE)
                    )
                } else {
                    bubbleHelper.getDrawableForMessage(
                        messageItem.message,
                        messageItem.isMine,
                        messageItem.positions
                    )
                }
            }
        }

        if (background != null) {
            binding.tvText.background = background
        } else {
            binding.tvText.setBackgroundResource(0)
        }
    }

    private var isLongClick = false

    private fun configMessageTextClickListener(
        message: Message
    ) {
        binding.tvText.setOnClickListener {
            if (message.isFailed() && message.command.isNullOrEmpty()) {
                messageRetryListener.onRetryMessage(message)
            } else {
                messageClickListener.onMessageClick(message)
            }
        }

        binding.tvText.setOnLongClickListener {
            if (message.isDeleted() || message.isFailed()) {
                return@setOnLongClickListener true
            }
            isLongClick = true
            messageLongClickListener.onMessageLongClick(message)
            true
        }

        binding.tvText.movementMethod = object : Utils.TextViewLinkHandler() {
            override fun onLinkClick(url: String) {
                if (message.isDeleted() || message.isFailed()) {
                    return
                }
                if (isLongClick) {
                    isLongClick = false
                    return
                }
                ChatUI.instance().navigator.navigate(WebLinkDestination(url, context))
            }
        }
    }

    private fun configParamsMessageText(messageItem: MessageItem) {
        if (binding.tvText.isGone) {
            return
        }

        binding.tvText.updateLayoutParams<ConstraintLayout.LayoutParams> {
            horizontalBias = if (messageItem.isTheirs) 0f else 1f
        }
    }
}
