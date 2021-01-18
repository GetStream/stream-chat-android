package com.getstream.sdk.chat.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.TextView
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.style.TextStyle
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.utils.roundedImageView.CircularImageView
import com.getstream.sdk.chat.view.messages.AvatarStyle
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.initials

public class ReadStateView : RelativeLayout {
    private var readStateStyle: ReadStateStyle = defaultReadStateStyle
    private var avatarStyle: AvatarStyle = defaultAvatarStyle
    private var isIncoming = false
    private var reads: List<ChannelUserRead> = emptyList()

    public constructor(context: Context?) : super(context) {
        init()
    }

    public constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    public fun setReads(
        reads: List<ChannelUserRead>,
        isIncoming: Boolean,
        readStateStyle: ReadStateStyle = defaultReadStateStyle,
        avatarStyle: AvatarStyle = defaultAvatarStyle,
    ) {
        this.reads = reads
        this.readStateStyle = readStateStyle
        this.avatarStyle = avatarStyle
        this.isIncoming = isIncoming
        init()
    }

    @SuppressLint("ResourceType")
    private fun init() {
        removeAllViews()

        if (!readStateStyle.isReadStateEnabled || reads.isEmpty()) return

        val chatFonts = ChatUI.instance().fonts
        val user = reads[0].user
        val image = user.image

        // Avatar
        val imageView = CircularImageView(context)
        val initials = user.initials
        imageView.setPlaceholder(
            initials,
            avatarStyle.avatarBackgroundColor,
            readStateStyle.readStateText.color
        )
        val typeface = chatFonts.getFont(readStateStyle.readStateText)
        if (typeface != null) {
            imageView.setPlaceholderTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                readStateStyle.readStateText.size,
                typeface
            )
        } else {
            imageView.setPlaceholderTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                readStateStyle.readStateText.size,
                readStateStyle.readStateText.style
            )
        }
        if (!Utils.isSVGImage(image)) {
            imageView.load(data = image)
        }
        val avatarParams = LayoutParams(
            readStateStyle.readStateAvatarWidth,
            readStateStyle.readStateAvatarHeight
        )
        val textParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            readStateStyle.readStateAvatarHeight
        )
        imageView.id = 1
        if (reads.size < 2) {
            imageView.layoutParams = avatarParams
            addView(imageView)
            return
        }

        // Count Text
        val textView = TextView(context)
        textView.text = (reads.size - 1).toString()
        textView.setTextColor(readStateStyle.readStateText.color)
        textView.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            readStateStyle.readStateText.size.toFloat()
        )
        chatFonts.setFont(readStateStyle.readStateText, textView)
        textView.gravity = Gravity.CENTER
        textView.id = 2
        if (isIncoming) {
            textParams.addRule(RIGHT_OF, imageView.id)
            textParams.marginStart =
                context.resources.getDimensionPixelOffset(R.dimen.stream_composer_stroke_width)
        } else {
            avatarParams.addRule(RIGHT_OF, textView.id)
            avatarParams.marginStart =
                context.resources.getDimensionPixelOffset(R.dimen.stream_composer_stroke_width)
        }
        imageView.layoutParams = avatarParams
        textView.layoutParams = textParams
        addView(textView)
        addView(imageView)
    }

    private companion object {
        private val defaultAvatarStyle = AvatarStyle(
            avatarWidth = 0,
            avatarHeight = 0,
            avatarBorderWidth = 0,
            avatarBorderColor = 0,
            avatarBackgroundColor = 0,
            avatarInitialText = TextStyle()
        )
        private val defaultReadStateStyle = ReadStateStyle(
            readStateText = TextStyle(),
            isReadStateEnabled = false,
            isDeliveredIndicatorEnabled = false,
            readStateAvatarWidth = 0,
            readStateAvatarHeight = 0
        )
    }
}
