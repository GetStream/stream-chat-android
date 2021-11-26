package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerBinding

@ExperimentalStreamChatApi
public class MessageComposerView : ConstraintLayout {

    private lateinit var binding: StreamUiMessageComposerBinding

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

    /**
     * @property leadingContent A function returning content visible on the left.
     */
    public var leadingContent: MessageComposerView.(ViewGroup) -> View = { emptyView() }
        set(value) {
            field = value
            binding.leadingContent.apply {
                removeAllViews()
                addView(value(this))
            }
        }

    /**
     * @property centerContent A function returning content visible in the middle.
     */
    public var centerContent: MessageComposerView.(ViewGroup) -> View = { emptyView() }
        set(value) {
            field = value
            binding.centerContent.apply {
                removeAllViews()
                addView(value(this))
            }
        }

    /**
     * @property trailingContent A function returning content visible on the right.
     */
    public var trailingContent: MessageComposerView.(ViewGroup) -> View = { emptyView() }
        set(value) {
            field = value
            binding.trailingContent.apply {
                removeAllViews()
                addView(value(this))
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
            addView(leadingContent(this))
        }
        binding.centerContent.apply {
            removeAllViews()
            addView(centerContent(this))
        }
        binding.trailingContent.apply {
            removeAllViews()
            addView(trailingContent(this))
        }
    }

    private fun emptyView() = View(context)
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
