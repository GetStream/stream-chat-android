package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultTrailingContentBinding

/**
 * Default trailing content of [MessageComposerView].
 */
internal class MessageComposerDefaultTrailingContent : FrameLayout, MessageComposerChild {
    /**
     * Callback invoked when send button is clicked.
     */
    var onSendButtonClicked: () -> Unit = {}

    /**
     * Handle to layout binding.
     */
    private lateinit var binding: StreamUiMessageComposerDefaultTrailingContentBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    /**
     * Initial UI rendering and setting up callbacks.
     */
    private fun init() {
        binding = StreamUiMessageComposerDefaultTrailingContentBinding.inflate(streamThemeInflater, this)
        binding.sendMessageButtonEnabled.setOnClickListener {
            onSendButtonClicked()
        }
    }

    /**
     * Re-rendering the UI according to the new state.
     */
    override fun renderState(state: MessageInputState) {
        val sendButtonEnabled = (state.inputValue.isNotEmpty() || state.attachments.isNotEmpty()) && state.validationErrors.isEmpty()
        binding.apply {
            sendMessageButtonDisabled.isVisible = !sendButtonEnabled
            sendMessageButtonEnabled.isVisible = sendButtonEnabled
        }
    }
}