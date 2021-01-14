package io.getstream.chat.android.ui.options

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiAttachmentOptionsViewBinding

internal class AttachmentOptionsView: FrameLayout {

    init {
        StreamUiAttachmentOptionsViewBinding.inflate(context.inflater, this, true)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context,
        attrs,
        defStyleAttr,
        defStyleRes)
}