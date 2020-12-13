package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.getstream.sdk.chat.adapter.constrainViewToParentBySide
import com.getstream.sdk.chat.adapter.horizontalChainInParent
import com.getstream.sdk.chat.adapter.verticalChainInParent
import com.google.android.material.shape.AbsoluteCornerSize
import com.google.android.material.shape.CornerSize
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.utils.extensions.dpToPx
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
            4 -> showFour(attachments.first(), attachments[1], attachments[2], attachments[3])
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
            setupMinHeight(mediaAttachmentView)
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
            constrainHeight(viewOne.id, LayoutParams.MATCH_PARENT)
            constrainHeight(viewTwo.id, LayoutParams.MATCH_PARENT)
            setupMinHeight(viewOne)
            setupMinHeight(viewTwo)
            constrainViewToParentBySide(viewOne, ConstraintSet.TOP)
            constrainViewToParentBySide(viewTwo, ConstraintSet.TOP)
            constrainViewToParentBySide(viewOne, ConstraintSet.BOTTOM)
            constrainViewToParentBySide(viewTwo, ConstraintSet.BOTTOM)
            horizontalChainInParent(viewOne, viewTwo)
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
            constrainHeight(viewOne.id, LayoutParams.MATCH_PARENT)
            setupMinHeight(viewOne)
            setupMinHeight(viewTwo)
            setupMinHeight(viewThree)
            horizontalChainInParent(viewOne, viewTwo)
            horizontalChainInParent(viewOne, viewThree)
            verticalChainInParent(viewTwo, viewThree)
            applyTo(this@MediaAttachmentsGroupView)
        }
        first.imageUrl?.let(viewOne::showImageByUrl)
        second.imageUrl?.let(viewTwo::showImageByUrl)
        third.imageUrl?.let(viewThree::showImageByUrl)
    }

    private fun showFour(first: Attachment, second: Attachment, third: Attachment, fourth: Attachment) {
        removeAllViews()
        val viewOne = createMediaAttachmentView(context).also { addView(it) }
        val viewTwo = createMediaAttachmentView(context).also { addView(it) }
        val viewThree = createMediaAttachmentView(context).also { addView(it) }
        val viewFour = createMediaAttachmentView(context).also { addView(it) }
        state = State.FourViews(viewOne, viewTwo, viewThree, viewFour)
        ConstraintSet().apply {
            constrainHeight(viewOne.id, LayoutParams.WRAP_CONTENT)
            constrainHeight(viewThree.id, LayoutParams.WRAP_CONTENT)
            setupMinHeight(viewOne)
            setupMinHeight(viewTwo)
            setupMinHeight(viewThree)
            setupMinHeight(viewFour)
            horizontalChainInParent(viewOne, viewTwo)
            horizontalChainInParent(viewThree, viewFour)
            verticalChainInParent(viewOne, viewThree)
            verticalChainInParent(viewTwo, viewFour)
            applyTo(this@MediaAttachmentsGroupView)
        }
        first.imageUrl?.let(viewOne::showImageByUrl)
        second.imageUrl?.let(viewTwo::showImageByUrl)
        third.imageUrl?.let(viewThree::showImageByUrl)
        fourth.imageUrl?.let(viewFour::showImageByUrl)
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
            is State.FourViews -> {
                stateCopy.viewOne.setImageShapeByCorners(topLeftCorner, 0f, 0f, 0f)
                stateCopy.viewTwo.setImageShapeByCorners(0f, topRightCorner, 0f, 0f)
                stateCopy.viewThree.setImageShapeByCorners(0f, 0f, 0f, bottomLeftCorner)
                stateCopy.viewFour.setImageShapeByCorners(0f, 0f, bottomRightCorner, 0f)
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
        data class FourViews(
            val viewOne: MediaAttachmentView,
            val viewTwo: MediaAttachmentView,
            val viewThree: MediaAttachmentView,
            val viewFour: MediaAttachmentView
        ) : State()
    }

    companion object {
        private val MIN_HEIGHT_PX = 95.dpToPx()
        private val DEFAULT_SCALE_TYPE = ImageView.ScaleType.CENTER_CROP
        private val STROKE_WIDTH = 2.dpToPxPrecise()

        private fun createMediaAttachmentView(context: Context): MediaAttachmentView =
            MediaAttachmentView(context).apply {
                id = generateViewId()
            }

        private fun ConstraintSet.setupMinHeight(view: View) {
            this.constrainMinHeight(view.id, MIN_HEIGHT_PX)
        }
    }
}