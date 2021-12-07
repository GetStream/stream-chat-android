package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.google.android.material.internal.TextWatcherAdapter
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultCenterContentBinding

/**
 * Default center content of [MessageComposerView]
 */
internal class MessageComposerDefaultCenterContent : FrameLayout, MessageComposerChild {
    /**
     * Lambda invoked each time after text was changed
     */
    var onTextChangedListener: (String) -> Unit = {}

    /**
     * Lambda invoked when clear button was clicked
     */
    var onClearButtonClickListener: () -> Unit = {}

    private lateinit var binding: StreamUiMessageComposerDefaultCenterContentBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    /**
     * Initial UI rendering and setting up callbacks
     */
    private fun init() {
        binding = StreamUiMessageComposerDefaultCenterContentBinding.inflate(streamThemeInflater, this)
        binding.messageEditText.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                onTextChangedListener(s.toString())
            }
        })
        binding.clearCommandButton.setOnClickListener {
            onClearButtonClickListener()
        }
    }

    /**
     * Re-rendering the UI according to the new state
     */
    override fun renderState(state: MessageInputState) {
        val isClearInputButtonVisible = state.inputValue.isNotEmpty()
        binding.clearCommandButton.isVisible = isClearInputButtonVisible

        binding.messageEditText.apply {
            val currentValue = text.toString()
            val newValue = state.inputValue
            if (newValue != currentValue) {
                setText(state.inputValue)
            }
        }
    }
}