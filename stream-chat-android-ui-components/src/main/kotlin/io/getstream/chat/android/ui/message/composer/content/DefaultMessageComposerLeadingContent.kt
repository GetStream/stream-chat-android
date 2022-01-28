package io.getstream.chat.android.ui.message.composer.content

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultLeadingContentBinding
import io.getstream.chat.android.ui.message.composer.MessageComposerComponent
import io.getstream.chat.android.ui.message.composer.MessageComposerView

/**
 * Represents the default content shown at the start of [MessageComposerView].
 */
public class DefaultMessageComposerLeadingContent : FrameLayout, MessageComposerComponent {

    /**
     * Generated binding class for the XML layout.
     */
    private lateinit var binding: StreamUiMessageComposerDefaultLeadingContentBinding

    /**
     * Click listener for the pick attachments button.
     */
    public var attachmentsButtonClickListener: () -> Unit = {}

    /**
     * Click listener for the pick commands button.
     */
    public var commandsButtonClickListener: () -> Unit = {}

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
        binding.attachmentsButton.setOnClickListener { attachmentsButtonClickListener() }
        binding.commandsButton.setOnClickListener { commandsButtonClickListener() }
    }

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    override fun renderState(state: MessageComposerState) {
        val hasTextInput = state.inputValue.isNotEmpty()
        val hasAttachments = state.attachments.isNotEmpty()
        val hasCommandInput = state.inputValue.startsWith("/")
        val hasCommandSuggestions = state.commandSuggestions.isNotEmpty()
        val hasMentionSuggestions = state.mentionSuggestions.isNotEmpty()

        val isAttachmentsButtonEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions
        val isCommandsButtonEnabled = !hasTextInput && !hasAttachments

        binding.attachmentsButton.isEnabled = isAttachmentsButtonEnabled
        binding.commandsButton.isEnabled = isCommandsButtonEnabled
    }
}
