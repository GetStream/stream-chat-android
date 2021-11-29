package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import com.google.android.material.internal.TextWatcherAdapter
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultCenterContentBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultLeadingContentBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultTrailingContentBinding

@ExperimentalStreamChatApi
public class MessageComposerView : ConstraintLayout {

    private lateinit var binding: StreamUiMessageComposerBinding

    public var onSendMessage: () -> Unit = {}

    public var onInputChanged: (String) -> Unit = {}

    public var onClearInput: () -> Unit = {}

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    private fun init() {
        binding = StreamUiMessageComposerBinding.inflate(streamThemeInflater, this)
        binding.leadingContent.apply {
            val defaultLeadingContent = DefaultLeadingContent(context)
            removeAllViews()
            addView(defaultLeadingContent)
        }
        binding.centerContent.apply {
            val defaultCenterContent = DefaultCenterContent(context).apply {
                onTextChangedListener = { onInputChanged(it) }
                onClearButtonClickListener = { onClearInput() }
            }
            removeAllViews()
            addView(defaultCenterContent)
        }
        binding.trailingContent.apply {
            val defaultTrailingContent = DefaultTrailingContent(context).apply {
                this.onSendButtonClickListener = { onSendMessage() }
            }
            removeAllViews()
            addView(defaultTrailingContent)
        }
    }

    public fun renderState(state: MessageInputState) {
        (binding.trailingContent.children.first() as? DefaultTrailingContent)?.renderState(state)
        (binding.centerContent.children.first() as? DefaultCenterContent)?.renderState(state)
        (binding.leadingContent.children.first() as? DefaultLeadingContent)?.renderState(state)
    }
}

internal class DefaultLeadingContent : FrameLayout {
    private lateinit var binding: StreamUiMessageComposerDefaultLeadingContentBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    private fun init() {
        binding = StreamUiMessageComposerDefaultLeadingContentBinding.inflate(streamThemeInflater, this)
    }

    fun renderState(state: MessageInputState) {
    }
}

internal class DefaultCenterContent : FrameLayout {
    var onTextChangedListener: (String) -> Unit = {}
    var onClearButtonClickListener: () -> Unit = {}

    private lateinit var binding: StreamUiMessageComposerDefaultCenterContentBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

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

    fun renderState(state: MessageInputState) {
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

