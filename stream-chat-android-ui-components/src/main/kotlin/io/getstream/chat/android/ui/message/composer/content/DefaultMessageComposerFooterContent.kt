package io.getstream.chat.android.ui.message.composer.content

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultFooterContentBinding
import io.getstream.chat.android.ui.message.composer.MessageComposerComponent
import io.getstream.chat.android.ui.message.composer.MessageComposerView

/**
 * Represents the default content shown at the bottom of [MessageComposerView].
 */
public class DefaultMessageComposerFooterContent : FrameLayout, MessageComposerComponent {

    /**
     * Generated binding class for the XML layout.
     */
    private lateinit var binding: StreamUiMessageComposerDefaultFooterContentBinding

    /**
     * Selection listener for the "also send to channel" checkbox.
     */
    public var alsoSendToChannelSelectionListener: (Boolean) -> Unit = {}

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
        binding = StreamUiMessageComposerDefaultFooterContentBinding.inflate(streamThemeInflater, this)
        binding.alsoSendToChannel.setOnCheckedChangeListener { _, _ ->
            alsoSendToChannelSelectionListener(binding.alsoSendToChannel.isChecked)
        }
    }

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    override fun renderState(state: MessageComposerState) {
        val shouldShowCheckbox = state.messageMode is MessageMode.MessageThread
        binding.alsoSendToChannel.isVisible = shouldShowCheckbox
        binding.alsoSendToChannel.isChecked = state.alsoSendToChannel
    }
}
