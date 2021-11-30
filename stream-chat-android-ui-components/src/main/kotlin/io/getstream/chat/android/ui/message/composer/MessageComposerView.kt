package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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

    /**
     * Called by external controller. Responsible for refreshing UI of the default contents [MessageComposerView].
     * For example, when using [MessageComposerView] along with [MessageComposerViewModel] and connecting both with
     * [MessageComposerViewModel.bindView] function the [MessageComposerView.renderState] will be invoked automatically
     * on each change of the [MessageComposerViewModel.messageInputState].
     *
     * In case you are not using [MessageComposerViewModel.bindView] mechanism, you can call this function on your own
     * to refresh the state of the [MessageComposerView].
     *
     * Note that when you override the default contents using [setLeadingContent], [setCenterContent], or
     * [setTrailingContent] calling this function will make no effect.
     *
     * @param state [MessageInputState] instance representing current UI state
     */
    public fun renderState(state: MessageInputState) {
        (binding.trailingContent.children.first() as? DefaultTrailingContent)?.renderState(state)
        (binding.centerContent.children.first() as? DefaultCenterContent)?.renderState(state)
        (binding.leadingContent.children.first() as? DefaultLeadingContent)?.renderState(state)
    }

    /**
     * Sets custom leading content view.
     *
     * @param view The [View] which replaces default leading content of [MessageComposerView]
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container
     */
    public fun setLeadingContent(view: View, layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams) {
        binding.leadingContent.removeAllViews()
        binding.leadingContent.addView(view, layoutParams)
    }

    /**
     * Sets custom center content view.
     *
     * @param view The [View] which replaces default center content of [MessageComposerView]
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container
     */
    public fun setCenterContent(view: View, layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams) {
        binding.centerContent.removeAllViews()
        binding.centerContent.addView(view, layoutParams)
    }

    /**
     * Sets custom trailing content view.
     *
     * @param view The [View] which replaces default trailing content of [MessageComposerView]
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container
     */
    public fun setTrailingContent(view: View, layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams) {
        binding.trailingContent.removeAllViews()
        binding.trailingContent.addView(view, layoutParams)
    }

    private companion object {
        private val defaultChildLayoutParams by lazy {
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        }
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

