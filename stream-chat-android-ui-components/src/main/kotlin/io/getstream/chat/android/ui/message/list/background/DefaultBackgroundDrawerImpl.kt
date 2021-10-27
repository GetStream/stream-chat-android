package io.getstream.chat.android.ui.message.list.background

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.common.extensions.internal.hasLink
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator

public open class BackgroundDrawerImpl(private val style: MessageListItemStyle): BackgroundDrawer {

    override fun plainTextMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable {
        return defaultBackground(context, data)
    }

    override fun deletedMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable {
        return ShapeAppearanceModel.builder()
            .setAllCornerSizes(BackgroundDecorator.DEFAULT_CORNER_RADIUS)
            .apply {
                when {
                    data.isBottomPosition() && data.isMine -> setBottomRightCornerSize(0f)
                    data.isBottomPosition() && data.isTheirs -> setBottomLeftCornerSize(0f)
                }
            }
            .build()
            .let(::MaterialShapeDrawable)
            .apply {
                setTint(style.messageDeletedBackground)
            }
    }

    override fun textAndAttachmentMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable {
        return defaultBackground(context, data)
    }

    private fun defaultBackground(context: Context, data: MessageListItem.MessageItem): Drawable {
        val radius = BackgroundDecorator.DEFAULT_CORNER_RADIUS
        val bottomRightCorner = if (data.isMine && data.isBottomPosition()) 0f else radius
        val bottomLeftCorner = if (data.isMine.not() && data.isBottomPosition()) 0f else radius

        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(radius)
            .setBottomLeftCornerSize(bottomLeftCorner)
            .setBottomRightCornerSize(bottomRightCorner)
            .build()

        return MaterialShapeDrawable(shapeAppearanceModel).apply {
            val hasLink = data.message.attachments.any(Attachment::hasLink)
            if (data.isMine) {
                paintStyle = Paint.Style.FILL_AND_STROKE
                setStrokeTint(style.messageStrokeColorMine)
                strokeWidth = style.messageStrokeWidthMine
                // for messages with links, we use a different background color than other messages by default.
                // however, if a user has specified a background color attribute, we use it for _all_ message backgrounds.
                val backgroundTintColor = if (hasLink) {
                    style.messageLinkBackgroundColorMine
                } else {
                    style.messageBackgroundColorMine ?: ContextCompat.getColor(
                        context,
                        MESSAGE_CURRENT_USER_BACKGROUND
                    )
                }

                setTint(backgroundTintColor)
            } else {
                paintStyle = Paint.Style.FILL_AND_STROKE
                setStrokeTint(style.messageStrokeColorTheirs)
                strokeWidth = style.messageStrokeWidthTheirs

                val backgroundTintColor = if (hasLink) {
                    style.messageLinkBackgroundColorTheirs
                } else {
                    style.messageBackgroundColorTheirs ?: ContextCompat.getColor(
                        context,
                        MESSAGE_OTHER_USER_BACKGROUND
                    )
                }

                setTint(backgroundTintColor)
            }
        }
    }

    public companion object {
        private val MESSAGE_OTHER_USER_BACKGROUND = R.color.stream_ui_white
        private val MESSAGE_CURRENT_USER_BACKGROUND = R.color.stream_ui_grey_gainsboro
        private val SMALL_CARD_VIEW_CORNER_RADIUS = 2.dpToPxPrecise()
        private val IMAGE_VIEW_CORNER_RADIUS = 8.dpToPxPrecise()

        internal val DEFAULT_CORNER_RADIUS = 16.dpToPxPrecise()
    }
}
