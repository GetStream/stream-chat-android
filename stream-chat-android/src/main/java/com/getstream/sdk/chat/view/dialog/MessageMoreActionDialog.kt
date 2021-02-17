package com.getstream.sdk.chat.view.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.ReactionDialogAdapter
import com.getstream.sdk.chat.databinding.StreamDialogMessageMoreactionBinding
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import top.defaults.drawabletoolbox.DrawableBuilder

public class MessageMoreActionDialog(
    context: Context,
    private val channel: Channel,
    private val message: Message,
    private val currentUser: User,
    private val style: MessageListViewStyle,
    private val onMessageEditHandler: (message: Message) -> Unit,
    private val onMessageDeleteHandler: (message: Message) -> Unit,
    private val onStartThreadHandler: (message: Message) -> Unit,
    private val onMessageFlagHandler: (message: Message) -> Unit,
) : Dialog(context, R.style.DialogTheme) {

    init {
        Utils.hideSoftKeyboard(context)
        window?.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
            setFlags(
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            )
        }
        setupMessageActions()
    }

    private fun setupMessageActions() {
        val binding = StreamDialogMessageMoreactionBinding.inflate(context.inflater).also {
            applyButtonsTextStyle(it)
            applyButtonsIconTint(it)
        }

        style.messageActionButtonsBackground?.let { actionButtonsBackground ->
            binding.messageActionButtons.background = actionButtonsBackground
        }
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        binding.startThreadButton.isVisible = canThreadOnMessage()
        binding.copyMessageButton.isVisible = canCopyOnMessage()
        if (isMessageNotCreatedByCurrentUser()) {
            binding.editMessageButton.visibility = View.GONE
            binding.deleteMessageButton.visibility = View.GONE
            binding.flagMessageButton.setOnClickListener {
                onMessageFlagHandler(message)
                dismiss()
            }
            binding.flagMessageButton.isVisible = style.flagMessageActionEnabled
        } else {
            binding.flagMessageButton.visibility = View.GONE
            binding.editMessageButton.setOnClickListener {
                onMessageEditHandler(message)
                dismiss()
            }
            binding.deleteMessageButton.setOnClickListener {
                onMessageDeleteHandler(message)
                dismiss()
            }
            binding.editMessageButton.isVisible = style.editMessageActionEnabled
            binding.deleteMessageButton.isVisible = style.deleteMessageActionEnabled
        }

        if (canReactOnMessage()) {
            binding.reactionsContainer.background = DrawableBuilder()
                .rectangle()
                .solidColor(style.reactionInputBgColor)
                .cornerRadii(binding.reactionsContainer.height / 2, binding.reactionsContainer.height / 2, 0, 0)
                .build()
            binding.reactionsRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val reactionAdapter = ReactionDialogAdapter(message, style) { dismiss() }
            binding.reactionsRecyclerView.adapter = reactionAdapter
        } else {
            binding.reactionsContainer.visibility = View.GONE
        }

        binding.startThreadButton.apply {
            val enabled = style.startThreadMessageActionEnabled
            isVisible = enabled
            if (enabled) {
                setOnClickListener {
                    onStartThreadHandler(message)
                    dismiss()
                }
            }
        }

        binding.copyMessageButton.apply {
            val enabled = style.copyMessageActionEnabled
            isVisible = enabled
            if (enabled) {
                setOnClickListener {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("label", message.text)
                    clipboard.setPrimaryClip(clip)
                    dismiss()
                }
            }
        }
    }

    private fun applyButtonsTextStyle(binding: StreamDialogMessageMoreactionBinding) {
        style.messageActionButtonsTextStyle.let { textStyle ->
            binding.apply {
                textStyle.apply(startThreadButtonTextView)
                textStyle.apply(copyMessageButtonTextView)
                textStyle.apply(flagMessageButtonTextView)
                textStyle.apply(editMessageButtonTextView)
                textStyle.apply(deleteMessageButtonTextView)
            }
        }
    }

    private fun applyButtonsIconTint(binding: StreamDialogMessageMoreactionBinding) {
        style.messageActionButtonsIconTint.let { tint ->
            binding.apply {
                startThreadButtonImageView.imageTintList = tint
                copyMessageButtonImageView.imageTintList = tint
                flagMessageButtonImageView.imageTintList = tint
                editMessageButtonImageView.imageTintList = tint
                deleteMessageButtonImageView.imageTintList = tint
            }
        }
    }

    private fun isMessageNotCreatedByCurrentUser() = message.user.id != currentUser.id

    private fun canCopyOnMessage(): Boolean {
        return !(
            message.deletedAt != null || // TODO: llc cache
                message.type == ModelType.message_error ||
                message.type == ModelType.message_ephemeral ||
                TextUtils.isEmpty(message.text)
            )
    }

    private fun canThreadOnMessage(): Boolean {
        return (style.isThreadEnabled && channel.config.isRepliesEnabled && message.parentId == null)
    }

    private fun canReactOnMessage(): Boolean {
        return style.isReactionEnabled && channel.config.isReactionsEnabled
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        dismiss()
        return true
    }
}
