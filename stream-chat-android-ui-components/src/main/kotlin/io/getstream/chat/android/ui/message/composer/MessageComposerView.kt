package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
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
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerCenterContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerCommandSuggestionsContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerFooterContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerHeaderContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerLeadingContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerMentionSuggestionsContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerTrailingContent
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogFragment
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionListener
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSource

/**
 * UI component designed for message handling message text input, attachments, actions, and sending the message.
 */
@ExperimentalStreamChatApi
public class MessageComposerView : ConstraintLayout, MessageComposerComponent {

    /**
     * Generated binding class for the XML layout.
     */
    private lateinit var binding: StreamUiMessageComposerBinding

    /**
     * Click listener for the send message button.
     */
    public var sendMessageButtonClickListener: () -> Unit = {}

    /**
     * Text change listener invoked each time after text was changed.
     */
    public var textInputChangeListener: (String) -> Unit = {}

    /**
     * Click listener for the clear input button.
     */
    public var clearInputButtonClickListener: () -> Unit = {}

    /**
     * Selection listener invoked when attachments are selected.
     */
    public var attachmentSelectionListener: (List<Attachment>) -> Unit = {}

    /**
     * Click listener for the remove attachment button.
     */
    public var attachmentRemovalListener: (Attachment) -> Unit = {}

    /**
     * Selection listener invoked when a mention suggestion item is selected.
     */
    public var mentionSelectionListener: (User) -> Unit = {}

    /**
     * Selection listener invoked when a command suggestion item is selected.
     */
    public var commandSelectionListener: (Command) -> Unit = {}

    /**
     * Selection listener for the "also send to channel" checkbox.
     */
    public var alsoSendToChannelSelectionListener: (Boolean) -> Unit = {}

    /**
     * Click listener for the dismiss action button.
     */
    public var dismissActionClickListener: () -> Unit = {}

    /**
     * Click listener for the pick commands button.
     */
    public var commandsButtonClickListener: () -> Unit = {}

    /**
     * Click listener invoked when suggestion popup is dismissed,
     */
    public var dismissSuggestionsListener: () -> Unit = {}

