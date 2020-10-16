package com.getstream.sdk.chat.view.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.ReactionDialogAdapter
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.view.MessageListViewStyle
import com.getstream.sdk.chat.view.common.visible
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import kotlinx.android.synthetic.main.stream_dialog_message_moreaction.*
import top.defaults.drawabletoolbox.DrawableBuilder

class MessageMoreActionDialog(
    context: Context,
    private val channel: Channel,
    private val message: Message,
    private val currentUser: User,
    private val style: MessageListViewStyle,
    private val onMessageEditHandler: (message: Message) -> Unit,
    private val onMessageDeleteHandler: (message: Message) -> Unit,
    private val onStartThreadHandler: (message: Message) -> Unit,
    private val onMessageFlagHandler: (message: Message) -> Unit
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
        setContentView(R.layout.stream_dialog_message_moreaction)
        setCanceledOnTouchOutside(true)
        startThreadButton.visible(canThreadOnMessage())
        copyMessageButton.visible(canCopyOnMessage())
        if (isMessageCreatedByCurrentUser()) {
            editMessageButton.visibility = View.GONE
            deleteMessageButton.visibility = View.GONE
            flagMessageButton.setOnClickListener {
                onMessageFlagHandler(message)
                dismiss()
            }
        } else {
            flagMessageButton.visibility = View.GONE
            editMessageButton.setOnClickListener {
                onMessageEditHandler(message)
                dismiss()
            }
            deleteMessageButton.setOnClickListener {
                onMessageDeleteHandler(message)
                dismiss()
            }
        }

        if (canReactOnMessage()) {
            reactionsContainer.background = DrawableBuilder()
                .rectangle()
                .solidColor(style.reactionInputBgColor)
                .cornerRadii(reactionsContainer.height / 2, reactionsContainer.height / 2, 0, 0)
                .build()
            reactionsRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val reactionAdapter = ReactionDialogAdapter(message, style) { dismiss() }
            reactionsRecyclerView.adapter = reactionAdapter
        } else {
            reactionsContainer.visibility = View.GONE
        }
        startThreadButton.setOnClickListener {
            onStartThreadHandler(message)
            dismiss()
        }
        copyMessageButton.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", message.text)
            clipboard.setPrimaryClip(clip)
            dismiss()
        }
    }

    private fun isMessageCreatedByCurrentUser() = message.user.id != currentUser.id

    private fun canCopyOnMessage(): Boolean {
        return !(
            message.deletedAt != null || // TODO: llc cache
                message.type == ModelType.message_error || message.type == ModelType.message_ephemeral || TextUtils.isEmpty(
                message.text
            )
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
