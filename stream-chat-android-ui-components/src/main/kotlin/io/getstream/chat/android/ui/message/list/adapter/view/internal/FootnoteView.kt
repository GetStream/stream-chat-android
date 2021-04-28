package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.extensions.constrainViewToParentBySide
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageFootnoteBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageThreadsFootnoteBinding
import io.getstream.chat.android.ui.message.list.MessageListItemStyle

internal class FootnoteView : ConstraintLayout {

    constructor(context: Context) : super(context.createStreamThemeWrapper())
    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context.createStreamThemeWrapper(), attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
        defStyleRes
    )

    private val footnote = StreamUiItemMessageFootnoteBinding.inflate(streamThemeInflater).also { addView(it.root) }
    private val threadsFootnote = StreamUiMessageThreadsFootnoteBinding.inflate(streamThemeInflater).also { addView(it.root) }
    val footerTextLabel: TextView = footnote.messageFooterLabel

    init {
        ConstraintSet().apply {
            constrainView(footnote.root)
            constrainView(threadsFootnote.root)
            applyTo(this@FootnoteView)
        }
        footnote.root.isVisible = false
        threadsFootnote.root.isVisible = false
    }

    fun applyGravity(isMine: Boolean) {
        val bias = if (isMine) 1f else 0f
        updateConstraints {
            setHorizontalBias(footnote.root.id, bias)
            setHorizontalBias(threadsFootnote.root.id, bias)
        }
    }

    fun showSimpleFootnote() {
        footnote.root.isVisible = true
        threadsFootnote.root.isVisible = false
    }

    fun showThreadRepliesFootnote(isMine: Boolean, replyCount: Int, threadParticipants: List<User>, style: MessageListItemStyle) {
        footnote.root.isVisible = false
        with(threadsFootnote) {
            root.isVisible = true
            threadsOrnamentLeft.isVisible = !isMine
            threadsOrnamentRight.isVisible = isMine

            threadRepliesButton.text =
                resources.getQuantityString(R.plurals.stream_ui_thread_messages_indicator, replyCount, replyCount)
            style.textStyleThreadCounter.apply(threadRepliesButton)
        }
        setupUserAvatars(isMine, threadParticipants)
    }

    private fun setupUserAvatars(isMine: Boolean, threadParticipants: List<User>) {
        fun applyUser(user: User?, avatarView: AvatarView) {
            if (user != null) {
                avatarView.setUserData(user)
            } else {
                avatarView.isVisible = false
            }
        }

        with(threadsFootnote) {
            firstTheirUserImage.isVisible = !isMine
            secondTheirUserImage.isVisible = !isMine
            firstMineUserImage.isVisible = isMine
            secondMineUserImage.isVisible = isMine

            val (first, second) = getTwoLastUsers(threadParticipants)

            applyUser(first, if (isMine) firstMineUserImage else firstTheirUserImage)
            applyUser(second, if (isMine) secondMineUserImage else secondTheirUserImage)
        }
    }

    private fun getTwoLastUsers(threadParticipants: List<User>): Pair<User?, User?> {
        if (threadParticipants.isEmpty()) {
            return null to null
        }
        return threadParticipants.toSet().let { userSet ->
            when {
                userSet.size > 1 -> userSet.first() to userSet.elementAt(1)
                else -> userSet.first() to null
            }
        }
    }

    fun hideStatusIndicator() {
        footnote.deliveryStatusIcon.isVisible = false
    }

    internal fun showStatusIndicator(drawableRes: Drawable) {
        footnote.deliveryStatusIcon.isVisible = true
        footnote.deliveryStatusIcon.setImageDrawable(drawableRes)
    }

    fun showTime(time: String, style: MessageListItemStyle) {
        footnote.timeView.apply {
            isVisible = true
            text = time
            style.textStyleMessageDate.apply(this)
        }
    }

    fun hideTimeLabel() {
        footnote.timeView.isVisible = false
    }

    fun setOnThreadClickListener(onClick: (View) -> Unit) {
        threadsFootnote.root.setOnClickListener(onClick)
    }

    private fun ConstraintSet.constrainView(view: View) {
        constrainViewToParentBySide(view, ConstraintSet.TOP)
        constrainViewToParentBySide(view, ConstraintSet.LEFT)
        constrainViewToParentBySide(view, ConstraintSet.RIGHT)
        constrainWidth(view.id, ConstraintSet.WRAP_CONTENT)
        constrainHeight(view.id, ConstraintSet.WRAP_CONTENT)
    }
}