    /**
     * Handle to [PopupWindow] which is currently displayed.
     * It is shown above [MessageComposerView] to display hints/suggestions, e.g. suggested mentions, available commands.
     */
    private var suggestionsPopup: MessageComposerSuggestionsPopup? = null

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
                it.commandsButtonClickListener = { commandsButtonClickListener() }
            }
            removeAllViews()
            addView(defaultLeadingContent)
        }
        binding.centerContent.apply {
            val defaultCenterContent = DefaultMessageComposerCenterContent(context).also {
                it.textInputChangeListener = { textInputChangeListener(it) }
                it.clearInputButtonClickListener = { clearInputButtonClickListener() }
                it.attachmentRemovalListener = { attachmentRemovalListener(it) }
            }
            removeAllViews()
            addView(defaultCenterContent)
        }
        binding.trailingContent.apply {
            val defaultTrailingContent = DefaultMessageComposerTrailingContent(context).also {
                it.sendMessageButtonClickListener = { sendMessageButtonClickListener() }
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
    public override fun renderState(state: MessageComposerState) {
        (binding.trailingContent.children.first() as? MessageComposerComponent)?.renderState(state)
        (binding.centerContent.children.first() as? MessageComposerComponent)?.renderState(state)
        (binding.leadingContent.children.first() as? MessageComposerComponent)?.renderState(state)
        (binding.footerContent.children.first() as? MessageComposerComponent)?.renderState(state)
        (binding.headerContent.children.first() as? MessageComposerComponent)?.renderState(state)

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
        (commandSuggestionsContent as? MessageComposerComponent)?.renderState(state)

        val suggestionsPopup = suggestionsPopup ?: MessageComposerSuggestionsPopup(commandSuggestionsContent, this) {
            suggestionsPopup = null
            dismissSuggestionsListener()
        }.apply {
            this@MessageComposerView.suggestionsPopup = this
        }

        suggestionsPopup.showOrUpdate()
    }

    /**
     * Displays list of mention suggestions, or updates it according to the [MessageComposerState.mentionSuggestions] list.
     *
     * @param state Current instance of [MessageComposerState].
     */
    private fun renderMentionSuggestions(state: MessageComposerState) {
        (mentionSuggestionsContent as? MessageComposerComponent)?.renderState(state)

        val suggestionsPopup = suggestionsPopup ?: MessageComposerSuggestionsPopup(mentionSuggestionsContent, this) {
            suggestionsPopup = null
            dismissSuggestionsListener()
        }.apply {
            this@MessageComposerView.suggestionsPopup = this
        }
        suggestionsPopup.showOrUpdate()
    }

    /**
     * Sets custom leading content view.
     *
     * @param view The [View] which replaces default leading content of [MessageComposerView]. It must implement [MessageComposerComponent] interface.
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container.
     */
    public fun <V> setLeadingContent(
        view: V,
        layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams,
    ) where V : View, V : MessageComposerComponent {
        binding.leadingContent.removeAllViews()
        binding.leadingContent.addView(view, layoutParams)
    }

    /**
     * Sets custom center content view.
     *
     * @param view The [View] which replaces default center content of [MessageComposerView]. It must implement [MessageComposerComponent] interface.
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container.
     */
    public fun <V> setCenterContent(
        view: V,
        layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams,
    ) where V : View, V : MessageComposerComponent {
        binding.centerContent.removeAllViews()
        binding.centerContent.addView(view, layoutParams)
    }

    /**
     * Sets custom trailing content view.
     *
     * @param view The [View] which replaces default trailing content of [MessageComposerView]. It must implement [MessageComposerComponent] interface.
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container.
     */
    public fun <V> setTrailingContent(
        view: V,
        layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams,
    ) where V : View, V : MessageComposerComponent {
        binding.trailingContent.removeAllViews()
        binding.trailingContent.addView(view, layoutParams)
    }

    /**
     * Sets custom footer content view.
     *
     * @param view The [View] which replaces default footer content of [MessageComposerView]. It must implement [MessageComposerComponent] interface.
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container.
     */
    public fun <V> setFooterContent(
        view: V,
        layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams,
    ) where V : View, V : MessageComposerComponent {
        binding.footerContent.removeAllViews()
        binding.footerContent.addView(view, layoutParams)
    }

    /**
     * Sets custom header content view.
     *
     * @param view The [View] which replaces default header content of [MessageComposerView]. It must implement [MessageComposerComponent] interface.
     * @param layoutParams The [FrameLayout.LayoutParams] defining how the view will be situated inside its container.
     */
    public fun <V> setHeaderContent(
        view: V,
        layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams,
    ) where V : View, V : MessageComposerComponent {
        binding.headerContent.removeAllViews()
        binding.headerContent.addView(view, layoutParams)
    }

    /**
     * Sets custom mention suggestions content view. It must implement [MessageComposerComponent] interface, and should
     * render mention suggestions according to the received state. List of currently available mention suggestions is propagated
     * to the [view] in the [MessageComposerComponent.renderState] hook function.
     *
     * @param view The [View] which shows mention suggestions list and allows to choose one of them.
     */
    public fun <V> setMentionSuggestionsContent(view: V) where V : View, V : MessageComposerComponent {
        mentionSuggestionsContentOverride = view
    }

    /**
     * Sets custom command suggestions content view. It must implement [MessageComposerComponent] interface, and should
     * render command suggestions according to the received state. List of currently available command suggestions is propagated
     * to the [view] in the [MessageComposerComponent.renderState] hook function.
     *
     * @param view The [View] which shows command suggestions list and allows to choose one of them.
     */
    public fun <V> setCommandSuggestionsContent(view: V) where V : View, V : MessageComposerComponent {
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
