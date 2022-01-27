package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultTrailingContentBinding

/**
 * Represents the default trailing content for the [MessageComposerView].
 */
internal class MessageComposerDefaultTrailingContent : FrameLayout, MessageComposerChild {

    /**
     * Handle to layout binding.
     */
    private val binding: StreamUiMessageComposerDefaultTrailingContentBinding =
        StreamUiMessageComposerDefaultTrailingContentBinding.inflate(streamThemeInflater, this)

    /**
     * Handler when the user clicks on the send message button.
     */
    var onSendButtonClick: () -> Unit = {}

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
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
        binding.sendMessageButton.setOnClickListener { onSendButtonClick() }
    }

    /**
     * Re-rendering the UI according to the new state.
     */
    override fun renderState(state: MessageComposerState) {
        val hasTextInput = state.inputValue.isNotEmpty()
        val hasAttachments = state.attachments.isNotEmpty()
        val isInputValid = state.validationErrors.isEmpty()
        val coolDownTime = state.coolDownTime

        val isSendMessageButtonEnabled = (hasTextInput || hasAttachments) && isInputValid

        binding.apply {
            if (coolDownTime > 0) {
                coolDownBadge.isVisible = true
                coolDownBadge.text = coolDownTime.toString()
                sendMessageButton.isVisible = false
            } else {
                coolDownBadge.isVisible = false
                sendMessageButton.isVisible = true
                sendMessageButton.isEnabled = isSendMessageButtonEnabled
            }
        }
    }
}
