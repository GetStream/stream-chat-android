package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.internal.TextWatcherAdapter
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultCenterContentBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultLeadingContentBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultTrailingContentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalStreamChatApi
public class MessageComposerView : ConstraintLayout {

    private lateinit var binding: StreamUiMessageComposerBinding

    /**
     * @property messageInputState The current state of the [MessageComposerView] distributed using [Flow] API.
     *
     * Whenever the value of this property changes updated the [MessageComposerView] re-renders automatically.
     * If you are using custom [View] implementation for [leadingContent], [centerContent], or [trailingContent] you may
     * want to collect [messageInputState] items to update them when the state changes.
     */
    public val messageInputState: MutableStateFlow<MessageInputState> = MutableStateFlow(MessageInputState())

    /**
     * @property onSendMessageAction Handler invoked when the user taps on the send message button.
     */
    public var onSendMessageAction: () -> Unit = {}

    /**
     * @property onCancelAction Handler invoked when the user cancels the active action (in Reply or Edit modes).
     */
    public var onCancelAction: () -> Unit = {}

    /**
     * @property onInputTextChanged Handler invoked when the user enters text in message input field.
     */
    public var onInputTextChanged: (String) -> Unit = {}

    private val defaultLeadingContent: View by lazy {
        val binding = StreamUiMessageComposerDefaultLeadingContentBinding.inflate(
            streamThemeInflater,
            binding.trailingContent,
            false
        )
        binding.root
    }

    /**
     * @property leadingContent A function returning content visible on the left.
     */
    public var leadingContent: MessageComposerView.() -> View = {
        defaultLeadingContent
    }
        set(value) {
            field = value
            binding.leadingContent.apply {
                removeAllViews()
                addView(value())
            }
        }

    private val defaultCenterContent: View by lazy {
        val binding = StreamUiMessageComposerDefaultCenterContentBinding.inflate(
            streamThemeInflater,
            binding.trailingContent,
            false
        )
        binding.messageEditText.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                onInputTextChanged(s.toString())
            }
        })
        CoroutineScope(Dispatchers.Main).launch {
            messageInputState.collect {
                binding.messageEditText.apply {
                    val currentValue = text.toString()
                    val newValue = it.inputValue
                    if (newValue != currentValue) {
                        setText(it.inputValue)
                    }
                }
            }
        }
        binding.root
    }

    /**
     * @property centerContent A function returning content visible in the middle.
     */
    public var centerContent: MessageComposerView.() -> View = {
        defaultCenterContent
    }
        set(value) {
            field = value
            binding.centerContent.apply {
                removeAllViews()
                addView(value())
            }
        }

    private val defaultTrailingContent: View by lazy {
        val binding = StreamUiMessageComposerDefaultTrailingContentBinding.inflate(
            streamThemeInflater,
            binding.trailingContent,
            false
        ).apply {
            CoroutineScope(Dispatchers.Main).launch {
                messageInputState.collect {
                    val sendButtonEnabled = it.inputValue.isNotEmpty()
                    sendMessageButtonDisabled.isVisible = !sendButtonEnabled
                    sendMessageButtonEnabled.isVisible = sendButtonEnabled
                }
            }

            sendMessageButtonEnabled.setOnClickListener {
                onSendMessageAction()
            }
        }
        binding.root
    }

    /**
     * @property trailingContent A function returning content visible on the right.
     */
    public var trailingContent: MessageComposerView.() -> View = { defaultTrailingContent }
        set(value) {
            field = value
            binding.trailingContent.apply {
                removeAllViews()
                addView(value())
            }
        }

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
            removeAllViews()
            addView(leadingContent())
        }
        binding.centerContent.apply {
            removeAllViews()
            addView(centerContent())
        }
        binding.trailingContent.apply {
            removeAllViews()
            addView(trailingContent())
        }
        invalidate()
    }
}

/**
 * Represents the state within the message input.
 *
 * @param inputValue The current text value that's within the input.
 * @param attachments The currently selected attachments.
 * @param action The currently active [MessageAction].
 */
public data class MessageInputState(
    val inputValue: String = "",
    val attachments: List<Attachment> = emptyList(),
    val action: MessageAction? = null,
)
