package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultLeadingContentBinding

/**
 * Default leading content of [MessageComposerView]
 */
internal class MessageComposerDefaultLeadingContent : FrameLayout, MessageComposerChild {
    private lateinit var binding: StreamUiMessageComposerDefaultLeadingContentBinding

    /**
     * Lambda invoked whn attachments button is clicked
     */
    public var onAttachmentsButtonClick: () -> Unit = {}

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    /**
     * Initial UI rendering and setting up callbacks
     */
    private fun init() {
        binding = StreamUiMessageComposerDefaultLeadingContentBinding.inflate(streamThemeInflater, this)
        binding.attachmentsButton.setOnClickListener { onAttachmentsButtonClick() }
    }

    /**
     * Re-rendering the UI according to the new state
     */
    override fun renderState(state: MessageInputState) {
    }
}
