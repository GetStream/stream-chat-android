package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.getstream.sdk.chat.adapter.constrainViewToParentBySide
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
            3 -> showThree(attachments.first(), attachments[1], attachments[2])
            else -> Unit
        }
        (background as? MaterialShapeDrawable)?.shapeAppearanceModel?.let(::applyToImages)
    }

    private fun showOne(first: Attachment) {
        removeAllViews()
        val mediaAttachmentView = createMediaAttachmentView(context)
        addView(mediaAttachmentView)
        state = State.OneView(mediaAttachmentView)
        ConstraintSet().apply {
            constrainHeight(mediaAttachmentView.id, LayoutParams.WRAP_CONTENT)
            constrainViewToParentBySide(mediaAttachmentView, ConstraintSet.LEFT)
            constrainViewToParentBySide(mediaAttachmentView, ConstraintSet.RIGHT)
            constrainViewToParentBySide(mediaAttachmentView, ConstraintSet.TOP)
            applyTo(this@MediaAttachmentsGroupView)
        }
        first.imageUrl?.let(mediaAttachmentView::showImageByUrl)
    }

    private fun showTwo(first: Attachment, second: Attachment) {
        removeAllViews()
        val viewOne = createMediaAttachmentView(context).also { addView(it) }
        val viewTwo = createMediaAttachmentView(context).also { addView(it) }
        state = State.TwoViews(viewOne, viewTwo)
        ConstraintSet().apply {
            constrainViewToParentBySide(viewOne, ConstraintSet.TOP)
            constrainViewToParentBySide(viewTwo, ConstraintSet.TOP)
            constrainViewToParentBySide(viewOne, ConstraintSet.BOTTOM)
            constrainViewToParentBySide(viewTwo, ConstraintSet.BOTTOM)
            constrainHeight(viewOne.id, LayoutParams.MATCH_PARENT)
            constrainHeight(viewTwo.id, LayoutParams.MATCH_PARENT)
            createHorizontalChain(
                ConstraintSet.PARENT_ID,
                ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.RIGHT,
                intArrayOf(viewOne.id, viewTwo.id),
                null,
                ConstraintSet.CHAIN_SPREAD
            )
            applyTo(this@MediaAttachmentsGroupView)
        }
        first.imageUrl?.let(viewOne::showImageByUrl)
        second.imageUrl?.let(viewTwo::showImageByUrl)
    }

    private fun showThree(first: Attachment, second: Attachment, third: Attachment) {
        removeAllViews()
        val viewOne = createMediaAttachmentView(context).also { addView(it) }
        val viewTwo = createMediaAttachmentView(context).also { addView(it) }
        val viewThree = createMediaAttachmentView(context).also { addView(it) }
        state = State.ThreeViews(viewOne, viewTwo, viewThree)
        ConstraintSet().apply {
            constrainViewToParentBySide(viewOne, ConstraintSet.TOP)
            constrainViewToParentBySide(viewThree, ConstraintSet.RIGHT)
            constrainHeight(viewOne.id, LayoutParams.MATCH_PARENT)
            createHorizontalChain(
                ConstraintSet.PARENT_ID,
                ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.RIGHT,
                intArrayOf(viewOne.id, viewTwo.id),
                null,
                ConstraintSet.CHAIN_SPREAD
            )
            createHorizontalChain(
                ConstraintSet.PARENT_ID,
                ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.RIGHT,
                intArrayOf(viewOne.id, viewThree.id),
                null,
                ConstraintSet.CHAIN_SPREAD
            )
            createVerticalChain(
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                intArrayOf(viewTwo.id, viewThree.id),
                null,
                ConstraintSet.CHAIN_SPREAD
            )
            applyTo(this@MediaAttachmentsGroupView)
        }
        first.imageUrl?.let(viewOne::showImageByUrl)
        second.imageUrl?.let(viewTwo::showImageByUrl)
        third.imageUrl?.let(viewThree::showImageByUrl)
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
        when (val stateCopy = state) {
            is State.OneView -> stateCopy.view.setImageShapeByCorners(
                topLeftCorner,
                topRightCorner,
                bottomRightCorner,
                bottomLeftCorner
            )
            is State.TwoViews -> {
                stateCopy.viewOne.setImageShapeByCorners(topLeftCorner, 0f, 0f, bottomLeftCorner)
                stateCopy.viewTwo.setImageShapeByCorners(0f, topRightCorner, bottomRightCorner, 0f)
            }
            is State.ThreeViews -> {
                stateCopy.viewOne.setImageShapeByCorners(topLeftCorner, 0f, 0f, bottomLeftCorner)
                stateCopy.viewTwo.setImageShapeByCorners(0f, topRightCorner, 0f, 0f)
                stateCopy.viewThree.setImageShapeByCorners(0f, 0f, bottomRightCorner, 0f)
            }
        }
    }

    private fun ShapeAppearanceModel.getCornerSize(selector: (ShapeAppearanceModel) -> CornerSize): Float {
        return ((selector(this) as? AbsoluteCornerSize)?.cornerSize ?: 0f - STROKE_WIDTH).takeIf { it >= 0f }
            .getOrDefault(0f)
    }

    private sealed class State {
        object Empty : State()
        data class OneView(val view: MediaAttachmentView) : State()
        data class TwoViews(val viewOne: MediaAttachmentView, val viewTwo: MediaAttachmentView) : State()
        data class ThreeViews(
            val viewOne: MediaAttachmentView,
            val viewTwo: MediaAttachmentView,
            val viewThree: MediaAttachmentView
        ) : State()
    }

    companion object {
        private val DEFAULT_SCALE_TYPE = ImageView.ScaleType.CENTER_CROP
        private val STROKE_WIDTH = 2.dpToPxPrecise()

        private fun createMediaAttachmentView(context: Context): MediaAttachmentView =
            MediaAttachmentView(context).apply {
                id = generateViewId()
            }
    }
}