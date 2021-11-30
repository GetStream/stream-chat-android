package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultTrailingContentBinding

internal class DefaultTrailingContent : FrameLayout {
    var onSendButtonClickListener: () -> Unit = {}

    private lateinit var binding: StreamUiMessageComposerDefaultTrailingContentBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    private fun init() {
        binding = StreamUiMessageComposerDefaultTrailingContentBinding.inflate(streamThemeInflater, this)
        binding.sendMessageButtonEnabled.setOnClickListener {
            onSendButtonClickListener()
        }
    }

    fun renderState(state: MessageInputState) {
        val sendButtonEnabled = state.inputValue.isNotEmpty()
        binding.apply {
            sendMessageButtonDisabled.isVisible = !sendButtonEnabled
            sendMessageButtonEnabled.isVisible = sendButtonEnabled
        }
    }
}