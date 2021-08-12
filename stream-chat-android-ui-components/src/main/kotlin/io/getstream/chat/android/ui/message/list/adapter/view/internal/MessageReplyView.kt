package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.RoundedCorners
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.databinding.StreamUiMessageReplyViewBinding
import io.getstream.chat.android.ui.message.list.MessageReplyStyle

internal class MessageReplyView : FrameLayout {
    private val binding: StreamUiMessageReplyViewBinding =
        StreamUiMessageReplyViewBinding.inflate(streamThemeInflater, this, true)
    private var ellipsize = false

    constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.MessageReplyView).use {
            ellipsize = it.getBoolean(R.styleable.MessageReplyView_streamUiEllipsize, false)
        }
    }

    fun setMessage(message: Message, isMine: Boolean, style: MessageReplyStyle?) {
        setUserAvatar(message)
        setAvatarPosition(isMine)
        setReplyBackground(message, isMine, style)
        setAttachmentImage(message)
        setReplyText(message, isMine, style)
    }

    private fun setUserAvatar(message: Message) {
        binding.replyAvatarView.setUserData(message.user)
        binding.replyAvatarView.isVisible = true
    }

    private fun setAvatarPosition(isMine: Boolean) {
        with(binding) {
            root.updateConstraints {
                clear(replyAvatarView.id, ConstraintSet.START)
                clear(replyAvatarView.id, ConstraintSet.END)
                clear(replyContainer.id, ConstraintSet.START)
                clear(replyContainer.id, ConstraintSet.END)
            }
            replyAvatarView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (isMine) {
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    startToEnd = replyContainer.id
                } else {
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToStart = replyContainer.id
                }
                marginStart = CONTENT_MARGIN
                marginEnd = CONTENT_MARGIN
            }
            replyContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (isMine) {
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToStart = replyAvatarView.id
                } else {
                    startToEnd = replyAvatarView.id
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
                marginStart = CONTENT_MARGIN
                marginEnd = CONTENT_MARGIN
            }
        }
    }

    private fun setReplyBackground(message: Message, isMine: Boolean, style: MessageReplyStyle?) {
        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(REPLY_CORNER_RADIUS)
            .setBottomLeftCornerSize(if (isMine.not()) 0f else REPLY_CORNER_RADIUS)
            .setBottomRightCornerSize(if (isMine) 0f else REPLY_CORNER_RADIUS)
            .build()
        binding.replyContainer.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            when {
                isLink(message) -> {
                    paintStyle = Paint.Style.FILL
                    val color = if (isMine) {
                        style?.linkBackgroundColorMine ?: context.getColorCompat(R.color.stream_ui_blue_alice)
                    } else {
                        style?.linkBackgroundColorTheirs ?: context.getColorCompat(R.color.stream_ui_blue_alice)
                    }
                    setTint(color)
                }
                isMine -> {
                    paintStyle = Paint.Style.FILL_AND_STROKE
                    val color =
                        style?.messageBackgroundColorMine ?: context.getColorCompat(R.color.stream_ui_grey_whisper)
                    setTint(color)
                    style?.messageStrokeColorMine?.let(::setStrokeTint)
                    strokeWidth = style?.messageStrokeWidthMine ?: DEFAULT_STROKE_WIDTH
                }
                else -> {
                    paintStyle = Paint.Style.FILL_AND_STROKE
                    setStrokeTint(style?.messageStrokeColorTheirs ?: context.getColorCompat(R.color.stream_ui_grey_whisper))
                    strokeWidth = style?.messageStrokeWidthTheirs ?: DEFAULT_STROKE_WIDTH
                    val tintColor = style?.messageBackgroundColorTheirs ?: context.getColorCompat(R.color.stream_ui_white)
                    setTint(tintColor)
                }
            }
        }
    }

    private fun isLink(message: Message) = message.attachments.run {
        size == 1 && last().type == ModelType.attach_link
    }

    private fun setAttachmentImage(message: Message) {
        val attachment = message.attachments.lastOrNull()
        if (attachment == null) {
            binding.logoContainer.isVisible = false
        } else {
            when (attachment.type) {
                ModelType.attach_file -> showFileTypeLogo(attachment.mimeType)
                ModelType.attach_image -> showAttachmentThumb(attachment.imagePreviewUrl)
                ModelType.attach_giphy,
                ModelType.attach_video,
                -> showAttachmentThumb(attachment.thumbUrl)
                else -> showAttachmentThumb(attachment.image)
            }
        }
    }

    private fun setReplyText(message: Message, isMine: Boolean, style: MessageReplyStyle?) {
        val attachment = message.attachments.lastOrNull()
        binding.replyText.text = if (attachment == null || message.text.isNotBlank()) {
            if (ellipsize) {
                ellipsize(message.text)
            } else {
                message.text
            }
        } else {
            val type = attachment.type
            if (type == ModelType.attach_link) {
                attachment.ogUrl
            } else {
                attachment.title ?: attachment.name
            }
        }
        if (isLink(message)) {
            configureLinkTextStyle(isMine, style)
        } else if (isMine) {
            style?.textStyleMine?.apply(binding.replyText)
        } else {
            style?.textStyleTheirs?.apply(binding.replyText)
        }
    }

    private fun configureLinkTextStyle(
        isMine: Boolean,
        style: MessageReplyStyle?,
    ) {
        if (isMine) {
            style?.linkStyleMine?.apply(binding.replyText)
        } else {
            style?.linkStyleTheirs?.apply(binding.replyText)
        }
    }

    private fun ellipsize(text: String): String {
        if (text.length <= MAX_ELLIPSIZE_CHAR_COUNT) return text

        return text.substring(0, MAX_ELLIPSIZE_CHAR_COUNT) + "..."
    }

    private fun showAttachmentThumb(url: String?) {
        with(binding) {
            if (url != null) {
                logoContainer.isVisible = true
                thumbImageView.isVisible = true
                fileTypeImageView.isVisible = false
                thumbImageView.load(
                    data = url,
                    transformation = RoundedCorners(REPLY_IMAGE_CORNER_RADIUS),
                )
            } else {
                logoContainer.isVisible = false
            }
        }
    }

    private fun showFileTypeLogo(mimeType: String?) {
        with(binding) {
            logoContainer.isVisible = true
            fileTypeImageView.isVisible = true
            thumbImageView.isVisible = false
            fileTypeImageView.setImageResource(ChatUI.mimeTypeIconProvider.getIconRes(mimeType))
        }
    }

    private companion object {
        private val DEFAULT_STROKE_WIDTH = 1.dpToPxPrecise()
        private val REPLY_CORNER_RADIUS = 12.dpToPxPrecise()
        private val REPLY_IMAGE_CORNER_RADIUS = 7.dpToPxPrecise()
        private val CONTENT_MARGIN = 4.dpToPx()
        private const val MAX_ELLIPSIZE_CHAR_COUNT = 170
    }
}
