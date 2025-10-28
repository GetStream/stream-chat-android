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

package io.getstream.chat.android.ui.feature.messages.composer

import android.content.Context
import android.graphics.Rect
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerBinding
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogFragment
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerCenterContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerCommandSuggestionsContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerFooterContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerHeaderContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerLeadingContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerMentionSuggestionsContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerOverlappingContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerTrailingContent
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerCenterContent
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerCommandSuggestionsContent
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerContent
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerContentContainer
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerFooterContent
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerHeaderContent
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerLeadingContent
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerMentionSuggestionsContent
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerOverlappingContent
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerTrailingContent
import io.getstream.chat.android.ui.feature.messages.composer.internal.MessageComposerSuggestionsPopup
import io.getstream.chat.android.ui.feature.messages.composer.internal.ValidationErrorRenderer
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.log.taggedLogger

/**
 * UI component designed for handling message text input, attachments, actions,
 * and sending the message.
 */
public class MessageComposerView : ConstraintLayout {

    private val logger by taggedLogger("Chat:MsgComposerView")

    private var arePollEnabled: Boolean = false

    /**
     * Generated binding class for the XML layout.
     */
    private lateinit var binding: StreamUiMessageComposerBinding

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
     * Selection listener invoked when a poll is submitted.
     */
    public var pollSubmissionListener: (PollConfig) -> Unit = {}

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
    public var commandsButtonClickListener: () -> Unit = {
        logger.d { "[onCommandsButtonClick] no args" }
    }

    /**
     * Click listener invoked when suggestion popup is dismissed.
     */
    public var dismissSuggestionsListener: () -> Unit = {}

    /**
     * Builder for the attachments picker dialog.
     */
    public var attachmentsPickerDialogBuilder: (AttachmentsPickerDialogStyle) -> AttachmentsPickerDialogFragment = {
        AttachmentsPickerDialogFragment
            .newInstance(it).apply {
                setAttachmentsSelectionListener { attachments: List<Attachment> ->
                    attachmentSelectionListener(attachments)
                }
                setPollSubmissionListener { pollConfig ->
                    pollSubmissionListener(pollConfig)
                }
            }
    }

    /**
     * Click listener for the pick attachments button.
     */
    public var attachmentsButtonClickListener: () -> Unit = {
        context.getFragmentManager()?.let {
            attachmentsPickerDialogBuilder(
                messageComposerContext.style.attachmentsPickerDialogStyle
                    .copy(
                        pollAttachmentsTabEnabled = arePollEnabled &&
                            messageComposerContext.style.attachmentsPickerDialogStyle.pollAttachmentsTabEnabled,
                    ),
            )
                .show(it, AttachmentsPickerDialogFragment.TAG)
        }
    }

    private var maxOffset = 0

    /**
     * Touch listener for the audio record button.
     */
    public var audioRecordButtonTouchListener: (event: MotionEvent) -> Boolean = { event ->
        // TODO delete commented code below after finalizing audio recording
        // maxOffset = maxOf(maxOffset, v.width - v.micButton.width)
        logger.v { "[onMicBtnTouchListener] event($maxOffset): $event" }
        // event.offsetLocation(maxOffset.toFloat(), 0f)
        binding.centerOverlapContent.children.first().dispatchTouchEvent(event)
        true
    }

    /**
     * Hold listener invoked when the microphone button gets pressed down.
     */
    public var audioRecordButtonHoldListener: () -> Unit = {}

    /**
     * Lock listener invoked when the audio recording gets locked.
     */
    public var audioRecordButtonLockListener: () -> Unit = {}

    /**
     * Cancel listener invoked when the audio recording gets cancelled.
     */
    public var audioRecordButtonCancelListener: () -> Unit = {}

    /**
     * Release listener invoked when the microphone button gets released.
     */
    public var audioRecordButtonReleaseListener: () -> Unit = {}

    /**
     * Click listener for the audio recording delete button.
     */
    public var audioDeleteButtonClickListener: () -> Unit = {}

    /**
     * Click listener for the audio recording stop button.
     */
    public var audioStopButtonClickListener: () -> Unit = {}

    /**
     * Click listener for the audio recording playback button.
     */
    public var audioPlaybackButtonClickListener: () -> Unit = {}

    /**
     * Click listener for the audio recording complete button.
     */
    public var audioCompleteButtonClickListener: () -> Unit = {}

