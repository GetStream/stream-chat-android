/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
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
import io.getstream.chat.android.ui.message.composer.content.MessageComposerContent
import io.getstream.chat.android.ui.message.composer.internal.MessageComposerSuggestionsPopup
import io.getstream.chat.android.ui.message.composer.internal.ValidationErrorRenderer
import io.getstream.chat.android.ui.message.composer.internal.toAttachment
import io.getstream.chat.android.ui.message.composer.internal.toMessageInputViewStyle
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogFragment
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionListener
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSource

/**
 * UI component designed for handling message text input, attachments, actions,
 * and sending the message.
 */
@ExperimentalStreamChatApi
public class MessageComposerView : ConstraintLayout {
    /**
     * Generated binding class for the XML layout.
     */
    private lateinit var binding: StreamUiMessageComposerBinding

    /**
     * Legacy style that is needed for [AttachmentSelectionDialogFragment].
     */
    private lateinit var messageInputViewStyle: MessageInputViewStyle

    /**
     * The context that will be propagated to each content view.
     */
    private lateinit var messageComposerContext: MessageComposerContext

    /**
     * A helper class that displays validation errors.
     */
    private lateinit var validationErrorRenderer: ValidationErrorRenderer

    /**
     * Click listener for the send message button.
     */
    public var sendMessageButtonClickListener: () -> Unit = {}

    /**
     * Text change listener invoked each time after text was changed.
     */
    public var textInputChangeListener: (String) -> Unit = {}

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
     * Click listener invoked when suggestion popup is dismissed.
     */
    public var dismissSuggestionsListener: () -> Unit = {}

    /**
     * Click listener for the pick attachments button.
     */
    public var attachmentsButtonClickListener: () -> Unit = {
        context.getFragmentManager()?.let {
            AttachmentSelectionDialogFragment.newInstance(messageInputViewStyle)
                .apply {
                    val listener =
                        AttachmentSelectionListener { attachments: Set<AttachmentMetaData>, _: AttachmentSource ->
                            attachmentSelectionListener(attachments.map { it.toAttachment(requireContext()) })
                        }
                    setAttachmentSelectionListener(listener)
                    show(it, AttachmentSelectionDialogFragment.TAG)
                }
        }
    }

    /**
     * Handle for [PopupWindow] which is currently displayed. Used to display command
     * and mention suggestions.
     */
    private var suggestionsPopup: MessageComposerSuggestionsPopup? = null

    /**
     * The current list of command suggestions.
     */
    private var commandSuggestions: List<Command>? = null

    /**
     * The current list of mention suggestions.
     */
    private var mentionSuggestions: List<User>? = null

    /**
     * Default implementation of [mentionSuggestionsContent].
     */
    private val defaultMentionSuggestionsView: View by lazy {
        DefaultMessageComposerMentionSuggestionsContent(context).also {
            it.mentionSelectionListener = { user -> mentionSelectionListener(user) }
        }.attachContext()
    }

    /**
     * Handle for a custom mention suggestions view set with [setMentionSuggestionsContent].
     */
    private var mentionSuggestionsContentOverride: View? = null

    /**
     * Mention suggestions list shown in a popup window above the [MessageComposerView].
     */
    private val mentionSuggestionsContent: View
        get() = mentionSuggestionsContentOverride ?: defaultMentionSuggestionsView

    /**
     * Default implementation of [commandSuggestionsContent].
     */
    private val defaultCommandSuggestionsView: View by lazy {
        DefaultMessageComposerCommandSuggestionsContent(context).also {
            it.commandSelectionListener = { command -> commandSelectionListener(command) }
        }.attachContext()
    }

    /**
     * Handle for a custom command suggestions view set with [setCommandSuggestionsContent].
     */
    private var commandSuggestionsContentOverride: View? = null

