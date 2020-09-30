package com.getstream.sdk.chat.adapter.viewholder.attachment

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.AttachmentListItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.GiphySendListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import top.defaults.drawabletoolbox.DrawableBuilder

class AttachmentViewHolderMedia(
    resId: Int,
    parent: ViewGroup,
    private val giphySendListener: GiphySendListener
) : BaseAttachmentViewHolder(resId, parent) {

    private val iv_media_thumb: PorterShapeImageView = itemView.findViewById(R.id.iv_media_thumb)
    private val tv_media_title: TextView = itemView.findViewById(R.id.tv_media_title)
    private val tv_media_play: TextView = itemView.findViewById(R.id.tv_media_play)
    private val tv_media_des: TextView = itemView.findViewById(R.id.tv_media_des)
    private val iv_command_logo: ImageView = itemView.findViewById(R.id.iv_command_logo)
    private val cl_des: ConstraintLayout = itemView.findViewById(R.id.cl_des)
    private val cl_action: ConstraintLayout = itemView.findViewById(R.id.cl_action)
    private val tv_action_send: TextView = itemView.findViewById(R.id.tv_action_send)
    private val tv_action_shuffle: TextView = itemView.findViewById(R.id.tv_action_shuffle)
    private val tv_action_cancel: TextView = itemView.findViewById(R.id.tv_action_cancel)
    private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

    private lateinit var context: Context
    private lateinit var message: Message
    private lateinit var messageListItem: MessageItem
    private lateinit var style: MessageListViewStyle
    private lateinit var bubbleHelper: BubbleHelper

    private var clickListener: AttachmentClickListener? = null
    private var longClickListener: MessageLongClickListener? = null

    private lateinit var attachment: Attachment

    override fun bind(
        context: Context,
        messageListItem: MessageItem,
        message: Message,
        attachmentListItem: AttachmentListItem,
        style: MessageListViewStyle,
        bubbleHelper: BubbleHelper,
        clickListener: AttachmentClickListener?,
        longClickListener: MessageLongClickListener?
    ) {
        this.context = context
        this.messageListItem = messageListItem
        this.message = message
        this.style = style
        this.bubbleHelper = bubbleHelper
        this.clickListener = clickListener
        this.longClickListener = longClickListener

        attachment = attachmentListItem.attachment

        applyStyle()
        configMediaAttach()
        configActions()
        configClickListeners()
    }

    private fun applyStyle() {
        if (messageListItem.isMine) {
            style.attachmentTitleTextMine.apply(tv_media_title)
            style.attachmentDescriptionTextMine.apply(tv_media_des)
        } else {
            style.attachmentTitleTextTheirs.apply(tv_media_title)
            style.attachmentDescriptionTextTheirs.apply(tv_media_des)
        }
    }

    private fun configMediaAttach() {
        val type = attachment.type
        configImageThumbBackground()

        val thumbUrl = attachment.thumbUrl
        val imageUrl = if (thumbUrl.isNullOrEmpty()) attachment.imageUrl else thumbUrl

        Glide.with(context)
            .asDrawable()
            .load(imageUrl)
            .placeholder(R.drawable.stream_placeholder)
            .into(iv_media_thumb)

        if (message.type != ModelType.message_ephemeral) {
            tv_media_title.text = attachment.title
        }

        tv_media_des.text = attachment.text
        tv_media_title.isVisible = !attachment.title.isNullOrEmpty()
        tv_media_des.isVisible = !attachment.text.isNullOrEmpty()
        tv_media_play.isVisible = type == ModelType.attach_video
        iv_command_logo.isVisible = type == ModelType.attach_giphy

        if (tv_media_des.isVisible || tv_media_title.isVisible) {
            val background = bubbleHelper.getDrawableForAttachmentDescription(
                messageListItem.message, messageListItem.isMine, messageListItem.positions
            )
            cl_des.background = background
        }
    }

    private fun configImageThumbBackground() {
        val background = bubbleHelper.getDrawableForAttachment(
            message,
            messageListItem.isMine,
            messageListItem.positions,
            attachment
        )
        iv_media_thumb.setShape(context, background)
    }

    private fun configActions() {
        if (message.type == ModelType.message_ephemeral && message.command == ModelType.attach_giphy) {
            configGiphyAction()
        } else {
            cl_action.visibility = View.GONE
        }
    }

    private fun configGiphyAction() {
        cl_action.visibility = View.VISIBLE
        tv_action_send.background = DrawableBuilder()
            .rectangle()
            .rounded()
            .strokeColor(Color.WHITE)
            .strokeWidth(Utils.dpToPx(2))
            .solidColor(ContextCompat.getColor(context, R.color.stream_input_message_send_button))
            .solidColorPressed(Color.LTGRAY)
            .build()
        tv_action_shuffle.background = DrawableBuilder()
            .rectangle()
            .rounded()
            .strokeColor(ContextCompat.getColor(context, R.color.stream_message_stroke))
            .strokeWidth(Utils.dpToPx(2))
            .solidColor(Color.WHITE)
            .solidColorPressed(Color.LTGRAY)
            .build()
        tv_action_cancel.background = DrawableBuilder()
            .rectangle()
            .rounded()
            .strokeColor(ContextCompat.getColor(context, R.color.stream_message_stroke))
            .strokeWidth(Utils.dpToPx(2))
            .solidColor(Color.WHITE)
            .solidColorPressed(Color.LTGRAY)
            .build()
        tv_action_send.setOnClickListener {
            enableSendGiphyButtons(false)
            giphySendListener.onGiphySend(message, GiphyAction.SEND)
        }
        tv_action_shuffle.setOnClickListener {
            enableSendGiphyButtons(false)
            giphySendListener.onGiphySend(message, GiphyAction.SHUFFLE)
        }
        tv_action_cancel.setOnClickListener {
            giphySendListener.onGiphySend(
                message,
                GiphyAction.CANCEL
            )
        }
    }

    private fun enableSendGiphyButtons(isEnable: Boolean) {
        progressBar.isVisible = !isEnable
        tv_action_send.isEnabled = isEnable
        tv_action_shuffle.isEnabled = isEnable
        tv_action_cancel.isEnabled = isEnable
    }

    private fun configClickListeners() {
        iv_media_thumb.setOnClickListener {
            clickListener?.onAttachmentClick(
                message,
                attachment
            )
        }
        iv_media_thumb.setOnLongClickListener {
            longClickListener?.onMessageLongClick(message)
            true
        }
    }
}