    /**
     * Drag start listener invoked when the audio slider starts being dragged.
     */
    public var audioSliderDragStartListener: (Float) -> Unit = {}

    /**
     * Drag stop listener invoked when the audio slider stops being dragged.
     */
    public var audioSliderDragStopListener: (Float) -> Unit = {}

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
        defStyleAttr,
    ) {
        init(attrs)
    }

    /**
     * Initializing the view with default contents.
     */
    private fun init(attrs: AttributeSet? = null) {
        binding = StreamUiMessageComposerBinding.inflate(streamThemeInflater, this)

        validationErrorRenderer = ValidationErrorRenderer(context, this)
        messageComposerContext = MessageComposerContext(
            MessageComposerViewStyle(context, attrs),
            binding.asContentContainer(),
        )

        setBackgroundColor(messageComposerContext.style.backgroundColor)
        binding.separator.background = messageComposerContext.style.dividerBackgroundDrawable

        setLeadingContent(
            DefaultMessageComposerLeadingContent(context).also {
                it.attachmentsButtonClickListener = { attachmentsButtonClickListener() }
                it.commandsButtonClickListener = { commandsButtonClickListener() }
            },
        )
        setCenterContent(
            DefaultMessageComposerCenterContent(context).also {
                it.textInputChangeListener = { text -> textInputChangeListener(text) }
                it.attachmentRemovalListener = { attachment -> attachmentRemovalListener(attachment) }
            },
        )
        setTrailingContent(
            DefaultMessageComposerTrailingContent(context).also {
                it.sendMessageButtonClickListener = { sendMessageButtonClickListener() }
                it.recordAudioButtonTouchListener = { event -> audioRecordButtonTouchListener(event) }
            },
        )
        setFooterContent(
            DefaultMessageComposerFooterContent(context).also {
                it.alsoSendToChannelSelectionListener = { checked -> alsoSendToChannelSelectionListener(checked) }
            },
        )
        setHeaderContent(
            DefaultMessageComposerHeaderContent(context).also {
                it.dismissActionClickListener = { dismissActionClickListener() }
            },
        )
        setCenterOverlapContent(
            DefaultMessageComposerOverlappingContent(context).also {
                it.recordButtonHoldListener = { audioRecordButtonHoldListener() }
                it.recordButtonLockListener = { audioRecordButtonLockListener() }
                it.recordButtonCancelListener = { audioRecordButtonCancelListener() }
                it.recordButtonReleaseListener = { audioRecordButtonReleaseListener() }
                it.deleteButtonClickListener = { audioDeleteButtonClickListener() }
                it.stopButtonClickListener = { audioStopButtonClickListener() }
                it.playbackButtonClickListener = { audioPlaybackButtonClickListener() }
                it.completeButtonClickListener = { audioCompleteButtonClickListener() }
                it.sliderDragStartListener = { progress -> audioSliderDragStartListener(progress) }
                it.sliderDragStopListener = { progress -> audioSliderDragStopListener(progress) }
            },
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        logger.w { "[onRestoreInstanceState] state: $state" }
        super.onRestoreInstanceState(state)
    }

    override fun onSaveInstanceState(): Parcelable? {
        logger.w { "[onSaveInstanceState] no args" }
        return super.onSaveInstanceState()
    }

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    public fun renderState(state: MessageComposerState) {
        (binding.trailingContent.children.first() as? MessageComposerContent)?.renderState(state)
        (binding.centerContent.children.first() as? MessageComposerContent)?.renderState(state)
        (binding.centerOverlapContent.children.first() as? MessageComposerContent)?.renderState(state)
        (binding.leadingContent.children.first() as? MessageComposerContent)?.renderState(state)
        (binding.footerContent.children.first() as? MessageComposerContent)?.renderState(state)
        (binding.headerContent.children.first() as? MessageComposerContent)?.renderState(state)

        renderSuggestion(state)
        arePollEnabled = arePollsEnabled(state)
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
    @JvmOverloads
    public fun <V> setLeadingContent(
        contentView: V,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM,
        ),
    ) where V : View, V : MessageComposerContent {
        binding.leadingContent.removeAllViews()
        binding.leadingContent.addView(contentView.attachContext(), layoutParams)
        if (contentView is MessageComposerLeadingContent) {
            if (contentView.attachmentsButtonClickListener == null) {
                contentView.attachmentsButtonClickListener = { attachmentsButtonClickListener() }
            }
            if (contentView.commandsButtonClickListener == null) {
                contentView.commandsButtonClickListener = { commandsButtonClickListener() }
            }
        }
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
    @JvmOverloads
    public fun <V> setCenterContent(
        contentView: V,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ),
    ) where V : View, V : MessageComposerContent {
        binding.centerContent.removeAllViews()
        binding.centerContent.addView(contentView.attachContext(), layoutParams)
        if (contentView is MessageComposerCenterContent) {
            if (contentView.textInputChangeListener == null) {
                contentView.textInputChangeListener = { textInputChangeListener(it) }
            }
            if (contentView.attachmentRemovalListener == null) {
                contentView.attachmentRemovalListener = { attachmentRemovalListener(it) }
            }
        }
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
    @JvmOverloads
    public fun <V> setTrailingContent(
        contentView: V,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM,
        ),
    ) where V : View, V : MessageComposerContent {
        binding.trailingContent.removeAllViews()
        binding.trailingContent.addView(contentView.attachContext(), layoutParams)
        if (contentView is MessageComposerTrailingContent) {
            if (contentView.sendMessageButtonClickListener == null) {
                contentView.sendMessageButtonClickListener = { sendMessageButtonClickListener() }
            }
            if (contentView.recordAudioButtonTouchListener == null) {
                contentView.recordAudioButtonTouchListener = { audioRecordButtonTouchListener(it) }
            }
        }
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
    @JvmOverloads
    public fun <V> setFooterContent(
        contentView: V,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ),
    ) where V : View, V : MessageComposerContent {
        binding.footerContent.removeAllViews()
        binding.footerContent.addView(contentView.attachContext(), layoutParams)
        if (contentView is MessageComposerFooterContent) {
            if (contentView.alsoSendToChannelSelectionListener == null) {
                contentView.alsoSendToChannelSelectionListener = { alsoSendToChannelSelectionListener(it) }
            }
        }
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
    @JvmOverloads
    public fun <V> setHeaderContent(
        contentView: V,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ),
    ) where V : View, V : MessageComposerContent {
        binding.headerContent.removeAllViews()
        binding.headerContent.addView(contentView.attachContext(), layoutParams)
        if (contentView is MessageComposerHeaderContent) {
            if (contentView.dismissActionClickListener == null) {
                contentView.dismissActionClickListener = { dismissActionClickListener() }
            }
        }
    }

    /**
     * Sets a custom overlapping content view. It must implement the [MessageComposerContent] interface and should
     * render the currently active action according to the received state. The currently active action
     * is propagated to the [contentView] in the [MessageComposerContent.renderState] function.
     *
     * @param contentView The [View] which shows the currently active action.
     * @param layoutParams The layout parameters to set on the content view.
     * @see [DefaultMessageComposerOverlappingContent]
     */
    @JvmOverloads
    public fun <V> setCenterOverlapContent(
        contentView: V,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ),
    ) where V : View, V : MessageComposerContent {
        binding.centerOverlapContent.removeAllViews()
        binding.centerOverlapContent.addView(contentView.attachContext(), layoutParams)
        if (contentView is MessageComposerOverlappingContent) {
            if (contentView.recordButtonHoldListener == null) {
                contentView.recordButtonHoldListener = { audioRecordButtonHoldListener() }
            }
            if (contentView.recordButtonLockListener == null) {
                contentView.recordButtonLockListener = { audioRecordButtonLockListener() }
            }
            if (contentView.recordButtonCancelListener == null) {
                contentView.recordButtonCancelListener = { audioRecordButtonCancelListener() }
            }
            if (contentView.recordButtonReleaseListener == null) {
                contentView.recordButtonReleaseListener = { audioRecordButtonReleaseListener() }
            }
            if (contentView.deleteButtonClickListener == null) {
                contentView.deleteButtonClickListener = { audioDeleteButtonClickListener() }
            }
            if (contentView.stopButtonClickListener == null) {
                contentView.stopButtonClickListener = { audioStopButtonClickListener() }
            }
            if (contentView.playbackButtonClickListener == null) {
                contentView.playbackButtonClickListener = { audioPlaybackButtonClickListener() }
            }
            if (contentView.completeButtonClickListener == null) {
                contentView.completeButtonClickListener = { audioCompleteButtonClickListener() }
            }
            if (contentView.sliderDragStartListener == null) {
                contentView.sliderDragStartListener = { audioSliderDragStartListener(it) }
            }
            if (contentView.sliderDragStopListener == null) {
                contentView.sliderDragStopListener = { audioSliderDragStopListener(it) }
            }
        }
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
        if (contentView is MessageComposerMentionSuggestionsContent) {
            if (contentView.mentionSelectionListener == null) {
                contentView.mentionSelectionListener = { mentionSelectionListener(it) }
            }
        }
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
        if (contentView is MessageComposerCommandSuggestionsContent) {
            if (contentView.commandSelectionListener == null) {
                contentView.commandSelectionListener = { commandSelectionListener(it) }
            }
        }
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
            setTouchInterceptor(SuggestionPopupTouchListener())
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

    /**
     * Checks if the current state allows sending polls as attachments:
     * 1. The message mode is [MessageMode.Normal] (polls are not allowed in threads).
     * 2. Polls are enabled in the channel.
     * 3. The user has the [ChannelCapabilities.SEND_POLL] capability.
     */
    private fun arePollsEnabled(state: MessageComposerState): Boolean = state.messageMode is MessageMode.Normal &&
        state.pollsEnabled &&
        state.ownCapabilities.contains(ChannelCapabilities.SEND_POLL)

    /**
     * A listener that helps to hide the currently visible command suggestions popup when the
     * commands button is clicked.
     *
     * In general we don't want the suggestion popup to be "modal". An outside click should dismiss
     * the popup and then the click event should be passed down to the layout below. For example,
     * if the suggestion popup is displayed and the user clicks on the back button in the toolbar,
     * then the popup should be dismissed and the user should be navigated back to the previous
     * screen.
     *
     * However, there is one exception to this rule. If the command suggestion popup is displayed
     * and the user clicks the commands button again the click should be consumed as otherwise the
     * popup will be dismissed by the dismiss listener and then immediately shown again by the
     * commands button click listener. This listener addresses this problem.
     */
    private inner class SuggestionPopupTouchListener : OnTouchListener {

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            // Don't intercept the event if the suggestions popup is hidden
            val suggestionsPopup = suggestionsPopup ?: return false

            if (event != null && event.action == MotionEvent.ACTION_OUTSIDE) {
                val suggestionsPopupLocation = IntArray(2)
                suggestionsPopup.contentView.getLocationOnScreen(suggestionsPopupLocation)

                // Click position relative to the popup
                val relativeX = event.x
                val relativeY = event.y

                // Click position in global coordinates
                val absoluteX = suggestionsPopupLocation[0] + relativeX
                val absoluteY = suggestionsPopupLocation[1] + relativeY

                val rect = Rect()
                binding.leadingContent.getGlobalVisibleRect(rect)
                if (rect.contains(absoluteX.toInt(), absoluteY.toInt())) {
                    // Consume touch event outside the popup if the touch
                    // position belongs to the leading content area.
                    return true
                }
            }
            return false
        }
    }
}

