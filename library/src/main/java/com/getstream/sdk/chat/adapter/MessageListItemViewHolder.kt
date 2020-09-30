package com.getstream.sdk.chat.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.getstream.sdk.chat.Chat
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.navigation.destinations.WebLinkDestination
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.StringUtility
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.utils.Utils.TextViewLinkHandler
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.MessageClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListView.ReactionViewClickListener
import com.getstream.sdk.chat.view.MessageListView.ReadStateClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import top.defaults.drawabletoolbox.DrawableBuilder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Arrays

class MessageListItemViewHolder(
    parent: ViewGroup,
    protected val style: MessageListViewStyle,
    protected val channel: Channel,
    protected val viewHolderFactory: AttachmentViewHolderFactory,
    protected val bubbleHelper: BubbleHelper,
    protected val messageClickListener: MessageClickListener,
    protected val messageLongClickListener: MessageLongClickListener,
    protected val attachmentClickListener: AttachmentClickListener,
    protected val reactionViewClickListener: ReactionViewClickListener,
    protected val userClickListener: MessageListView.UserClickListener,
    protected val readStateClickListener: ReadStateClickListener,
    private val binding: StreamItemMessageBinding =
        StreamItemMessageBinding.inflate(parent.inflater, parent, false)
) : BaseMessageListItemViewHolder<MessageItem>(binding.root) {

    protected lateinit var message: Message
    protected lateinit var messageListItem: MessageItem
    protected lateinit var positions: List<MessageViewHolderFactory.Position>
    protected lateinit var set: ConstraintSet

    override fun bind(messageListItem: MessageItem) {
        this.messageListItem = messageListItem
        this.message = messageListItem.message
        this.positions = messageListItem.positions
        this.set = ConstraintSet()

        init()
    }

    protected fun init() {
        configMessageText()
        configAttachmentView()
        configReactionView()
        configReplyView()
        configDeliveredIndicator()
        configReadIndicator()
        configSpaces() // Apply position related style tweaks
        configUserAvatar()
        configUserNameAndMessageDateStyle()
        configMarginStartEnd() // Configure Layout Params
        configParamsMessageText()
        configParamsUserAvatar()
        configParamsReactionSpace()
        configParamsReactionTail()
        configParamsReactionRecycleView()
        configParamsMessageDate()
        configParamsReply()
        configParamsReadIndicator()
    }

    // extra spacing
    protected fun configSpaces() {
        if (isTopPosition()) {
            // TOP
            binding.spaceHeader.visibility = View.VISIBLE
            binding.spaceSameUser.visibility = View.GONE
        } else {
            binding.spaceHeader.visibility = View.GONE
            binding.spaceSameUser.visibility = View.VISIBLE
        }
        // Attach Gap
        binding.spaceAttachment.visibility = binding.attachmentview.visibility

        if (binding.attachmentview.visibility == View.VISIBLE && message.text.isEmpty()) {
            binding.spaceAttachment.visibility = View.GONE
        }

        // Reaction Gap
        binding.spaceReaction.visibility = binding.reactionsRecyclerView.visibility

        // ONLY_FOR_DEBUG
        if (false) {
            binding.spaceHeader.setBackgroundResource(R.color.stream_gap_header)
            binding.spaceSameUser.setBackgroundResource(R.color.stream_gap_message)
            binding.spaceAttachment.setBackgroundResource(R.color.stream_gap_attach)
            binding.spaceReaction.setBackgroundResource(R.color.stream_gap_reaction)
        }
    }

    protected fun configUserAvatar() {
        binding.avatar.visibility = if (isBottomPosition()) View.VISIBLE else View.GONE
        binding.avatar.setUser(message.user, style)
        binding.avatar.setOnClickListener {
            userClickListener.onUserClick(message.user)
        }
    }

    protected fun configUserNameAndMessageDateStyle() {
        if (!isBottomPosition() || (!style.isUserNameShow && !style.isMessageDateShow)) {
            binding.tvUsername.visibility = View.GONE
            binding.tvMessagedate.visibility = View.GONE
            return
        }

        if (style.isUserNameShow && messageListItem.isTheirs()) {
            binding.tvUsername.visibility = View.VISIBLE
            binding.tvUsername.text = message.user.getExtraValue("name", "")
        } else {
            binding.tvUsername.visibility = View.GONE
        }

        if (style.isMessageDateShow) {
            binding.tvMessagedate.visibility = View.VISIBLE
            binding.tvMessagedate.text = TIME_DATEFORMAT.format(message.createdAt)
        } else {
            binding.tvMessagedate.visibility = View.GONE
        }
        style.messageUserNameText.apply(binding.tvUsername)

        if (messageListItem.isMine) {
            style.messageDateTextMine.apply(binding.tvMessagedate)
        } else {
            style.messageDateTextTheirs.apply(binding.tvMessagedate)
        }
    }

    private fun isTopPosition(): Boolean =
        positions.contains(MessageViewHolderFactory.Position.TOP)

    private fun isBottomPosition(): Boolean =
        positions.contains(MessageViewHolderFactory.Position.BOTTOM)

    protected fun configDeliveredIndicator() {
        binding.ivDeliver.visibility = View.GONE
        binding.pbDeliver.visibility = View.GONE

        val lastMessage = LlcMigrationUtils.computeLastMessage(channel)

        if (isDeletedMessage() ||
            isFailedMessage() ||
            lastMessage == null ||
            message.id.isEmpty() ||
            !isBottomPosition() ||
            messageListItem.messageReadBy.isNotEmpty() ||
            !messageListItem.isMine ||
            message.createdAt!!.time < lastMessage.createdAt!!.time ||
            message.type == ModelType.message_ephemeral ||
            isThread() ||
            isEphemeral()
        ) {
            return
        }

        // TODO: llc add sync
        //
        // switch (this.message.getSyncStatus()) {
        //     case Sync.LOCAL_ONLY:
        //         pb_deliver.setVisibility(View.VISIBLE);
        //         iv_deliver.setVisibility(View.GONE);
        //         break;
        //     case Sync.SYNCED:
        //         pb_deliver.setVisibility(View.GONE);
        //         iv_deliver.setVisibility(View.VISIBLE);
        //         break;
        //     case Sync.IN_MEMORY: // Same as LOCAL_FAILED
        //     case Sync.LOCAL_FAILED:
        //         pb_deliver.setVisibility(View.GONE);
        //         iv_deliver.setVisibility(View.GONE);
        //         break;
        // }
    }

    protected fun configReadIndicator() {
        val readBy: List<ChannelUserRead> = messageListItem.messageReadBy

        if (isDeletedMessage() ||
            isFailedMessage() ||
            readBy.isEmpty() ||
            isThread() ||
            isEphemeral()
        ) {
            binding.readState.visibility = View.GONE
            return
        }

        binding.readState.visibility = View.VISIBLE
        binding.readState.setReads(readBy, messageListItem.isTheirs(), style)
        binding.readState.setOnClickListener {
            readStateClickListener.onReadStateClick(readBy)
        }
    }

    protected fun configMessageText() {
        if (message.text.isEmpty() && !isDeletedMessage()) {
            binding.tvText.visibility = View.GONE
            return
        }

        binding.tvText.visibility = View.VISIBLE
        configMessageTextViewText()
        configMessageTextStyle()
        configMessageTextBackground()
        configMessageTextClickListener()
    }

    protected fun configMessageTextViewText() {
        if (isFailedMessage()) {
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
        val markdown = Chat.getInstance().markdown
        markdown.setText(binding.tvText, text)
    }

    protected fun configMessageTextStyle() {
        if (isDeletedMessage()) {
            binding.tvText.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources
                    .getDimensionPixelSize(R.dimen.stream_message_deleted_text_font_size)
                    .toFloat()
            )
            binding.tvText.setTextColor(context.resources.getColor(R.color.stream_gray_dark))
            return
        }

        if (messageListItem.isMine) {
            style.messageTextMine.apply(binding.tvText)
        } else {
            style.messageTextTheirs.apply(binding.tvText)
        }

        val messageLinkTextColor = style.getMessageLinkTextColor(messageListItem.isMine)
        if (messageLinkTextColor != 0) {
            binding.tvText.setLinkTextColor(messageLinkTextColor)
        }
    }

    protected fun configMessageTextBackground() {
        val background: Drawable? = when {
            isFailedMessage() -> {
                bubbleHelper.getDrawableForMessage(
                    messageListItem.message,
                    messageListItem.isMine,
                    messageListItem.positions
                )
            }
            isDeletedMessage() || StringUtility.isEmoji(message.text) -> {
                null
            }
            else -> {
                if (message.attachments.isNotEmpty()) {
                    bubbleHelper.getDrawableForMessage(
                        messageListItem.message,
                        messageListItem.isMine,
                        Arrays.asList(MessageViewHolderFactory.Position.MIDDLE)
                    )
                } else {
                    bubbleHelper.getDrawableForMessage(
                        messageListItem.message,
                        messageListItem.isMine,
                        messageListItem.positions
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

    protected var isLongClick = false

    protected fun configMessageTextClickListener() {
        binding.tvText.setOnClickListener {
            if (isFailedMessage() && !ChatClient.instance().isSocketConnected()) {
                return@setOnClickListener
            }
            messageClickListener.onMessageClick(message)
        }

        binding.tvText.setOnLongClickListener {
            if (isDeletedMessage() || isFailedMessage()) {
                return@setOnLongClickListener true
            }
            isLongClick = true
            messageLongClickListener.onMessageLongClick(message)
            true
        }

        binding.tvText.movementMethod = object : TextViewLinkHandler() {
            override fun onLinkClick(url: String) {
                if (isDeletedMessage() || isFailedMessage()) {
                    return
                }
                if (isLongClick) {
                    isLongClick = false
                    return
                }
                Chat.getInstance().navigator.navigate(WebLinkDestination(url, context))
            }
        }
    }

    protected fun configAttachmentView() {
        val deletedMessage = isDeletedMessage()
        val failedMessage = isFailedMessage()
        val noAttachments = message.attachments.isEmpty()
        if (deletedMessage || failedMessage || noAttachments) {
            ChatLogger.instance.logE(
                tag = javaClass.simpleName,
                message = "attachment hidden: deletedMessage:$deletedMessage, failedMessage:$failedMessage noAttachments:$noAttachments"
            )
            binding.attachmentview.visibility = View.GONE
            return
        }

        binding.attachmentview.visibility = View.VISIBLE
        binding.attachmentview.init(
            viewHolderFactory,
            style,
            bubbleHelper,
            attachmentClickListener,
            messageLongClickListener
        )
        binding.attachmentview.setEntity(messageListItem)
    }

    protected fun configReactionView() {
        if (isDeletedMessage() ||
            isFailedMessage() ||
            !style.isReactionEnabled ||
            !channel.config.isReactionsEnabled ||
            message.reactionCounts.isEmpty()
        ) {
            binding.reactionsRecyclerView.visibility = View.GONE
            binding.ivTail.visibility = View.GONE
            binding.spaceReactionTail.visibility = View.GONE
            return
        }

        configStyleReactionView()
        binding.reactionsRecyclerView.visibility = View.VISIBLE
        binding.ivTail.visibility = View.VISIBLE
        binding.spaceReactionTail.visibility = View.VISIBLE
        binding.reactionsRecyclerView.adapter = ReactionListItemAdapter(
            context,
            message.reactionCounts,
            LlcMigrationUtils.getReactionTypes(),
            style
        )
        binding.reactionsRecyclerView.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                reactionViewClickListener.onReactionViewClick(message)
            }
            false
        }
    }

    protected fun configReplyView() {
        val replyCount = message.replyCount

        if (!style.isThreadEnabled ||
            !channel.config.isRepliesEnabled ||
            (bindingAdapterPosition == 0 && message.id.isEmpty()) ||
            isDeletedMessage() ||
            isFailedMessage() ||
            replyCount == 0 || isThread()
        ) {
            binding.ivReply.visibility = View.GONE
            binding.tvReply.visibility = View.GONE
            return
        }
        binding.ivReply.visibility = View.VISIBLE
        binding.tvReply.visibility = View.VISIBLE
        binding.tvReply.text = context.resources.getQuantityString(
            R.plurals.stream_reply_count,
            replyCount,
            replyCount
        )

        binding.ivReply.setOnClickListener { messageClickListener.onMessageClick(message) }
        binding.tvReply.setOnClickListener { messageClickListener.onMessageClick(message) }
    }

    protected fun configMarginStartEnd() {
        configMarginStartEnd_(binding.tvText)
        configMarginStartEnd_(binding.attachmentview)
        configMarginStartEnd_(binding.ivReply)
        configMarginStartEnd_(binding.tvUsername)
        configMarginStartEnd_(binding.tvMessagedate)
    }

    protected fun configMarginStartEnd_(view: View) {
        val avatarWidth = style.avatarWidth

        val params = view.layoutParams as ConstraintLayout.LayoutParams
        if (view == binding.tvUsername) {
            params.leftMargin = Utils.dpToPx(10 + 5) + avatarWidth
            view.layoutParams = params
            return
        }
        if (view == binding.tvMessagedate) {
            params.rightMargin = Utils.dpToPx(15 + 5) + avatarWidth
            view.layoutParams = params
            return
        }
        params.leftMargin = Utils.dpToPx(10 + 5) + avatarWidth
        params.rightMargin = Utils.dpToPx(15 + 5) + avatarWidth
        view.layoutParams = params
    }

    protected fun configParamsMessageText() {
        if (binding.tvText.visibility != View.VISIBLE) {
            return
        }

        val params = binding.tvText.layoutParams as ConstraintLayout.LayoutParams
        if (messageListItem.isTheirs()) {
            params.horizontalBias = 0f
        } else {
            params.horizontalBias = 1f
        }
        binding.tvText.layoutParams = params
    }

    protected fun configParamsMessageDate() {
        if (binding.tvMessagedate.visibility != View.VISIBLE) {
            return
        }

        val params = binding.tvMessagedate.layoutParams as ConstraintLayout.LayoutParams
        if (!style.isUserNameShow && style.isMessageDateShow) {
            set.clone(itemView as ConstraintLayout)
            set.clear(R.id.tv_messagedate, ConstraintSet.START)
            set.applyTo(itemView)
            params.startToStart = getActiveContentViewResId()
        }
        if (messageListItem.isTheirs()) {
            params.horizontalBias = 0f
        } else {
            params.horizontalBias = 1f
        }
        binding.tvMessagedate.layoutParams = params
    }

    protected fun configParamsReactionSpace() {
        if (binding.ivTail.visibility != View.VISIBLE) {
            return
        }

        set.clone(itemView as ConstraintLayout)
        set.clear(R.id.space_reaction_tail, ConstraintSet.START)
        set.clear(R.id.space_reaction_tail, ConstraintSet.END)
        set.applyTo(itemView)

        val params = binding.spaceReactionTail.layoutParams as ConstraintLayout.LayoutParams
        val activeContentViewResId = getActiveContentViewResId()
        if (messageListItem.isMine) {
            params.endToStart = activeContentViewResId
        } else {
            params.startToEnd = activeContentViewResId
        }

        binding.spaceReactionTail.layoutParams = params
        binding.reactionsRecyclerView.post {
            params.width = binding.reactionsRecyclerView.height / 3
            binding.spaceReactionTail.layoutParams = params
        }
    }

    protected fun configParamsReactionTail() {
        if (binding.ivTail.visibility != View.VISIBLE) {
            return
        }

        set.clone(itemView as ConstraintLayout)
        set.clear(R.id.iv_tail, ConstraintSet.START)
        set.clear(R.id.iv_tail, ConstraintSet.END)
        set.applyTo(itemView)

        val params = binding.ivTail.layoutParams as ConstraintLayout.LayoutParams
        if (messageListItem.isMine) {
            params.startToStart = binding.spaceReactionTail.id
        } else {
            params.endToEnd = binding.spaceReactionTail.id
        }
        binding.reactionsRecyclerView.post {
            params.height = binding.reactionsRecyclerView.height
            params.width = binding.reactionsRecyclerView.height
            params.topMargin = binding.reactionsRecyclerView.height / 3
            binding.ivTail.layoutParams = params
        }
    }

    protected fun configParamsReactionRecycleView() {
        if (binding.reactionsRecyclerView.visibility != View.VISIBLE) {
            return
        }

        binding.reactionsRecyclerView.visibility = View.INVISIBLE
        binding.ivTail.visibility = View.INVISIBLE
        binding.reactionsRecyclerView.post {
            if (binding.reactionsRecyclerView.visibility == View.GONE) {
                return@post
            }

            set.clone(itemView as ConstraintLayout)
            set.clear(R.id.reactionsRecyclerView, ConstraintSet.START)
            set.clear(R.id.reactionsRecyclerView, ConstraintSet.END)
            set.applyTo(itemView)

            val params = binding.reactionsRecyclerView.layoutParams as ConstraintLayout.LayoutParams
            if (message.attachments.isNotEmpty()) {
                if (messageListItem.isMine) {
                    params.startToStart = R.id.space_reaction_tail
                } else {
                    params.endToEnd = R.id.space_reaction_tail
                }
            } else {
                val reactionMargin =
                    context.resources.getDimensionPixelSize(R.dimen.stream_reaction_margin)
                if (binding.tvText.width + reactionMargin < binding.reactionsRecyclerView.width) {
                    if (messageListItem.isMine) {
                        params.endToEnd = R.id.tv_text
                    } else {
                        params.startToStart = R.id.tv_text
                    }
                } else {
                    if (messageListItem.isMine) {
                        params.startToStart = R.id.space_reaction_tail
                    } else {
                        params.endToEnd = R.id.space_reaction_tail
                    }
                }
            }
            binding.reactionsRecyclerView.layoutParams = params
            binding.reactionsRecyclerView.visibility = View.VISIBLE
            binding.ivTail.visibility = View.VISIBLE
            configParamsReadIndicator()
        }
    }

    protected fun configParamsUserAvatar() {
        if (binding.avatar.visibility != View.VISIBLE) {
            return
        }

        val params = binding.avatar.layoutParams as ConstraintLayout.LayoutParams
        var marginStart =
            context.resources.getDimension(R.dimen.stream_message_avatar_margin).toInt()
        if (messageListItem.isTheirs()) {
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.marginStart = marginStart
            params.marginEnd = 0
            params.horizontalBias = 0f
        } else {
            marginStart = Utils.dpToPx(15)
            params.marginStart = 0
            params.marginEnd = marginStart
            params.horizontalBias = 1f
        }
        binding.avatar.layoutParams = params
    }

    protected fun configParamsReply() {
        if (binding.ivReply.visibility != View.VISIBLE) {
            return
        }

        // Clear Constraint
        set.clone(itemView as ConstraintLayout)
        set.clear(R.id.tv_reply, ConstraintSet.START)
        set.clear(R.id.tv_reply, ConstraintSet.END)
        set.clear(R.id.iv_reply, ConstraintSet.START)
        set.clear(R.id.iv_reply, ConstraintSet.END)
        set.applyTo(itemView)

        val paramsArrow = binding.ivReply.layoutParams as ConstraintLayout.LayoutParams
        val paramsText = binding.tvReply.layoutParams as ConstraintLayout.LayoutParams

        // Set Constraint
        if (messageListItem.isTheirs()) {
            binding.ivReply.setBackgroundResource(R.drawable.stream_ic_reply_incoming)
            paramsArrow.horizontalBias = 0f
            paramsArrow.startToStart = getActiveContentViewResId()
            paramsText.startToEnd = binding.ivReply.id
        } else {
            binding.ivReply.setBackgroundResource(R.drawable.stream_ic_reply_outgoing)
            paramsArrow.horizontalBias = 1f
            paramsArrow.endToEnd = getActiveContentViewResId()
            paramsText.endToStart = binding.ivReply.id
        }
        binding.ivReply.layoutParams = paramsArrow
        binding.tvReply.layoutParams = paramsText
    }

    fun configParamsReadIndicator() {
        if (binding.readState.visibility != View.VISIBLE) {
            return
        }

        set.clone(itemView as ConstraintLayout)
        set.clear(R.id.read_state, ConstraintSet.START)
        set.clear(R.id.read_state, ConstraintSet.END)
        set.clear(R.id.read_state, ConstraintSet.BOTTOM)
        set.applyTo(itemView)

        val params = binding.readState.layoutParams as ConstraintLayout.LayoutParams
        if (messageListItem.isMine) {
            params.endToStart = getActiveContentViewResId()
        } else {
            params.startToEnd = getActiveContentViewResId()
        }

        params.bottomToBottom = getActiveContentViewResId()
        params.leftMargin = Utils.dpToPx(8)
        params.rightMargin = Utils.dpToPx(8)
        binding.readState.layoutParams = params
    }

    @IdRes
    protected fun getActiveContentViewResId(): Int {
        return if (message.attachments.isNotEmpty()) binding.attachmentview.id else binding.tvText.id
    }

    protected fun configStyleReactionView() {
        if (style.reactionViewBgDrawable == -1) {
            binding.reactionsRecyclerView.background = DrawableBuilder()
                .rectangle()
                .rounded()
                .solidColor(style.reactionViewBgColor)
                .solidColorPressed(Color.LTGRAY)
                .build()

            if (messageListItem.isMine) {
                binding.ivTail.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.stream_tail_outgoing
                    )
                )
            } else {
                binding.ivTail.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.stream_tail_incoming
                    )
                )
            }

            DrawableCompat.setTint(binding.ivTail.drawable, style.reactionViewBgColor)
        } else {
            val drawable = style.reactionViewBgDrawable
            binding.reactionsRecyclerView.background = ContextCompat.getDrawable(context, drawable)
            binding.ivTail.visibility = View.GONE
        }
    }

    protected fun isDeletedMessage(): Boolean = message.deletedAt != null

    protected fun isFailedMessage(): Boolean {
        return message.syncStatus == SyncStatus.FAILED_PERMANENTLY || message.type == ModelType.message_error
    }

    protected fun isThread(): Boolean = !(message.parentId.isNullOrEmpty())

    protected fun isEphemeral(): Boolean = (message.type == ModelType.message_ephemeral)

    companion object {
        private val TIME_DATEFORMAT: DateFormat = SimpleDateFormat("HH:mm")
    }
}
