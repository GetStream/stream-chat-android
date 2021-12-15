package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.common.extensions.internal.getFragmentManager
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerBinding
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogFragment
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionListener
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSource

/**
 * UI component designed for message handling message text input, attachments, actions, and sending the message.
 */
@ExperimentalStreamChatApi
public class MessageComposerView : ConstraintLayout {

    /**
     * Handle to layout binding.
     */
    private lateinit var binding: StreamUiMessageComposerBinding

    /**
     * Callback invoked when send button is clicked.
     */
    public var onSendMessageClicked: () -> Unit = {}

    /**
     * Callback invoked when text input is modified.
     */
    public var onInputChanged: (String) -> Unit = {}

    /**
     * Callback invoked when clear button is clicked.
     */
    public var onMessageDismissed: () -> Unit = {}

    /**
     * Callback invoked when attachments are selected.
     */
    public var onAttachmentSelected: (List<Attachment>) -> Unit = {}

    /**
     * Callback invoked when attachment is removed.
     */
    public var onAttachmentRemovedHandler: (Attachment) -> Unit = {}

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    /**
     * Initializing the view with default contents.
     */
    private fun init() {
        binding = StreamUiMessageComposerBinding.inflate(streamThemeInflater, this)
        binding.leadingContent.apply {
            val defaultLeadingContent = MessageComposerDefaultLeadingContent(context).apply {
                onAttachmentsButtonClick = {
                    context.getFragmentManager()?.let {
                        AttachmentSelectionDialogFragment.newInstance(MessageInputViewStyle.createDefault(context))
                            .apply {
                                val listener =
                                    AttachmentSelectionListener { attachments: Set<AttachmentMetaData>, attachmentSource: AttachmentSource ->
                                        attachments.map { // TODO move to view model
                                            val fileFromUri = StorageHelper().getCachedFileFromUri(requireContext(), it)
                                            Attachment(
                                                upload = fileFromUri,
                                                type = it.type,
                                                name = it.title ?: fileFromUri.name ?: "",
                                                fileSize = it.size.toInt(),
                                                mimeType = it.mimeType,
                                            )
                                        }.also {
                                            onAttachmentSelected(it)
                                        }
                                    }
                                setAttachmentSelectionListener(listener)
                                show(it, AttachmentSelectionDialogFragment.TAG)
                            }
                    }
                }
            }
            removeAllViews()
            addView(defaultLeadingContent)
        }
        binding.centerContent.apply {
            val defaultCenterContent = MessageComposerDefaultCenterContent(context).apply {
                onTextChangedListener = { onInputChanged(it) }
                onClearButtonClickListener = { onMessageDismissed() }
                onAttachmentRemovedListener = { onAttachmentRemovedHandler(it) }
            }
            removeAllViews()
            addView(defaultCenterContent)
        }
        binding.trailingContent.apply {
            val defaultTrailingContent = MessageComposerDefaultTrailingContent(context).apply {
                this.onSendButtonClickListener = { onSendMessageClicked() }
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
     * @param state [MessageInputState] instance representing current UI state.
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
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container.
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
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container.
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
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container.
     */
    public fun <V> setTrailingContent(
        view: V,
        layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams,
    ) where V : View, V : MessageComposerChild {
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

/**
 * Interface implemented by MessageComposerView children set by [MessageComposerView.setLeadingContent],
 * [MessageComposerView.setCenterContent], and [MessageComposerView.setTrailingContent] functions.
 *
 * The [renderState] hook function is invoked when the state changes. You should update the UI of the view inside this function implementation.
 */
public interface MessageComposerChild {
    public fun renderState(state: MessageInputState)
}