    /**
     * Command suggestions list shown in a popup window above the [MessageComposerView].
     */
    private val commandSuggestionsContent: View
        get() = commandSuggestionsContentOverride ?: defaultCommandSuggestionsView

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    /**
     * Initializing the view with default contents.
     */
    private fun init(attr: AttributeSet? = null) {
        binding = StreamUiMessageComposerBinding.inflate(streamThemeInflater, this)

        validationErrorRenderer = ValidationErrorRenderer(context, this)
        messageInputViewStyle = AttachmentsPickerDialogStyle(context, attr).toMessageInputViewStyle(context)
        messageComposerContext = MessageComposerContext(MessageComposerViewStyle(context, attr))

        setBackgroundColor(messageComposerContext.style.backgroundColor)
        binding.separator.background = messageComposerContext.style.dividerBackgroundDrawable

        setLeadingContent(
            DefaultMessageComposerLeadingContent(context).also {
                it.attachmentsButtonClickListener = { attachmentsButtonClickListener() }
                it.commandsButtonClickListener = { commandsButtonClickListener() }
            }
        )
        setCenterContent(
            DefaultMessageComposerCenterContent(context).also {
                it.textInputChangeListener = { text -> textInputChangeListener(text) }
                it.attachmentRemovalListener = { attachment -> attachmentRemovalListener(attachment) }
            }
        )
        setTrailingContent(
            DefaultMessageComposerTrailingContent(context).also {
                it.sendMessageButtonClickListener = { sendMessageButtonClickListener() }
            }
        )
        setFooterContent(
            DefaultMessageComposerFooterContent(context).also {
                it.alsoSendToChannelSelectionListener = { checked -> alsoSendToChannelSelectionListener(checked) }
            }
        )
        setHeaderContent(
            DefaultMessageComposerHeaderContent(context).also {
                it.dismissActionClickListener = { dismissActionClickListener() }
            }
        )
    }

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    public fun renderState(state: MessageComposerState) {
        (binding.trailingContent.children.first() as? MessageComposerContent)?.renderState(state)
        (binding.centerContent.children.first() as? MessageComposerContent)?.renderState(state)
        (binding.leadingContent.children.first() as? MessageComposerContent)?.renderState(state)
        (binding.footerContent.children.first() as? MessageComposerContent)?.renderState(state)
        (binding.headerContent.children.first() as? MessageComposerContent)?.renderState(state)

        renderSuggestion(state)

        validationErrorRenderer.renderValidationErrors(state.validationErrors)
    }

