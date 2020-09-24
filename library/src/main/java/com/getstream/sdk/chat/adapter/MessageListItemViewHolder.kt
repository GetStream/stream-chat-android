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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Space
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.Chat
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.navigation.destinations.WebLinkDestination
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.StringUtility
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.utils.Utils.TextViewLinkHandler
import com.getstream.sdk.chat.view.AttachmentListView
import com.getstream.sdk.chat.view.AvatarView
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.GiphySendListener
import com.getstream.sdk.chat.view.MessageListView.MessageClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListView.ReactionViewClickListener
import com.getstream.sdk.chat.view.MessageListView.ReadStateClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import com.getstream.sdk.chat.view.ReadStateView
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
    resId: Int,
    viewGroup: ViewGroup,
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
    protected val giphySendListener: GiphySendListener
) : BaseMessageListItemViewHolder<MessageItem>(resId, viewGroup) {

    protected val tv_text: TextView = itemView.findViewById(R.id.tv_text)
    protected val rv_reaction: RecyclerView = itemView.findViewById(R.id.reactionsRecyclerView)
    protected val avatar: AvatarView = itemView.findViewById(R.id.avatar)
    protected val iv_tail: ImageView = itemView.findViewById(R.id.iv_tail)
    protected val space_reaction_tail: Space = itemView.findViewById(R.id.space_reaction_tail)
    protected val space_header: Space = itemView.findViewById(R.id.space_header)
    protected val space_same_user: Space = itemView.findViewById(R.id.space_same_user)
    protected val space_reaction: Space = itemView.findViewById(R.id.space_reaction)
    protected val space_attachment: Space = itemView.findViewById(R.id.space_attachment)
    protected val tv_username: TextView = itemView.findViewById(R.id.tv_username)
    protected val tv_messagedate: TextView = itemView.findViewById(R.id.tv_messagedate)
    protected val read_state: ReadStateView<MessageListViewStyle> =
        itemView.findViewById(R.id.read_state)
    protected val pb_deliver: ProgressBar = itemView.findViewById(R.id.pb_deliver)
    protected val iv_deliver: ImageView = itemView.findViewById(R.id.iv_deliver)
    protected val attachmentview: AttachmentListView = itemView.findViewById(R.id.attachmentview)
    protected val iv_reply: ImageView = itemView.findViewById(R.id.iv_reply)
    protected val tv_reply: TextView = itemView.findViewById(R.id.tv_reply)

    protected var bindingPosition = 0
    protected lateinit var message: Message
    protected lateinit var messageListItem: MessageItem
    protected lateinit var positions: List<MessageViewHolderFactory.Position>
    protected lateinit var set: ConstraintSet

    override fun bind(messageListItem: MessageItem, position: Int) {
        this.messageListItem = messageListItem
        this.bindingPosition = position
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
            space_header.visibility = View.VISIBLE
            space_same_user.visibility = View.GONE
        } else {
            space_header.visibility = View.GONE
            space_same_user.visibility = View.VISIBLE
        }
        // Attach Gap
        space_attachment.visibility = attachmentview.visibility

        if (attachmentview.visibility == View.VISIBLE && message.text.isEmpty()) {
            space_attachment.visibility = View.GONE
        }

        // Reaction Gap
        space_reaction.visibility = rv_reaction.visibility

        // ONLY_FOR_DEBUG
        if (false) {
            space_header.setBackgroundResource(R.color.stream_gap_header)
            space_same_user.setBackgroundResource(R.color.stream_gap_message)
            space_attachment.setBackgroundResource(R.color.stream_gap_attach)
            space_reaction.setBackgroundResource(R.color.stream_gap_reaction)
        }
    }

    protected fun configUserAvatar() {
        avatar.visibility = if (isBottomPosition()) View.VISIBLE else View.GONE
        avatar.setUser(message.user, style)
        avatar.setOnClickListener {
            userClickListener.onUserClick(message.user)
        }
    }

    protected fun configUserNameAndMessageDateStyle() {
        if (!isBottomPosition() || (!style.isUserNameShow && !style.isMessageDateShow)) {
            tv_username.visibility = View.GONE
            tv_messagedate.visibility = View.GONE
            return
        }

        if (style.isUserNameShow && messageListItem.isTheirs()) {
            tv_username.visibility = View.VISIBLE
            tv_username.text = message.user.getExtraValue("name", "")
        } else {
            tv_username.visibility = View.GONE
        }

        if (style.isMessageDateShow) {
            tv_messagedate.visibility = View.VISIBLE
            tv_messagedate.text = TIME_DATEFORMAT.format(message.createdAt)
        } else {
            tv_messagedate.visibility = View.GONE
        }
        style.messageUserNameText.apply(tv_username)

        if (messageListItem.isMine) {
            style.messageDateTextMine.apply(tv_messagedate)
        } else {
            style.messageDateTextTheirs.apply(tv_messagedate)
        }
    }

    private fun isTopPosition(): Boolean =
        positions.contains(MessageViewHolderFactory.Position.TOP)

    private fun isBottomPosition(): Boolean =
        positions.contains(MessageViewHolderFactory.Position.BOTTOM)

    protected fun configDeliveredIndicator() {
        iv_deliver.visibility = View.GONE
        pb_deliver.visibility = View.GONE

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
            read_state.visibility = View.GONE
            return
        }

        read_state.visibility = View.VISIBLE
        read_state.setReads(readBy, messageListItem.isTheirs(), style)
        read_state.setOnClickListener {
            readStateClickListener.onReadStateClick(readBy)
        }
    }

    protected fun configMessageText() {
        if (message.text.isEmpty() && !isDeletedMessage()) {
            tv_text.visibility = View.GONE
            return
        }

        tv_text.visibility = View.VISIBLE
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
            tv_text.setText(builder, TextView.BufferType.SPANNABLE)

            return
        }

        val text = StringUtility.getDeletedOrMentionedText(message)
        val markdown = Chat.getInstance().markdown
        markdown.setText(tv_text, text)
    }

    protected fun configMessageTextStyle() {
        if (isDeletedMessage()) {
            tv_text.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources
                    .getDimensionPixelSize(R.dimen.stream_message_deleted_text_font_size)
                    .toFloat()
            )
            tv_text.setTextColor(context.resources.getColor(R.color.stream_gray_dark))
            return
        }

        if (messageListItem.isMine) {
            style.messageTextMine.apply(tv_text)
        } else {
            style.messageTextTheirs.apply(tv_text)
        }

        val messageLinkTextColor = style.getMessageLinkTextColor(messageListItem.isMine)
        if (messageLinkTextColor != 0) {
            tv_text.setLinkTextColor(messageLinkTextColor)
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
            tv_text.background = background
        } else {
            tv_text.setBackgroundResource(0)
        }
    }

    protected var isLongClick = false

    protected fun configMessageTextClickListener() {
        tv_text.setOnClickListener {
            if (isFailedMessage() && !ChatClient.instance().isSocketConnected()) {
                return@setOnClickListener
            }
            messageClickListener.onMessageClick(message)
        }

        tv_text.setOnLongClickListener {
            if (isDeletedMessage() || isFailedMessage()) {
                return@setOnLongClickListener true
            }
            isLongClick = true
            messageLongClickListener.onMessageLongClick(message)
            true
        }

        tv_text.movementMethod = object : TextViewLinkHandler() {
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
            attachmentview.visibility = View.GONE
            return
        }

        attachmentview.visibility = View.VISIBLE
        attachmentview.setViewHolderFactory(viewHolderFactory)
        attachmentview.setStyle(style)
        attachmentview.setGiphySendListener(giphySendListener)
        attachmentview.setEntity(messageListItem)
        attachmentview.setBubbleHelper(bubbleHelper)
        attachmentview.setAttachmentClickListener(attachmentClickListener)
        attachmentview.setLongClickListener(messageLongClickListener)
    }

    protected fun configReactionView() {
        if (isDeletedMessage() ||
            isFailedMessage() ||
            !style.isReactionEnabled ||
            !channel.config.isReactionsEnabled ||
            message.reactionCounts.isEmpty()
        ) {
            rv_reaction.visibility = View.GONE
            iv_tail.visibility = View.GONE
            space_reaction_tail.visibility = View.GONE
            return
        }

        configStyleReactionView()
        rv_reaction.visibility = View.VISIBLE
        iv_tail.visibility = View.VISIBLE
        space_reaction_tail.visibility = View.VISIBLE
        rv_reaction.adapter = ReactionListItemAdapter(
            context,
            message.reactionCounts,
            LlcMigrationUtils.getReactionTypes(),
            style
        )
        rv_reaction.setOnTouchListener { _: View?, event: MotionEvent ->
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
            (bindingPosition == 0 && message.id.isEmpty()) ||
            isDeletedMessage() ||
            isFailedMessage() ||
            replyCount == 0 || isThread()
        ) {
            iv_reply.visibility = View.GONE
            tv_reply.visibility = View.GONE
            return
        }
        iv_reply.visibility = View.VISIBLE
        tv_reply.visibility = View.VISIBLE
        tv_reply.text = tv_reply.context.resources.getQuantityString(
            R.plurals.stream_reply_count,
            replyCount,
            replyCount
        )

        iv_reply.setOnClickListener { messageClickListener.onMessageClick(message) }
        tv_reply.setOnClickListener { messageClickListener.onMessageClick(message) }
    }

    protected fun configMarginStartEnd() {
        configMarginStartEnd_(tv_text)
        configMarginStartEnd_(attachmentview)
        configMarginStartEnd_(iv_reply)
        configMarginStartEnd_(tv_username)
        configMarginStartEnd_(tv_messagedate)
    }

    protected fun configMarginStartEnd_(view: View) {
        val avatarWidth = style.avatarWidth

        val params = view.layoutParams as ConstraintLayout.LayoutParams
        if (view == tv_username) {
            params.leftMargin = Utils.dpToPx(10 + 5) + avatarWidth
            view.layoutParams = params
            return
        }
        if (view == tv_messagedate) {
            params.rightMargin = Utils.dpToPx(15 + 5) + avatarWidth
            view.layoutParams = params
            return
        }
        params.leftMargin = Utils.dpToPx(10 + 5) + avatarWidth
        params.rightMargin = Utils.dpToPx(15 + 5) + avatarWidth
        view.layoutParams = params
    }

    protected fun configParamsMessageText() {
        if (tv_text.visibility != View.VISIBLE) {
            return
        }

        val params = tv_text.layoutParams as ConstraintLayout.LayoutParams
        if (messageListItem.isTheirs()) {
            params.horizontalBias = 0f
        } else {
            params.horizontalBias = 1f
        }
        tv_text.layoutParams = params
    }

    protected fun configParamsMessageDate() {
        if (tv_messagedate.visibility != View.VISIBLE) {
            return
        }

        val params = tv_messagedate.layoutParams as ConstraintLayout.LayoutParams
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
        tv_messagedate.layoutParams = params
    }

    protected fun configParamsReactionSpace() {
        if (iv_tail.visibility != View.VISIBLE) {
            return
        }

        set.clone(itemView as ConstraintLayout)
        set.clear(R.id.space_reaction_tail, ConstraintSet.START)
        set.clear(R.id.space_reaction_tail, ConstraintSet.END)
        set.applyTo(itemView)

        val params = space_reaction_tail.layoutParams as ConstraintLayout.LayoutParams
        val activeContentViewResId = getActiveContentViewResId()
        if (messageListItem.isMine) {
            params.endToStart = activeContentViewResId
        } else {
            params.startToEnd = activeContentViewResId
        }

        space_reaction_tail.layoutParams = params
        rv_reaction.post {
            params.width = rv_reaction.height / 3
            space_reaction_tail.layoutParams = params
        }
    }

    protected fun configParamsReactionTail() {
        if (iv_tail.visibility != View.VISIBLE) {
            return
        }

        set.clone(itemView as ConstraintLayout)
        set.clear(R.id.iv_tail, ConstraintSet.START)
        set.clear(R.id.iv_tail, ConstraintSet.END)
        set.applyTo(itemView)

        val params = iv_tail.layoutParams as ConstraintLayout.LayoutParams
        if (messageListItem.isMine) {
            params.startToStart = space_reaction_tail.id
        } else {
            params.endToEnd = space_reaction_tail.id
        }
        rv_reaction.post {
            params.height = rv_reaction.height
            params.width = rv_reaction.height
            params.topMargin = rv_reaction.height / 3
            iv_tail.layoutParams = params
        }
    }

    protected fun configParamsReactionRecycleView() {
        if (rv_reaction.visibility != View.VISIBLE) {
            return
        }

        rv_reaction.visibility = View.INVISIBLE
        iv_tail.visibility = View.INVISIBLE
        rv_reaction.post {
            if (rv_reaction.visibility == View.GONE) {
                return@post
            }

            set.clone(itemView as ConstraintLayout)
            set.clear(R.id.reactionsRecyclerView, ConstraintSet.START)
            set.clear(R.id.reactionsRecyclerView, ConstraintSet.END)
            set.applyTo(itemView)

            val params = rv_reaction.layoutParams as ConstraintLayout.LayoutParams
            if (message.attachments.isNotEmpty()) {
                if (messageListItem.isMine) {
                    params.startToStart = R.id.space_reaction_tail
                } else {
                    params.endToEnd = R.id.space_reaction_tail
                }
            } else {
                val reactionMargin =
                    context.resources.getDimensionPixelSize(R.dimen.stream_reaction_margin)
                if (tv_text.width + reactionMargin < rv_reaction.width) {
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
            rv_reaction.layoutParams = params
            rv_reaction.visibility = View.VISIBLE
            iv_tail.visibility = View.VISIBLE
            configParamsReadIndicator()
        }
    }

    protected fun configParamsUserAvatar() {
        if (avatar.visibility != View.VISIBLE) {
            return
        }

        val params = avatar.layoutParams as ConstraintLayout.LayoutParams
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
        avatar.layoutParams = params
    }

    protected fun configParamsReply() {
        if (iv_reply.visibility != View.VISIBLE) {
            return
        }

        // Clear Constraint
        set.clone(itemView as ConstraintLayout)
        set.clear(R.id.tv_reply, ConstraintSet.START)
        set.clear(R.id.tv_reply, ConstraintSet.END)
        set.clear(R.id.iv_reply, ConstraintSet.START)
        set.clear(R.id.iv_reply, ConstraintSet.END)
        set.applyTo(itemView)

        val paramsArrow = iv_reply.layoutParams as ConstraintLayout.LayoutParams
        val paramsText = tv_reply.layoutParams as ConstraintLayout.LayoutParams

        // Set Constraint
        if (messageListItem.isTheirs()) {
            iv_reply.setBackgroundResource(R.drawable.stream_ic_reply_incoming)
            paramsArrow.horizontalBias = 0f
            paramsArrow.startToStart = getActiveContentViewResId()
            paramsText.startToEnd = iv_reply.id
        } else {
            iv_reply.setBackgroundResource(R.drawable.stream_ic_reply_outgoing)
            paramsArrow.horizontalBias = 1f
            paramsArrow.endToEnd = getActiveContentViewResId()
            paramsText.endToStart = iv_reply.id
        }
        iv_reply.layoutParams = paramsArrow
        tv_reply.layoutParams = paramsText
    }

    fun configParamsReadIndicator() {
        if (read_state.visibility != View.VISIBLE) {
            return
        }

        set.clone(itemView as ConstraintLayout)
        set.clear(R.id.read_state, ConstraintSet.START)
        set.clear(R.id.read_state, ConstraintSet.END)
        set.clear(R.id.read_state, ConstraintSet.BOTTOM)
        set.applyTo(itemView)

        val params = read_state.layoutParams as ConstraintLayout.LayoutParams
        if (messageListItem.isMine) {
            params.endToStart = getActiveContentViewResId()
        } else {
            params.startToEnd = getActiveContentViewResId()
        }

        params.bottomToBottom = getActiveContentViewResId()
        params.leftMargin = Utils.dpToPx(8)
        params.rightMargin = Utils.dpToPx(8)
        read_state.layoutParams = params
    }

    @IdRes
    protected fun getActiveContentViewResId(): Int {
        return if (message.attachments.isNotEmpty()) attachmentview.id else tv_text.id
    }

    protected fun configStyleReactionView() {
        if (style.reactionViewBgDrawable == -1) {
            rv_reaction.background = DrawableBuilder()
                .rectangle()
                .rounded()
                .solidColor(style.reactionViewBgColor)
                .solidColorPressed(Color.LTGRAY)
                .build()

            if (messageListItem.isMine) {
                iv_tail.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.stream_tail_outgoing
                    )
                )
            } else {
                iv_tail.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.stream_tail_incoming
                    )
                )
            }

            DrawableCompat.setTint(iv_tail.drawable, style.reactionViewBgColor)
        } else {
            val drawable = style.reactionViewBgDrawable
            rv_reaction.background = ContextCompat.getDrawable(context, drawable)
            iv_tail.visibility = View.GONE
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
