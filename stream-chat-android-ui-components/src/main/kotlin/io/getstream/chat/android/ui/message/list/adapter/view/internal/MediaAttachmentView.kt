package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.constrainViewToParentBySide
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.displayMetrics
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiMediaAttachmentViewBinding
import io.getstream.chat.android.ui.message.list.adapter.view.MediaAttachmentViewStyle
import kotlin.math.min

internal class MediaAttachmentView : ConstraintLayout {
    var attachmentClickListener: AttachmentClickListener? = null
    var attachmentLongClickListener: AttachmentLongClickListener? = null
    var giphyBadgeEnabled: Boolean = true

    private val maxMediaAttachmentWidth: Int by lazy(LazyThreadSafetyMode.NONE) {
        (displayMetrics().widthPixels * MAX_WIDTH_PERCENTAGE).toInt()
    }

    internal val binding: StreamUiMediaAttachmentViewBinding =
        StreamUiMediaAttachmentViewBinding.inflate(streamThemeInflater).also {
            it.root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            val padding = 1.dpToPx()
            it.root.setPadding(padding, padding, padding, padding)
            addView(it.root)
            updateConstraints {
                constrainViewToParentBySide(it.root, ConstraintSet.LEFT)
                constrainViewToParentBySide(it.root, ConstraintSet.TOP)
            }
        }
    private lateinit var style: MediaAttachmentViewStyle

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        style = MediaAttachmentViewStyle(context, attrs)
        binding.loadingProgressBar.indeterminateDrawable = style.progressIcon
        binding.moreCountLabel.setTextStyle(style.moreCountTextStyle)
        binding.giphyLabel.setImageDrawable(style.giphyIcon)
    }

    fun showAttachment(attachment: Attachment, andMoreCount: Int = NO_MORE_COUNT, containerView: View? = null) {
        val url = attachment.imagePreviewUrl ?: attachment.titleLink ?: attachment.ogUrl ?: return
        val showMore = {
            if (andMoreCount > NO_MORE_COUNT) {
                showMoreCount(andMoreCount)
            }
        }
        val showGiphyLabel = {
            if (giphyBadgeEnabled && attachment.type == ModelType.attach_giphy) {
                binding.giphyLabel.isVisible = true
            }
        }

        if (attachment.type == ModelType.attach_giphy) {
            attachment.giphyInfo(GiphyInfoType.ORIGINAL)?.let { giphyInfo ->
                containerView?.doOnPreDraw { container ->
                    val replyView = containerView.findViewById<MessageReplyView>(R.id.replyView)

                    container.updateLayoutParams {
                        height = parseHeight(giphyInfo) + parseReplyHeight(replyView)
                        width = parseWidth(giphyInfo)
                    }

                    binding.imageView.updateLayoutParams {
                        height = parseHeight(giphyInfo) - DEFAULT_MARGIN_FOR_CONTAINER_DP.dpToPx()
                        width = parseWidth(giphyInfo) - DEFAULT_MARGIN_FOR_CONTAINER_DP.dpToPx()
                    }

                    showImageByUrl(url) {
                        showMore()
                        showGiphyLabel()
                    }
                }
            } ?: run {
                showImageByUrl(url) {
                    showMore()
                    showGiphyLabel()
                }
            }
        } else {
            showImageByUrl(url) {
                showMore()
                showGiphyLabel()
            }
        }

        if (attachment.type != ModelType.attach_giphy) {
            setOnClickListener { attachmentClickListener?.onAttachmentClick(attachment) }
            setOnLongClickListener {
                attachmentLongClickListener?.onAttachmentLongClick()
                true
            }
        }
    }

    private fun parseReplyHeight(replyView: View) : Int {
        return replyView.height
    }

    private fun parseWidth(giphyInfo: GiphyInfo): Int {
        return min(maxMediaAttachmentWidth, giphyInfo.width)
    }

    private fun parseHeight(giphyInfo: GiphyInfo): Int {
        return if (giphyInfo.width > maxMediaAttachmentWidth) {
            giphyInfo.height * (maxMediaAttachmentWidth / giphyInfo.width)
        } else {
            giphyInfo.height
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadImage.isVisible = isLoading
    }

    private fun showImageByUrl(imageUrl: String, onCompleteCallback: () -> Unit) {
        binding.imageView.load(
            data = imageUrl,
            placeholderDrawable = style.placeholderIcon,
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
            context.getString(R.string.stream_ui_message_list_attachment_more_count, andMoreCount)
    }

    fun setImageShapeByCorners(
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float,
    ) {
        ShapeAppearanceModel.Builder()
            .setTopLeftCornerSize(topLeft)
            .setTopRightCornerSize(topRight)
            .setBottomRightCornerSize(bottomRight)
            .setBottomLeftCornerSize(bottomLeft)
            .build()
            .let(this::setImageShape)
    }

    private fun setImageShape(shapeAppearanceModel: ShapeAppearanceModel) {
        binding.imageView.shapeAppearanceModel = shapeAppearanceModel
        binding.loadImage.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(style.imageBackgroundColor)
        }
        binding.moreCount.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(style.moreCountOverlayColor)
        }
    }

    private fun Attachment.giphyInfo(field: GiphyInfoType): GiphyInfo? {
        val giphyInfoMap = (extraData[ModelType.attach_giphy] as Map<String, Any>?)?.get(field.value) as Map<String, String>?

        return giphyInfoMap?.let { map ->
            GiphyInfo(
                url = map["url"] ?: this.thumbUrl ?: "",
                width = map["width"]?.toInt() ?: GIPHY_INFO_DEFAULT_WIDTH_DP.dpToPx(),
                height = map["height"]?.toInt() ?: GIPHY_INFO_DEFAULT_HEIGHT_DP.dpToPx(),
                size = map["size"]?.toInt() ?: 0,
                frames = map["frames"]?.toInt() ?: 0
            )
        }
    }

    internal enum class GiphyInfoType(val value: String) {
        ORIGINAL("original")
    }

    internal data class GiphyInfo(
        val url: String,
        @Px val width: Int,
        @Px val height: Int,
        val size: Int,
        val frames: Int,
    )

    companion object {
        private const val NO_MORE_COUNT = 0
        private const val DEFAULT_MARGIN_FOR_CONTAINER_DP = 4
        private const val MAX_WIDTH_PERCENTAGE = .75

        private const val GIPHY_INFO_DEFAULT_WIDTH_DP = 200
        private const val GIPHY_INFO_DEFAULT_HEIGHT_DP = 200
    }
}
