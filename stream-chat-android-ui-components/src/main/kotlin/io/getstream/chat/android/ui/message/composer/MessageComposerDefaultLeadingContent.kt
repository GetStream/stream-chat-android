package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultLeadingContentBinding

/**
 * Default leading content of [MessageComposerView].
 */
public class MessageComposerDefaultLeadingContent : FrameLayout, MessageComposerChild {
    /**
     * Handle to layout binding.
     */
    private lateinit var binding: StreamUiMessageComposerDefaultLeadingContentBinding

    /**
     * Callback invoked when attachments button is clicked.
     */
    public var onAttachmentsButtonClicked: () -> Unit = {}

    /**
     * Callback invoked when commands button is clicked.
     */
    public var onCommandsButtonClicked: () -> Unit = {}

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    /**
     * Initial UI rendering and setting up callbacks.
     */
    private fun init() {
        binding = StreamUiMessageComposerDefaultLeadingContentBinding.inflate(streamThemeInflater, this)
        binding.attachmentsButton.setOnClickListener { onAttachmentsButtonClicked() }
        binding.commandsButton.setOnClickListener { onCommandsButtonClicked() }
    }

    /**
     * Re-rendering the UI according to the new state.
     */
    override fun renderState(state: MessageComposerState) {
    }
}
