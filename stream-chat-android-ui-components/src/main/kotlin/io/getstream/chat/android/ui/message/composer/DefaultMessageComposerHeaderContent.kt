package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultHeaderContentBinding

/**
 * Represents the default header content for the [MessageComposerView].
 */
public class DefaultMessageComposerHeaderContent : FrameLayout, MessageComposerChild {

    /**
     * Handle to layout binding.
     */
    private lateinit var binding: StreamUiMessageComposerDefaultHeaderContentBinding

    /**
     * Handler when the user clicks on the dismiss action button.
     */
    public var dismissActionClickListener: () -> Unit = {}

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    /**
     * Initial UI rendering and setting up callbacks.
     */
    private fun init() {
        binding = StreamUiMessageComposerDefaultHeaderContentBinding.inflate(streamThemeInflater, this)
        binding.dismissButton.setOnClickListener { dismissActionClickListener() }
    }

    /**
     * Re-rendering the UI according to the new state.
     */
    override fun renderState(state: MessageComposerState) {
        val activeAction = state.action

        if (activeAction is Reply) {
            binding.inputModeHeader.isVisible = true
            binding.actionTitle.text = context.getString(R.string.stream_ui_message_input_reply)
        } else if (activeAction is Edit) {
            binding.inputModeHeader.isVisible = true
            binding.actionTitle.text = context.getString(R.string.stream_ui_message_list_edit_message)
        } else {
            binding.inputModeHeader.isVisible = false
        }
    }
}
