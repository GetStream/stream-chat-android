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

package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.constrainViewToParentBySide
import com.getstream.sdk.chat.utils.extensions.horizontalChainInParent
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import com.getstream.sdk.chat.utils.extensions.verticalChainInParent
import com.google.android.material.shape.AbsoluteCornerSize
import com.google.android.material.shape.CornerSize
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.displayMetrics
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.common.extensions.internal.getOrDefault
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.background.ShapeAppearanceModelFactory
import io.getstream.chat.android.uiutils.extension.hasLink

internal class ImageAttachmentsGroupView : ConstraintLayout {
    var attachmentClickListener: AttachmentClickListener? = null
    var attachmentLongClickListener: AttachmentLongClickListener? = null
    private val maxImageAttachmentHeight: Int by lazy {
        (displayMetrics().heightPixels * MAX_HEIGHT_PERCENTAGE).toInt()
    }

    private var state: State = State.Empty

    constructor(context: Context) : super(context.createStreamThemeWrapper())
    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
        defStyleRes
    )

    fun showAttachments(attachments: List<Attachment>) {
        val images =
            attachments.filter { attachment -> !attachment.hasLink() && attachment.type == ModelType.attach_image }
        when (images.size) {
            0 -> Unit
            1 -> showOne(images.first())
            2 -> showTwo(images.first(), images[1])
            3 -> showThree(images.first(), images[1], images[2])
            else -> showFour(
                images.first(),
                images[1],
                images[2],
                images[3],
                images.size - MAX_PREVIEW_COUNT
            )
        }
        (background as? MaterialShapeDrawable)?.shapeAppearanceModel?.let(::applyToImages)
    }

    private fun showOne(first: Attachment) {
        removeAllViews()
        val imageAttachmentView = createImageAttachmentView()
        addView(imageAttachmentView)
        state = State.OneView(imageAttachmentView)

        val imageWidth = first.originalWidth?.toFloat()
        val imageHeight = first.originalHeight?.toFloat()
        val imageAspectRatio = (imageWidth ?: 0f) / (imageHeight ?: 0f)

        ConstraintSet().apply {
            constrainMaxHeight(imageAttachmentView.id, maxImageAttachmentHeight)
            constrainWidth(imageAttachmentView.id, ViewGroup.LayoutParams.MATCH_PARENT)
            constrainViewToParentBySide(imageAttachmentView, ConstraintSet.LEFT)
            constrainViewToParentBySide(imageAttachmentView, ConstraintSet.RIGHT)
            constrainViewToParentBySide(imageAttachmentView, ConstraintSet.TOP)
            constrainViewToParentBySide(imageAttachmentView, ConstraintSet.BOTTOM)

            // Used to set a dimension ratio before we load an image
            // so that message positions don't jump after we load it.
            if (imageAspectRatio != 0f) {
                this.setDimensionRatio(imageAttachmentView.id, imageAspectRatio.toString())
            } else {
                constrainHeight(imageAttachmentView.id, LayoutParams.WRAP_CONTENT)
            }

            applyTo(this@ImageAttachmentsGroupView)
        }

        if (imageAspectRatio != 0f) {
            imageAttachmentView.post {
                imageAttachmentView.binding.imageView.scaleType =
                    if (imageAttachmentView.measuredHeight < maxImageAttachmentHeight) {
                        ImageView.ScaleType.FIT_XY
                    } else {
                        val scaleFactor = imageAttachmentView.measuredWidth.toFloat() / imageWidth!!
                        val onScreenImageHeight = imageHeight!! * scaleFactor
                        if (onScreenImageHeight <= maxImageAttachmentHeight) {
                            ImageView.ScaleType.FIT_XY
                        } else {
                            ImageView.ScaleType.CENTER_CROP
                        }
                    }
            }
        } else {
            imageAttachmentView.binding.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        }


        imageAttachmentView.showAttachment(first)
    }

    private fun showTwo(first: Attachment, second: Attachment) {
        removeAllViews()
        val viewOne = createImageAttachmentView().also { addView(it) }
        val viewTwo = createImageAttachmentView().also { addView(it) }
        state = State.TwoViews(viewOne, viewTwo)
        ConstraintSet().apply {
            setupMinHeight(viewOne, false)
            setupMinHeight(viewTwo, false)
            constrainViewToParentBySide(viewOne, ConstraintSet.TOP)
            constrainViewToParentBySide(viewTwo, ConstraintSet.TOP)
            constrainViewToParentBySide(viewOne, ConstraintSet.BOTTOM)
            constrainViewToParentBySide(viewTwo, ConstraintSet.BOTTOM)
            horizontalChainInParent(viewOne, viewTwo)
            applyTo(this@ImageAttachmentsGroupView)
        }
        viewOne.showAttachment(first)
        viewTwo.showAttachment(second)
    }

    private fun showThree(first: Attachment, second: Attachment, third: Attachment) {
        removeAllViews()
        val viewOne = createImageAttachmentView().also { addView(it) }
        val viewTwo = createImageAttachmentView().also { addView(it) }
        val viewThree = createImageAttachmentView().also { addView(it) }
        state = State.ThreeViews(viewOne, viewTwo, viewThree)
        ConstraintSet().apply {
            setupMinHeight(viewTwo, true)
            setupMinHeight(viewThree, true)
            horizontalChainInParent(viewOne, viewTwo)
            horizontalChainInParent(viewOne, viewThree)
            verticalChainInParent(viewTwo, viewThree)
            connect(viewOne.id, ConstraintSet.TOP, viewTwo.id, ConstraintSet.TOP)
            connect(viewOne.id, ConstraintSet.BOTTOM, viewThree.id, ConstraintSet.BOTTOM)
            applyTo(this@ImageAttachmentsGroupView)
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
        val viewOne = createImageAttachmentView().also { addView(it) }
        val viewTwo = createImageAttachmentView().also { addView(it) }
        val viewThree = createImageAttachmentView().also { addView(it) }
        val viewFour = createImageAttachmentView().also { addView(it) }
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
            applyTo(this@ImageAttachmentsGroupView)
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
            else -> Unit
        }
    }

    private fun ShapeAppearanceModel.getCornerSize(selector: (ShapeAppearanceModel) -> CornerSize): Float {
        return (((selector(this) as? AbsoluteCornerSize)?.cornerSize ?: 0f) - STROKE_WIDTH).takeIf { it >= 0f }
            .getOrDefault(0f)
    }

    private fun createImageAttachmentView(): ImageAttachmentView {
        return ImageAttachmentView(context).also {
            it.id = generateViewId()
            it.attachmentClickListener = attachmentClickListener
            it.attachmentLongClickListener = attachmentLongClickListener
        }
    }

    /**
     * Configured the background of the View.
     *
     * @param data [MessageListItem.MessageItem].
     */
    fun setupBackground(data: MessageListItem.MessageItem) {
        background = ShapeAppearanceModelFactory.create(
            context,
            BackgroundDecorator.DEFAULT_CORNER_RADIUS,
            0F,
            data.isMine,
            data.isBottomPosition()
        )
            .let(::MaterialShapeDrawable)
            .apply { setTint(ContextCompat.getColor(context, R.color.stream_ui_literal_transparent)) }
    }

    private sealed class State {
        object Empty : State()
        data class OneView(val view: ImageAttachmentView) : State()
        data class TwoViews(val viewOne: ImageAttachmentView, val viewTwo: ImageAttachmentView) : State()
        data class ThreeViews(
            val viewOne: ImageAttachmentView,
            val viewTwo: ImageAttachmentView,
            val viewThree: ImageAttachmentView,
        ) : State()

        data class FourViews(
            val viewOne: ImageAttachmentView,
            val viewTwo: ImageAttachmentView,
            val viewThree: ImageAttachmentView,
            val viewFour: ImageAttachmentView,
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
