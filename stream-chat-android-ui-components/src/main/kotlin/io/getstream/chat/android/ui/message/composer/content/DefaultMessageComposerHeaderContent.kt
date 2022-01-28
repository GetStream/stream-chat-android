package io.getstream.chat.android.ui.message.composer.content

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
import io.getstream.chat.android.ui.message.composer.MessageComposerComponent
import io.getstream.chat.android.ui.message.composer.MessageComposerView

/**
 * Represents the default content shown at the top of [MessageComposerView].
 */
public class DefaultMessageComposerHeaderContent : FrameLayout, MessageComposerComponent {

    /**
     * Generated binding class for the XML layout.
     */
    private lateinit var binding: StreamUiMessageComposerDefaultHeaderContentBinding

    /**
     * Click listener for the dismiss action button.
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
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    override fun renderState(state: MessageComposerState) {
        when (state.action) {
            is Reply -> {
                binding.inputModeHeader.isVisible = true
                binding.actionTitle.text = context.getString(R.string.stream_ui_message_input_reply)
            }
            is Edit -> {
                binding.inputModeHeader.isVisible = true
                binding.actionTitle.text = context.getString(R.string.stream_ui_message_list_edit_message)
            }
            else -> {
                binding.inputModeHeader.isVisible = false
            }
        }
    }
}
