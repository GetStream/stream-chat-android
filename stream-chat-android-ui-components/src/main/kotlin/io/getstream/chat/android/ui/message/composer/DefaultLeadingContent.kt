package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultLeadingContentBinding

internal class DefaultLeadingContent : FrameLayout {
    private lateinit var binding: StreamUiMessageComposerDefaultLeadingContentBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    private fun init() {
        binding = StreamUiMessageComposerDefaultLeadingContentBinding.inflate(streamThemeInflater, this)
    }

    fun renderState(state: MessageInputState) {
    }
}