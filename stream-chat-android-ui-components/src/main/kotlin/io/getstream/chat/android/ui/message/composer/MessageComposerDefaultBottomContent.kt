package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultBottomContentBinding

/**
 * Default bottom content of [MessageComposerView]. Used in thread mode for showing "send also to channel" checkbox.
 */
public class MessageComposerDefaultBottomContent : FrameLayout, MessageComposerChild {
    /**
     * Handle to layout binding.
     */
    private lateinit var binding: StreamUiMessageComposerDefaultBottomContentBinding

    /**
     * Callback invoked when checkbox is clicked.
     */
    public var onSendAlsoToChannelSelected: (Boolean) -> Unit = {}

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
        binding = StreamUiMessageComposerDefaultBottomContentBinding.inflate(streamThemeInflater, this)
        // binding.root.isVisible = false
        binding.sendAlsoToChannel.setOnCheckedChangeListener { _, _ -> onSendAlsoToChannelSelected(binding.sendAlsoToChannel.isChecked) }
    }

    /**
     * Re-rendering the UI according to the new state.
     */
    override fun renderState(state: MessageInputState) {
        val shouldShowCheckbox = state.messageMode is MessageMode.MessageThread
        binding.sendAlsoToChannel.isVisible = shouldShowCheckbox
        binding.sendAlsoToChannel.isChecked = state.alsoSendToChannel
    }
}
