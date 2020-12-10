package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.getstream.sdk.chat.adapter.constraintViewToParentBySide
import io.getstream.chat.android.client.models.Attachment

internal class MediaAttachmentsGroupView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    private var state: State = State.Empty

    fun showAttachments(vararg attachments: Attachment) {
        when (attachments.size) {
            1 -> showOne(attachments.first())
            2 -> showTwo(attachments.first(), attachments[1])
            else -> Unit
        }
    }

    private fun showOne(first: Attachment) {
        removeAllViews()
        val mediaAttachmentView = MediaAttachmentView(context).apply {
            id = generateViewId()
            layoutParams = DEFAULT_LAYOUT_PARAMS
        }
        addView(mediaAttachmentView)
        state = State.OneView(mediaAttachmentView)
        constraintViewToParentBySide(mediaAttachmentView, ConstraintSet.LEFT)
        constraintViewToParentBySide(mediaAttachmentView, ConstraintSet.RIGHT)
        constraintViewToParentBySide(mediaAttachmentView, ConstraintSet.TOP)
        first.imageUrl?.let(mediaAttachmentView::showImageByUrl)
    }

    /*fun setShapeModel(shapeAppearanceModel: ShapeAppearanceModel) {
        this
    }*/

    private fun showTwo(first: Attachment, second: Attachment) {

    }

    private sealed class State {
        object Empty : State()
        data class OneView(val mediaAttachmentView: MediaAttachmentView) : State()
        data class TwoViews(val firstView: MediaAttachmentView, val secondView: MediaAttachmentView) : State()
    }

    companion object {
        private val DEFAULT_LAYOUT_PARAMS: LayoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT)
    }
}