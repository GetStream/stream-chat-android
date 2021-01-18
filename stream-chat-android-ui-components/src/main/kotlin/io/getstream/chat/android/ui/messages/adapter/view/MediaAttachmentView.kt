package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.utils.extensions.constrainViewToParentBySide
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMediaAttachmentViewBinding
import io.getstream.chat.android.ui.utils.ModelType
import io.getstream.chat.android.ui.utils.extensions.dpToPx

internal class MediaAttachmentView : ConstraintLayout {
    var attachmentClickListener: AttachmentClickListener? = null
    var attachmentLongClickListener: AttachmentLongClickListener? = null

    internal val binding: StreamUiMediaAttachmentViewBinding =
        StreamUiMediaAttachmentViewBinding.inflate(LayoutInflater.from(context)).also {
            it.root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            val padding = 1.dpToPx()
            it.root.setPadding(padding, padding, padding, padding)
            addView(it.root)
            updateConstraints {
                constrainViewToParentBySide(it.root, ConstraintSet.LEFT)
                constrainViewToParentBySide(it.root, ConstraintSet.TOP)
            }
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    fun showAttachment(attachment: Attachment, andMoreCount: Int = NO_MORE_COUNT) {
        val url = attachment.thumbUrl ?: attachment.imageUrl ?: attachment.ogUrl ?: return
        val showMore = {
            if (andMoreCount > NO_MORE_COUNT) {
                showMoreCount(andMoreCount)
            }
        }
        val showGiphyLabel = {
            if (attachment.type == ModelType.attach_giphy) {
                binding.giphyLabel.isVisible = true
            }
        }

        showImageByUrl(url) {
            showMore()
            showGiphyLabel()
        }

        if (attachment.type != ModelType.attach_giphy) {
            setOnClickListener { attachmentClickListener?.onAttachmentClick(attachment) }
            setOnLongClickListener {
                attachmentLongClickListener?.onAttachmentLongClick()
                true
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadImage.isVisible = isLoading
    }

    private fun showImageByUrl(imageUrl: String, onCompleteCallback: () -> Unit) {
        binding.imageView.load(
            data = imageUrl,
            placeholderResId = R.drawable.stream_ui_picture_placeholder,
            onStart = { showLoading(true) },
            onComplete = {
                showLoading(false)
                onCompleteCallback()
            }
        )
    }

    private fun showMoreCount(andMoreCount: Int) {
        binding.moreCount.isVisible = true
        binding.moreCountLabel.text =
            context.resources.getString(R.string.stream_ui_attachments_more_count_prefix, andMoreCount)
    }

    fun setImageShapeByCorners(
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float,
    ) {
        ShapeAppearanceModel.Builder().setTopLeftCornerSize(topLeft).setTopRightCornerSize(topRight)
            .setBottomRightCornerSize(bottomRight).setBottomLeftCornerSize(bottomLeft).build().let(this::setImageShape)
    }

    fun setImageShape(shapeAppearanceModel: ShapeAppearanceModel) {
        binding.imageView.shapeAppearanceModel = shapeAppearanceModel
        binding.loadImage.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(ContextCompat.getColor(context, R.color.stream_ui_grey))
        }
        binding.moreCount.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(ContextCompat.getColor(context, R.color.stream_ui_overlay))
        }
    }

    companion object {
        private const val NO_MORE_COUNT = 0
    }
}
