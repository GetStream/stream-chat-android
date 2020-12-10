package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.getstream.sdk.chat.adapter.constraintViewToParentBySide
import com.google.android.material.shape.AbsoluteCornerSize
import com.google.android.material.shape.CornerSize
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.getOrDefault

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
        (background as? MaterialShapeDrawable)?.shapeAppearanceModel?.let(::applyToImages)
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

    override fun setBackground(background: Drawable) {
        super.setBackground(background)
        if (background is MaterialShapeDrawable) {
            applyToImages(background.shapeAppearanceModel)
        }
    }

    private fun applyToImages(shapeAppearanceModel: ShapeAppearanceModel) {
        val topLeftCorner = shapeAppearanceModel.getCornerSize(ShapeAppearanceModel::getTopLeftCornerSize)
        val topRightCorner = shapeAppearanceModel.getCornerSize(ShapeAppearanceModel::getTopRightCornerSize)
        val bottomRightCorner = shapeAppearanceModel.getCornerSize(ShapeAppearanceModel::getBottomRightCornerSize)
        val bottomLeftCorner = shapeAppearanceModel.getCornerSize(ShapeAppearanceModel::getBottomLeftCornerSize)
        (shapeAppearanceModel.topLeftCornerSize as? AbsoluteCornerSize)?.let {
            when (val stateCopy = state) {
                is State.OneView -> stateCopy.mediaAttachmentView.setImageShapeByCorners(
                    topLeftCorner,
                    topRightCorner,
                    bottomRightCorner,
                    bottomLeftCorner
                )
            }
        }
    }

    private fun ShapeAppearanceModel.getCornerSize(selector: (ShapeAppearanceModel) -> CornerSize): Float {
        return ((selector(this) as? AbsoluteCornerSize)?.cornerSize ?: 0f - STROKE_WIDTH).takeIf { it >= 0f }
            .getOrDefault(0f)
    }

    private fun showTwo(first: Attachment, second: Attachment) {
    }

    private sealed class State {
        object Empty : State()
        data class OneView(val mediaAttachmentView: MediaAttachmentView) : State()
        data class TwoViews(val firstView: MediaAttachmentView, val secondView: MediaAttachmentView) : State()
    }

    companion object {
        private val DEFAULT_LAYOUT_PARAMS: LayoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT)
        private val STROKE_WIDTH = 2.dpToPxPrecise()
    }
}