private fun StreamUiMessageComposerBinding.asContentContainer() = object : MessageComposerContentContainer {
    private val childCount = 6

    override val center get() = centerContent.children.firstOrNull() as? MessageComposerContent
    override val centerOverlap get() = centerOverlapContent.children.firstOrNull() as? MessageComposerContent
    override val leading get() = leadingContent.children.firstOrNull() as? MessageComposerContent
    override val trailing get() = trailingContent.children.firstOrNull() as? MessageComposerContent
    override val header get() = headerContent.children.firstOrNull() as? MessageComposerContent
    override val footer get() = footerContent.children.firstOrNull() as? MessageComposerContent

    override fun asView(): View = root

    override fun findViewByKey(key: String): View? {
        for (content in this) {
            return content?.findViewByKey(key) ?: continue
        }
        return null
    }

    private fun getChildAt(index: Int): MessageComposerContent? = when (index) {
        0 -> center
        1 -> centerOverlap
        2 -> leading
        3 -> trailing
        4 -> header
        5 -> footer
        else -> null
    }

    override fun iterator(): Iterator<MessageComposerContent?> = object : Iterator<MessageComposerContent?> {
        private var index = 0
        override fun hasNext() = index < childCount
        override fun next() = getChildAt(index++) ?: throw IndexOutOfBoundsException()
    }
}
