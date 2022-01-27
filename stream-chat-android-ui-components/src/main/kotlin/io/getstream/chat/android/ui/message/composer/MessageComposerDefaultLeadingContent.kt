package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultLeadingContentBinding

/**
 * Represents the default leading content for the [MessageComposerView].
 */
public class MessageComposerDefaultLeadingContent : FrameLayout, MessageComposerChild {

    /**
     * Handle to layout binding.
     */
    private val binding: StreamUiMessageComposerDefaultLeadingContentBinding =
        StreamUiMessageComposerDefaultLeadingContentBinding.inflate(streamThemeInflater, this)

    /**
     * Handler when the user clicks on the attachments button.
     */
    public var onAttachmentsButtonClick: () -> Unit = {}

    /**
     * Handler when the user clicks on the pick commands button.
     */
    public var onCommandsButtonClick: () -> Unit = {}

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
        binding.attachmentsButton.setOnClickListener { onAttachmentsButtonClick() }
        binding.commandsButton.setOnClickListener { onCommandsButtonClick() }
    }

    /**
     * Re-rendering the UI according to the new state.
     */
    override fun renderState(state: MessageComposerState): Unit = Unit
}
