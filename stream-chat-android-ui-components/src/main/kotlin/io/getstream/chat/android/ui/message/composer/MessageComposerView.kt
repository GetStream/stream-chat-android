package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.common.extensions.internal.getFragmentManager
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerBinding
import io.getstream.chat.android.ui.message.composer.suggestions.command.DefaultMessageComposerCommandSuggestionsContent
import io.getstream.chat.android.ui.message.composer.suggestions.mention.DefaultMessageComposerMentionSuggestionsContent
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
     * Handle to [PopupWindow] which is currently displayed.
     * It is shown above [MessageComposerView] to display hints/suggestions, e.g. suggested mentions, available commands.
     */
    private var suggestionsPopup: PopupWindow? = null

    /**
     * Default implementation of [mentionSuggestionsContent].
     */
    private val defaultMentionSuggestionsView: View by lazy {
        DefaultMessageComposerMentionSuggestionsContent(context).also {
            it.mentionSelectionListener = { mentionSelectionListener(it) }
        }
    }

    /**
     * Handle to a custom mention suggestions view set with [setMentionSuggestionsContent].
     */
    private var mentionSuggestionsContentOverride: View? = null

    /**
     * Mention suggestions list shown in a popup window above the [MessageComposerView].
     */
    private val mentionSuggestionsContent: View = mentionSuggestionsContentOverride ?: defaultMentionSuggestionsView

    /**
     * Default implementation of [commandSuggestionsContent].
     */
    private val defaultCommandSuggestionsView: View by lazy {
        DefaultMessageComposerCommandSuggestionsContent(context).also {
            it.commandSelectionListener = { commandSelectionListener(it) }
        }
    }

    /**
     * Handle to a custom command suggestions view set with [setCommandSuggestionsContent].
     */
    private var commandSuggestionsContentOverride: View? = null

    /**
     * Command suggestions list shown in a popup window above the [MessageComposerView].
     */
    private val commandSuggestionsContent: View = commandSuggestionsContentOverride ?: defaultCommandSuggestionsView

    /**
     * Callback invoked when send button is clicked.
     */
    public var sendMessageClickListener: () -> Unit = {}

    /**
     * Callback invoked when text input is modified.
     */
    public var textInputChangeListener: (String) -> Unit = {}

    /**
     * Callback invoked when clear button is clicked.
     */
    public var clearButtonClickListener: () -> Unit = {}

    /**
     * Callback invoked when attachments are selected.
     */
    public var attachmentSelectionListener: (List<Attachment>) -> Unit = {}

    /**
     * Callback invoked when attachment is removed.
     */
    public var attachmentRemovalListener: (Attachment) -> Unit = {}

    /**
     * Callback invoked when one of mention suggestions is selected.
     */
    public var mentionSelectionListener: (User) -> Unit = {}

    /**
     * Callback invoked when one of command suggestions is selected.
     */
    public var commandSelectionListener: (Command) -> Unit = {}

    /**
     * Callback invoked when "send also to channel" checkbox was clicked.
     */
    public var alsoSendToChannelSelectionListener: (Boolean) -> Unit = {}

    /**
     * Callback invoked when cancel button is clicked.
     */
    public var dismissActionClickListener: () -> Unit = {}

    /**
     * List of all the available commands. This value is used to display command suggestions popup when the "commands" button is clicked.
     */
    public var availableCommands: List<Command> = emptyList()

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
     * Initializing the view with default contents.
     */
    private fun init() {
        binding = StreamUiMessageComposerBinding.inflate(streamThemeInflater, this)
        binding.leadingContent.apply {
            val defaultLeadingContent = DefaultMessageComposerLeadingContent(context).also {
                it.attachmentsButtonClickListener = {
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
                                                title = it.title,
                                            )
                                        }.also {
                                            attachmentSelectionListener(it)
                                        }
                                    }
                                setAttachmentSelectionListener(listener)
                                show(it, AttachmentSelectionDialogFragment.TAG)
                            }
                    }
                }
                it.commandsButtonClickListener = {
                    renderCommandsSuggestions(MessageComposerState(commandSuggestions = availableCommands))
                }
            }
            removeAllViews()
            addView(defaultLeadingContent)
        }
        binding.centerContent.apply {
            val defaultCenterContent = DefaultMessageComposerCenterContent(context).also {
                it.textInputChangeListener = { textInputChangeListener(it) }
                it.clearButtonClickListener = { clearButtonClickListener() }
                it.attachmentRemovalListener = { attachmentRemovalListener(it) }
            }
            removeAllViews()
            addView(defaultCenterContent)
        }
        binding.trailingContent.apply {
            val defaultTrailingContent = DefaultMessageComposerTrailingContent(context).also {
                it.sendButtonClickListener = { sendMessageClickListener() }
            }
            removeAllViews()
            addView(defaultTrailingContent)
        }
        binding.footerContent.apply {
            val defaultFooterContent = DefaultMessageComposerFooterContent(context).also {
                it.alsoSendToChannelSelectionListener = { alsoSendToChannelSelectionListener(it) }
            }
            removeAllViews()
            addView(defaultFooterContent)
        }
        binding.headerContent.apply {
            val defaultHeaderContent = DefaultMessageComposerHeaderContent(context).also {
                it.dismissActionClickListener = { dismissActionClickListener() }
            }
            removeAllViews()
            addView(defaultHeaderContent)
        }
    }

    /**
     * Called by external controller. Responsible for refreshing UI of the default contents [MessageComposerView].
     * For example, when using [MessageComposerView] along with [MessageComposerViewModel] and connecting both with
     * [MessageComposerViewModel.bindView] function the [MessageComposerView.renderState] will be invoked automatically
     * on each change of the [MessageComposerViewModel.messageComposerState].
     *
     * In case you are not using [MessageComposerViewModel.bindView] mechanism, you can call this function on your own
     * to refresh the state of the [MessageComposerView].
     *
     * Note that when you override the default contents using [setLeadingContent], [setCenterContent], or
     * [setTrailingContent] calling this function will make no effect.
     *
     * @param state [MessageComposerState] instance representing current UI state.
     */
    public fun renderState(state: MessageComposerState) {
        (binding.trailingContent.children.first() as? MessageComposerChild)?.renderState(state)
        (binding.centerContent.children.first() as? MessageComposerChild)?.renderState(state)
        (binding.leadingContent.children.first() as? MessageComposerChild)?.renderState(state)
        (binding.footerContent.children.first() as? MessageComposerChild)?.renderState(state)
        (binding.headerContent.children.first() as? MessageComposerChild)?.renderState(state)

        updateSuggestionsPopup(state)
    }

    /**
     * Re-renders suggestions popup window for the given [MessageComposerState] instance.
     *
     * @param state [MessageComposerState] for which the suggestions popup is updated.
     */
    private fun updateSuggestionsPopup(state: MessageComposerState) {
        when {
            state.mentionSuggestions.isNotEmpty() -> renderMentionSuggestions(state)
            state.commandSuggestions.isNotEmpty() -> renderCommandsSuggestions(state)
            else -> suggestionsPopup?.dismiss()
        }
    }

    /**
     * Displays list of command suggestions, or updates it according to the [MessageComposerState.commandSuggestions] list.
     *
     * @param state Current instance of [MessageComposerState].
     */
    private fun renderCommandsSuggestions(state: MessageComposerState) {
        (commandSuggestionsContent as? MessageComposerChild)?.renderState(state)
        val popupWindow = suggestionsPopup ?: createSuggestionPopupWindow(commandSuggestionsContent)
        popupWindow.apply {
            val offset = computeSuggestionsPopupVerticalOffset(commandSuggestionsContent)
            if (isShowing) {
                update(
                    this@MessageComposerView,
                    0,
                    offset,
                    -1,
                    -1,
                )
            } else {
                showAsDropDown(this@MessageComposerView, 0, offset)
            }
        }
    }

    /**
     * Displays list of mention suggestions, or updates it according to the [MessageComposerState.mentionSuggestions] list.
     *
     * @param state Current instance of [MessageComposerState].
     */
    private fun renderMentionSuggestions(state: MessageComposerState) {
        (mentionSuggestionsContent as? MessageComposerChild)?.renderState(state)
        val popupWindow = suggestionsPopup ?: createSuggestionPopupWindow(mentionSuggestionsContent)
        popupWindow.apply {
            val offset = computeSuggestionsPopupVerticalOffset(mentionSuggestionsContent)
            if (isShowing) {
                update(
                    this@MessageComposerView,
                    0,
                    offset,
                    -1,
                    -1,
                )
            } else {
                showAsDropDown(this@MessageComposerView, 0, offset)
            }
        }
    }

    /**
     * Computes vertical offset of suggestions [PopupWindow] for the provided suggestions content [View].
     *
     * @param suggestionsContent Current suggestions content. Either [mentionSuggestionsContent] or [commandSuggestionsContent].
     *
     * @return Px value of the vertical offset of suggestions popup for the given [suggestionsContent] param.
     */
    private fun computeSuggestionsPopupVerticalOffset(suggestionsContent: View): Int {
        suggestionsContent.measure(
            MeasureSpec.makeMeasureSpec(Resources.getSystem().displayMetrics.widthPixels,
                MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        return -(suggestionsContent.measuredHeight + this@MessageComposerView.height)
    }

    /**
     * Creates new [PopupWindow] dedicated to displaying suggestions (e.g. available mention, command suggestions).
     */
    private fun createSuggestionPopupWindow(content: View): PopupWindow {
        val onDismissListener = PopupWindow.OnDismissListener { suggestionsPopup = null }
        return PopupWindow(
            content,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            suggestionsPopup = this
            isOutsideTouchable = true
            setOnDismissListener(onDismissListener)
            inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        }
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

    /**
     * Sets custom footer content view.
     *
     * @param view The [View] which replaces default footer content of [MessageComposerView]. It must implement [MessageComposerChild] interface.
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container.
     */
    public fun <V> setFooterContent(
        view: V,
        layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams,
    ) where V : View, V : MessageComposerChild {
        binding.footerContent.removeAllViews()
        binding.footerContent.addView(view, layoutParams)
    }

    /**
     * Sets custom header content view.
     *
     * @param view The [View] which replaces default header content of [MessageComposerView]. It must implement [MessageComposerChild] interface.
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container.
     */
    public fun <V> setHeaderContent(
        view: V,
        layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams,
    ) where V : View, V : MessageComposerChild {
        binding.headerContent.removeAllViews()
        binding.headerContent.addView(view, layoutParams)
    }

    /**
     * Sets custom mention suggestions content view. It must implement [MessageComposerChild] interface, and should
     * render mention suggestions according to the received state. List of currently available mention suggestions is propagated
     * to the [view] in the [MessageComposerChild.renderState] hook function.
     *
     * @param view The [View] which shows mention suggestions list and allows to choose one of them.
     */
    public fun <V> setMentionSuggestionsContent(view: V) where V : View, V : MessageComposerChild {
        mentionSuggestionsContentOverride = view
    }

    /**
     * Sets custom command suggestions content view. It must implement [MessageComposerChild] interface, and should
     * render command suggestions according to the received state. List of currently available command suggestions is propagated
     * to the [view] in the [MessageComposerChild.renderState] hook function.
     *
     * @param view The [View] which shows command suggestions list and allows to choose one of them.
     */
    public fun <V> setCommandSuggestionsContent(view: V) where V : View, V : MessageComposerChild {
        commandSuggestionsContentOverride = view
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
    public fun renderState(state: MessageComposerState)
}
