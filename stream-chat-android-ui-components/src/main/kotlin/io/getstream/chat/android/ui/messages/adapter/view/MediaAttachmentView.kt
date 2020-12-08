package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.getstream.sdk.chat.ImageLoader.load
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMediaAttachmentViewBinding

internal class MediaAttachmentView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    private val binding: StreamUiMediaAttachmentViewBinding = StreamUiMediaAttachmentViewBinding.inflate(LayoutInflater.from(context)).also {
        addView(it.root)
    }

    fun showLoading(isLoading: Boolean) {
        binding.loadImage.isVisible = isLoading
    }

    fun showImageByUrl(imageUrl: String) {
        binding.imageView.load(
            uri = imageUrl,
            placeholderResId = R.drawable.stream_ui_picture_placeholder,
            onStart = { showLoading(true) },
            onComplete = { showLoading(false) }
        )
    }

    fun setImageShape(shapeAppearanceModel: ShapeAppearanceModel) {
        binding.imageView.shapeAppearanceModel = shapeAppearanceModel
        binding.loadImage.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            alpha = 128
            setTint(ContextCompat.getColor(context, R.color.stream_ui_black))
        }
    }
}