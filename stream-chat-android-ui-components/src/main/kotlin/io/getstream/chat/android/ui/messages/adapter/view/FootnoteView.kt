package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.getstream.sdk.chat.utils.extensions.constrainViewToParentBySide
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageFootnoteBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageThreadsFootnoteBinding

internal class FootnoteView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context,
        attrs,
        defStyleAttr,
        defStyleRes)

    internal val footnote: StreamUiItemMessageFootnoteBinding =
        StreamUiItemMessageFootnoteBinding.inflate(inflater).also { addView(it.root) }
    internal val threadsFootnote: StreamUiMessageThreadsFootnoteBinding =
        StreamUiMessageThreadsFootnoteBinding.inflate(inflater).also { addView(it.root) }

    init {
        ConstraintSet().apply {
            constrainView(footnote.root)
            constrainView(threadsFootnote.root)
            applyTo(this@FootnoteView)
        }
    }

    fun applyGravity(isMine: Boolean) {
        val bias = if (isMine) 1f else 0f
        updateConstraints {
            setHorizontalBias(footnote.root.id, bias)
            setHorizontalBias(threadsFootnote.root.id, bias)
        }
    }

    private fun ConstraintSet.constrainView(view: View) {
        constrainViewToParentBySide(view, ConstraintSet.TOP)
        constrainViewToParentBySide(view, ConstraintSet.LEFT)
        constrainViewToParentBySide(view, ConstraintSet.RIGHT)
        constrainWidth(view.id, ConstraintSet.WRAP_CONTENT)
        constrainHeight(view.id, ConstraintSet.WRAP_CONTENT)
    }
}