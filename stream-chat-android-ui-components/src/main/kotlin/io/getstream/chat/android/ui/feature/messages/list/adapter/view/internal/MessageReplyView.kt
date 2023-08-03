/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.material.shape.MaterialShapeDrawable
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.client.utils.attachment.isLink
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.utils.DurationFormatter
import io.getstream.chat.android.ui.common.utils.extensions.isMine
import io.getstream.chat.android.ui.databinding.StreamUiMessageReplyViewBinding
import io.getstream.chat.android.ui.feature.messages.list.MessageReplyStyle
import io.getstream.chat.android.ui.feature.messages.list.background.ShapeAppearanceModelFactory
import io.getstream.chat.android.ui.utils.ellipsizeText
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.extensions.updateConstraints
import io.getstream.chat.android.ui.utils.extensions.use

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
        defStyleAttr,
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.MessageReplyView).use {
            ellipsize = it.getBoolean(R.styleable.MessageReplyView_streamUiEllipsize, true)
        }
    }

    /**
     * @param message [Message] that was replied to.
     * @param isMine If the message containing the reply was current users or not.
     * @param style The style to be applied to the view.
     */
    fun setMessage(message: Message, isMine: Boolean, style: MessageReplyStyle?) {
        setUserAvatar(message)
        setAvatarPosition(message.isMine(ChatClient.instance().getCurrentUser()))
        setReplyBackground(message, isMine, style)
        setAttachmentImage(message)
        setAdditionalInfo(message)
        setReplyText(message, isMine, style)
    }

    private fun setUserAvatar(message: Message) {
        binding.replyAvatarView.setUser(message.user)
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

    /**
     * Sets the background for message reply.
     *
     * @param quotedMessage [Message] The message contained in the reply bubble.
     * @param isMine Whether the message containing the reply is from the current user or not.
     * @param style [MessageReplyStyle] contains the styles of the background.
     */
    private fun setReplyBackground(quotedMessage: Message, isMine: Boolean, style: MessageReplyStyle?) {
        val shapeAppearanceModel = ShapeAppearanceModelFactory.create(
            context,
            REPLY_CORNER_RADIUS,
            0f,
            quotedMessage.isMine(ChatClient.instance().getCurrentUser()),
            true,
        )

        binding.replyContainer.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            when {
                isLink(quotedMessage) -> {
                    paintStyle = Paint.Style.FILL
                    val color = if (isMine) {
                        style?.linkBackgroundColorMine ?: context.getColorCompat(R.color.stream_ui_blue_alice)
                    } else {
                        style?.linkBackgroundColorTheirs ?: context.getColorCompat(R.color.stream_ui_blue_alice)
                    }
                    setTint(color)
                }
                quotedMessage.isMine(ChatClient.instance().getCurrentUser()) -> {
                    paintStyle = Paint.Style.FILL_AND_STROKE
                    val color = if (isMine) {
                        style?.messageBackgroundColorTheirs ?: context.getColorCompat(R.color.stream_ui_white)
                    } else {
                        style?.messageBackgroundColorMine ?: context.getColorCompat(R.color.stream_ui_grey_whisper)
                    }
                    setTint(color)
                    style?.messageStrokeColorMine?.let(::setStrokeTint)
                    strokeWidth = style?.messageStrokeWidthMine ?: DEFAULT_STROKE_WIDTH
                }
                else -> {
                    paintStyle = Paint.Style.FILL_AND_STROKE
                    setStrokeTint(
                        style?.messageStrokeColorTheirs
                            ?: context.getColorCompat(R.color.stream_ui_grey_whisper),
                    )
                    strokeWidth = style?.messageStrokeWidthTheirs ?: DEFAULT_STROKE_WIDTH
                    val tintColor =
                        style?.messageBackgroundColorTheirs ?: context.getColorCompat(R.color.stream_ui_white)
                    setTint(tintColor)
                }
            }
        }
    }

    private fun isLink(message: Message) = message.attachments.run {
        size == 1 && last().isLink()
    }

    private fun setAttachmentImage(message: Message) {
        if (ChatUI.quotedAttachmentFactoryManager.canHandle(message)) {
            binding.attachmentContainer.isVisible = true
            ChatUI.quotedAttachmentFactoryManager.createAndAddQuotedView(message, binding.attachmentContainer)
        } else {
            binding.attachmentContainer.isVisible = false
        }
    }

    private fun setAdditionalInfo(message: Message) {
        if (message.attachments.any { it.isAudioRecording() }) {
            binding.additionalInfo.isVisible = true
            binding.additionalInfo.text =
                DurationFormatter.formatDurationInMillis(
                    (
                        message.attachments
                            .first { it.isAudioRecording() }
                            .extraData["duration"] as? Double
                        )
                        ?.toInt() ?: 0,
                )
        } else {
            binding.additionalInfo.isVisible = false
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
            if (attachment.isLink()) {
                attachment.titleLink ?: attachment.ogUrl
            } else if (attachment.isAudioRecording()) {
                context.getString(R.string.stream_ui_message_audio_reply_info)
            } else {
                attachment.title ?: attachment.name
            }
        }

        when {
            isLink(message) -> {
                configureLinkTextStyle(isMine, style)
            }
            isMine -> {
                style?.textStyleMine?.apply(binding.replyText)
            }
            else -> {
                style?.textStyleTheirs?.apply(binding.replyText)
            }
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
        return ellipsizeText(text, MAX_ELLIPSIZE_CHAR_COUNT)
    }

    private companion object {
        private val DEFAULT_STROKE_WIDTH = 1.dpToPxPrecise()
        private val REPLY_CORNER_RADIUS = 12.dpToPxPrecise()
        private val CONTENT_MARGIN = 4.dpToPx()
        private const val MAX_ELLIPSIZE_CHAR_COUNT = 170
    }
}
