package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.common.extensions.internal.context
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerAttachmentContainerBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerBinding

/**
 * UI component designed for message handling message text input, attachments, actions, and sending the message.
 */
@ExperimentalStreamChatApi
public class MessageComposerView : ConstraintLayout {

    private lateinit var binding: StreamUiMessageComposerBinding

    /**
     * Callback invoked when send button is clicked
     */
    public var onSendMessageClickHandler: () -> Unit = {}

    /**
     * Callback invoked when text input is modified
     */
    public var onInputChangedHandler: (String) -> Unit = {}

    /**
     * Callback invoked when clear button is clicked
     */
    public var onDismissMessageHandler: () -> Unit = {}

    private val attachmentsAdapter: MessageComposerAttachmentsAdapter = MessageComposerAttachmentsAdapter()

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
            val defaultLeadingContent = MessageComposerDefaultLeadingContent(context)
            removeAllViews()
            addView(defaultLeadingContent)
        }
        binding.centerContent.apply {
            val defaultCenterContent = MessageComposerDefaultCenterContent(context).apply {
                onTextChangedListener = { onInputChangedHandler(it) }
                onClearButtonClickListener = { onDismissMessageHandler() }
            }
            removeAllViews()
            addView(defaultCenterContent)
        }
        binding.trailingContent.apply {
            val defaultTrailingContent = MessageComposerDefaultTrailingContent(context).apply {
                this.onSendButtonClickListener = { onSendMessageClickHandler() }
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
        (binding.trailingContent.children.first() as? MessageComposerChild)?.renderState(state)
        (binding.centerContent.children.first() as? MessageComposerChild)?.renderState(state)
        (binding.leadingContent.children.first() as? MessageComposerChild)?.renderState(state)
    }

    /**
     * Sets custom leading content view.
     *
     * @param view The [View] which replaces default leading content of [MessageComposerView]. It must implement [MessageComposerChild] interface.
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container
     */
    public fun <V> setLeadingContent(
        view: V,
        layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams,
    ) where V : View, V : MessageComposerChild {
        binding.leadingContent.removeAllViews()
        binding.leadingContent.addView(view, layoutParams)
    }

    /**
     * Sets custom center content view.
     *
     * @param view The [View] which replaces default center content of [MessageComposerView]. It must implement [MessageComposerChild] interface.
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container
     */
    public fun <V> setCenterContent(
        view: V,
        layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams,
    ) where V : View, V : MessageComposerChild {
        binding.centerContent.removeAllViews()
        binding.centerContent.addView(view, layoutParams)
    }

    /**
     * Sets custom trailing content view.
     *
     * @param view The [View] which replaces default trailing content of [MessageComposerView]. It must implement [MessageComposerChild] interface.
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container
     */
    public fun <V> setTrailingContent(
        view: V,
        layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams,
    ) where V : View, V : MessageComposerChild {
        binding.trailingContent.removeAllViews()
        binding.trailingContent.addView(view, layoutParams)
    }

    public fun setAttachmentViewFactory(factory: AttachmentViewFactory) {
        attachmentsAdapter.viewFactory = factory
        attachmentsAdapter.notifyDataSetChanged()
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

/**
 * Interface implemented by MessageComposerView children set by [MessageComposerView.setLeadingContent],
 * [MessageComposerView.setCenterContent], and [MessageComposerView.setTrailingContent] functions.
 *
 * The [renderState] hook function is invoked when the state changes. You should update the UI of the view inside this function implementation.
 */
public interface MessageComposerChild {
    public fun renderState(state: MessageInputState)
}

internal class MessageComposerAttachmentsAdapter(
    private val attachmentClearedListener: (Attachment) -> Unit = {},
) : RecyclerView.Adapter<MessageComposerViewHolder>() {
    private val attachments: MutableList<Attachment> = mutableListOf()

    public var viewFactory: AttachmentViewFactory = AttachmentViewFactory()

    internal fun setAttachments(attachments: List<Attachment>) {
        this.attachments.apply {
            clear()
            addAll(attachments)
            notifyDataSetChanged()
        }
    }

    fun removeItem(attachment: Attachment) {
        val position = attachments.indexOf(attachment)
        attachments -= attachment
        notifyItemRemoved(position)
    }

    fun clear() {
        attachments.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageComposerViewHolder {
        return MessageComposerViewHolder(parent, viewFactory, attachmentClearedListener)
    }

    override fun onBindViewHolder(holder: MessageComposerViewHolder, position: Int) {
        holder.bindData(attachments[position])
    }

    override fun getItemCount(): Int = attachments.size
}

internal class MessageComposerViewHolder(
    private val parent: ViewGroup,
    private val attachmentViewFactory: AttachmentViewFactory,
    private val onAttachmentClearedListener: (Attachment) -> Unit,
) : RecyclerView.ViewHolder(parent) {

    internal val binding: StreamUiMessageComposerAttachmentContainerBinding =
        StreamUiMessageComposerAttachmentContainerBinding.inflate(context.streamThemeInflater, parent, false)

    fun bindData(attachment: Attachment) {
        val attachmentContainer = binding.root
        val view = attachmentViewFactory.createViewForAttachment(parent, attachment, onAttachmentClearedListener)
        attachmentContainer.removeAllViews()
        attachmentContainer.addView(view)
    }
}

public open class AttachmentViewFactory {
    public open fun createViewForAttachment(
        parent: ViewGroup,
        attachment: Attachment,
        attachmentRemoveListener: (Attachment) -> Unit,
    ): View {
        return TextView(parent.context).apply { text = "attachment" }
    }
}