    /**
     * Sets custom leading content view. It must implement [MessageComposerContent] interface and should
     * render integration buttons according to the received state. The current mode is propagated to the
     * [contentView] in the [MessageComposerContent.renderState] function.
     *
     * @param contentView The [View] which shows a section with integrations.
     * @param layoutParams The layout parameters to set on the content view.
     * @see [DefaultMessageComposerLeadingContent]
     */
    public fun <V> setLeadingContent(
        contentView: V,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        ),
    ) where V : View, V : MessageComposerContent {
        binding.leadingContent.removeAllViews()
        binding.leadingContent.addView(contentView.attachContext(), layoutParams)
    }

    /**
     * Sets custom center content view. It must implement [MessageComposerContent] interface and should
     * render a message input field according to the received state. The current mode is propagated to the
     * [contentView] in the [MessageComposerContent.renderState] function.
     *
     * @param contentView The [View] which shows a message input field.
     * @param layoutParams The layout parameters to set on the content view.
     * @see [DefaultMessageComposerCenterContent]
     */
    public fun <V> setCenterContent(
        contentView: V,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ),
    ) where V : View, V : MessageComposerContent {
        binding.centerContent.removeAllViews()
        binding.centerContent.addView(contentView.attachContext(), layoutParams)
    }

    /**
     * Sets custom trailing content view. It must implement [MessageComposerContent] interface and should
     * render a send button according to the received state. The current mode is propagated to the
     * [contentView] in the [MessageComposerContent.renderState] function.
     *
     * @param contentView The [View] which shows a send button.
     * @param layoutParams The layout parameters to set on the content view.
     * @see [DefaultMessageComposerTrailingContent]
     */
    public fun <V> setTrailingContent(
        contentView: V,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        ),
    ) where V : View, V : MessageComposerContent {
        binding.trailingContent.removeAllViews()
        binding.trailingContent.addView(contentView.attachContext(), layoutParams)
    }

    /**
     * Sets a custom footer content view. It must implement [MessageComposerContent] interface and should
     * render the "also send to channel" checkbox in the thread mode according to the received state. The
     * current mode is propagated to the [contentView] in the [MessageComposerContent.renderState] function.
     *
     * @param contentView The [View] which shows the currently active mode.
     * @param layoutParams The layout parameters to set on the content view.
     * @see [DefaultMessageComposerFooterContent]
     */
    public fun <V> setFooterContent(
        contentView: V,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ),
    ) where V : View, V : MessageComposerContent {
        binding.footerContent.removeAllViews()
        binding.footerContent.addView(contentView.attachContext(), layoutParams)
    }

    /**
     * Sets a custom header content view. It must implement the [MessageComposerContent] interface and should
     * render the currently active action according to the received state. The currently active action
     * is propagated to the [contentView] in the [MessageComposerContent.renderState] function.
     *
     * @param contentView The [View] which shows the currently active action.
     * @param layoutParams The layout parameters to set on the content view.
     * @see [DefaultMessageComposerHeaderContent]
     */
    public fun <V> setHeaderContent(
        contentView: V,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ),
    ) where V : View, V : MessageComposerContent {
        binding.headerContent.removeAllViews()
        binding.headerContent.addView(contentView.attachContext(), layoutParams)
    }

    /**
     * Sets custom mention suggestions content view. It must implement [MessageComposerContent] interface,
     * and should render mention suggestions according to the received state. List of currently available
     * mention suggestions is propagated to the [contentView] in the [MessageComposerContent.renderState]
     * function.
     *
     * @param contentView The [View] which shows the mention suggestions list and allows the user to choose one of them.
     * @see [DefaultMessageComposerMentionSuggestionsContent]
     */
    public fun <V> setMentionSuggestionsContent(contentView: V) where V : View, V : MessageComposerContent {
        mentionSuggestionsContentOverride = contentView.attachContext()
    }

    /**
     * Sets a custom command suggestions content view. It must implement the [MessageComposerContent] interface,
     * and should render command suggestions according to the received state. List of currently available
     * command suggestions is propagated to the [contentView] in the [MessageComposerContent.renderState]
     * function.
     *
     * @param contentView The [View] which shows command suggestions list and allows to choose one of them.
     * @see [DefaultMessageComposerCommandSuggestionsContent]
     */
    public fun <V> setCommandSuggestionsContent(contentView: V) where V : View, V : MessageComposerContent {
        commandSuggestionsContentOverride = contentView.attachContext()
    }

    /**
     * Makes the necessary clean up before the view is detached from window.
     */
    override fun onDetachedFromWindow() {
        suggestionsPopup?.dismiss()
        validationErrorRenderer.dismissValidationErrors()
        super.onDetachedFromWindow()
    }

    /**
     * Re-renders the suggestions popup window for the given [MessageComposerState] instance.
     *
     * @param state [MessageComposerState] instance representing current UI state.
     */
    private fun renderSuggestion(state: MessageComposerState) {
        when {
            state.mentionSuggestions.isNotEmpty() -> renderMentionSuggestions(state)
            state.commandSuggestions.isNotEmpty() -> renderCommandsSuggestions(state)
            else -> suggestionsPopup?.dismiss()
        }
        this.commandSuggestions = state.commandSuggestions
        this.mentionSuggestions = state.mentionSuggestions
    }

    /**
     * Displays a list of command suggestions, or updates it according to the state in
     * [MessageComposerState.commandSuggestions].
     *
     * @param state [MessageComposerState] instance representing current UI state.
     */
    private fun renderCommandsSuggestions(state: MessageComposerState) {
        // Do not do anything if the list hasn't changed
        if (this.commandSuggestions == state.commandSuggestions) return
        if (!messageComposerContext.style.messageInputCommandsHandlingEnabled) return

        (commandSuggestionsContent as? MessageComposerContent)?.renderState(state)

        val suggestionsPopup = suggestionsPopup ?: MessageComposerSuggestionsPopup(commandSuggestionsContent, this) {
            suggestionsPopup = null
            dismissSuggestionsListener()
        }.apply {
            this@MessageComposerView.suggestionsPopup = this
        }

        suggestionsPopup.showOrUpdate()
    }

    /**
     * Displays a list of mention suggestions, or updates it according to the state in
     * [MessageComposerState.mentionSuggestions].
     *
     * @param state [MessageComposerState] instance representing current UI state.
     */
    private fun renderMentionSuggestions(state: MessageComposerState) {
        // Do not do anything if the list hasn't changed
        if (this.mentionSuggestions == state.mentionSuggestions) return
        if (!messageComposerContext.style.messageInputMentionsHandlingEnabled) return

        (mentionSuggestionsContent as? MessageComposerContent)?.renderState(state)

        val suggestionsPopup = suggestionsPopup ?: MessageComposerSuggestionsPopup(mentionSuggestionsContent, this) {
            suggestionsPopup = null
            dismissSuggestionsListener()
        }.apply {
            this@MessageComposerView.suggestionsPopup = this
        }
        suggestionsPopup.showOrUpdate()
    }

    /**
     * Initializes the content view with [MessageComposerContext].
     */
    private fun <V> V.attachContext(): V where V : View, V : MessageComposerContent {
        attachContext(messageComposerContext)
        return this
    }
}
