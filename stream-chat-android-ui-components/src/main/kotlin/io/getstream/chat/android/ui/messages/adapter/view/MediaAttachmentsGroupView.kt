package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.getstream.sdk.chat.utils.extensions.constrainViewToParentBySide
import com.getstream.sdk.chat.utils.extensions.horizontalChainInParent
import com.getstream.sdk.chat.utils.extensions.verticalChainInParent
import com.google.android.material.shape.AbsoluteCornerSize
import com.google.android.material.shape.CornerSize
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.getOrDefault

internal class MediaAttachmentsGroupView : ConstraintLayout {
    var attachmentClickListener: AttachmentClickListener? = null
    var attachmentLongClickListener: AttachmentLongClickListener? = null
    val maxMediaAttachmentHeight: Int by lazy {
        (Resources.getSystem().displayMetrics.heightPixels * MAX_HEIGHT_PERCENTAGE).toInt()
    }

    private var state: State = State.Empty

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    fun showAttachments(attachments: List<Attachment>) {
        when (attachments.size) {
            0 -> Unit
            1 -> showOne(attachments.first())
            2 -> showTwo(attachments.first(), attachments[1])
            3 -> showThree(attachments.first(), attachments[1], attachments[2])
            else -> showFour(
                attachments.first(),
                attachments[1],
                attachments[2],
                attachments[3],
                attachments.size - MAX_PREVIEW_COUNT
            )
        }
        (background as? MaterialShapeDrawable)?.shapeAppearanceModel?.let(::applyToImages)
    }

    private fun showOne(first: Attachment) {
        removeAllViews()
        val mediaAttachmentView = createMediaAttachmentView()
        addView(mediaAttachmentView)
        state = State.OneView(mediaAttachmentView)
        ConstraintSet().apply {
            constrainHeight(mediaAttachmentView.id, LayoutParams.WRAP_CONTENT)
            constrainMaxHeight(mediaAttachmentView.id, maxMediaAttachmentHeight)
            constrainViewToParentBySide(mediaAttachmentView, ConstraintSet.LEFT)
            constrainViewToParentBySide(mediaAttachmentView, ConstraintSet.RIGHT)
            constrainViewToParentBySide(mediaAttachmentView, ConstraintSet.TOP)
            applyTo(this@MediaAttachmentsGroupView)
        }
        mediaAttachmentView.showAttachment(first)
    }

    private fun showTwo(first: Attachment, second: Attachment) {
        removeAllViews()
        val viewOne = createMediaAttachmentView().also { addView(it) }
        val viewTwo = createMediaAttachmentView().also { addView(it) }
        state = State.TwoViews(viewOne, viewTwo)
        ConstraintSet().apply {
            setupMinHeight(viewOne, false)
            setupMinHeight(viewTwo, false)
            constrainViewToParentBySide(viewOne, ConstraintSet.TOP)
            constrainViewToParentBySide(viewTwo, ConstraintSet.TOP)
            constrainViewToParentBySide(viewOne, ConstraintSet.BOTTOM)
            constrainViewToParentBySide(viewTwo, ConstraintSet.BOTTOM)
            horizontalChainInParent(viewOne, viewTwo)
            applyTo(this@MediaAttachmentsGroupView)
        }
        viewOne.showAttachment(first)
        viewTwo.showAttachment(second)
    }

    private fun showThree(first: Attachment, second: Attachment, third: Attachment) {
        removeAllViews()
        val viewOne = createMediaAttachmentView().also { addView(it) }
        val viewTwo = createMediaAttachmentView().also { addView(it) }
        val viewThree = createMediaAttachmentView().also { addView(it) }
        state = State.ThreeViews(viewOne, viewTwo, viewThree)
        ConstraintSet().apply {
            setupMinHeight(viewTwo, true)
            setupMinHeight(viewThree, true)
            horizontalChainInParent(viewOne, viewTwo)
            horizontalChainInParent(viewOne, viewThree)
            verticalChainInParent(viewTwo, viewThree)
            connect(viewOne.id, ConstraintSet.TOP, viewTwo.id, ConstraintSet.TOP)
            connect(viewOne.id, ConstraintSet.BOTTOM, viewThree.id, ConstraintSet.BOTTOM)
            applyTo(this@MediaAttachmentsGroupView)
        }
        viewOne.showAttachment(first)
        viewTwo.showAttachment(second)
        viewThree.showAttachment(third)
    }

    private fun showFour(
        first: Attachment,
        second: Attachment,
        third: Attachment,
        fourth: Attachment,
        andMoreCount: Int = 0,
    ) {
        removeAllViews()
        val viewOne = createMediaAttachmentView().also { addView(it) }
        val viewTwo = createMediaAttachmentView().also { addView(it) }
        val viewThree = createMediaAttachmentView().also { addView(it) }
        val viewFour = createMediaAttachmentView().also { addView(it) }
        state = State.FourViews(viewOne, viewTwo, viewThree, viewFour)
        ConstraintSet().apply {
            setupMinHeight(viewOne, true)
            setupMinHeight(viewTwo, true)
            setupMinHeight(viewThree, true)
            setupMinHeight(viewFour, true)
            horizontalChainInParent(viewOne, viewTwo)
            horizontalChainInParent(viewThree, viewFour)
            verticalChainInParent(viewOne, viewThree)
            verticalChainInParent(viewTwo, viewFour)
            applyTo(this@MediaAttachmentsGroupView)
        }
        viewOne.showAttachment(first)
        viewTwo.showAttachment(second)
        viewThree.showAttachment(third)
        viewFour.showAttachment(fourth, andMoreCount)
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
        return (((selector(this) as? AbsoluteCornerSize)?.cornerSize ?: 0f) - STROKE_WIDTH).takeIf { it >= 0f }
            .getOrDefault(0f)
    }

    private fun createMediaAttachmentView(): MediaAttachmentView {
        return MediaAttachmentView(context).also {
            it.id = generateViewId()
            it.attachmentClickListener = attachmentClickListener
            it.attachmentLongClickListener = attachmentLongClickListener
        }
    }

    private sealed class State {
        object Empty : State()
        data class OneView(val view: MediaAttachmentView) : State()
        data class TwoViews(val viewOne: MediaAttachmentView, val viewTwo: MediaAttachmentView) : State()
        data class ThreeViews(
            val viewOne: MediaAttachmentView,
            val viewTwo: MediaAttachmentView,
            val viewThree: MediaAttachmentView,
        ) : State()

        data class FourViews(
            val viewOne: MediaAttachmentView,
            val viewTwo: MediaAttachmentView,
            val viewThree: MediaAttachmentView,
            val viewFour: MediaAttachmentView,
        ) : State()
    }

    companion object {
        private const val MAX_HEIGHT_PERCENTAGE = .75
        private const val MAX_PREVIEW_COUNT = 4
        private val MIN_HEIGHT_PX = 95.dpToPx()
        private val STROKE_WIDTH = 2.dpToPxPrecise()

        private fun ConstraintSet.setupMinHeight(view: View, isQuarter: Boolean) {
            this.constrainMinHeight(view.id, if (isQuarter) MIN_HEIGHT_PX else 2 * MIN_HEIGHT_PX)
        }
    }
}
