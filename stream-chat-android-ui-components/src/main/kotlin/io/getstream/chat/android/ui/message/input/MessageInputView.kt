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

package io.getstream.chat.android.ui.message.input

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.EditText
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.core.view.updatePadding
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.utils.extensions.activity
import com.getstream.sdk.chat.utils.extensions.focusAndShowKeyboard
import com.getstream.sdk.chat.utils.typing.TypingUpdatesBuffer
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.Debouncer
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.getFragmentManager
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiMessageInputBinding
import io.getstream.chat.android.ui.message.input.MessageInputView.MaxMessageLengthHandler
import io.getstream.chat.android.ui.message.input.MessageInputView.MessageInputMentionListener
import io.getstream.chat.android.ui.message.input.MessageInputView.MessageInputViewModeListener
import io.getstream.chat.android.ui.message.input.MessageInputView.SelectedAttachmentsCountListener
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogFragment
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionListener
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSource
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedCustomAttachmentViewHolderFactory
import io.getstream.chat.android.ui.message.input.internal.MessageInputFieldView
import io.getstream.chat.android.ui.message.input.mention.searchUsers
import io.getstream.chat.android.ui.message.input.transliteration.DefaultStreamTransliterator
import io.getstream.chat.android.ui.message.input.transliteration.StreamTransliterator
import io.getstream.chat.android.ui.suggestion.list.DefaultSuggestionListControllerListener
import io.getstream.chat.android.ui.suggestion.list.SuggestionListController
import io.getstream.chat.android.ui.suggestion.list.SuggestionListControllerListener
import io.getstream.chat.android.ui.suggestion.list.SuggestionListView
import io.getstream.chat.android.ui.suggestion.list.SuggestionListViewStyle
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItemViewHolderFactory
import io.getstream.chat.android.ui.suggestion.list.internal.SuggestionListPopupWindow
import io.getstream.chat.android.ui.utils.extensions.setBorderlessRipple
import io.getstream.chat.android.uiutils.extension.containsLinks
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar
import java.io.File
import kotlin.properties.Delegates

public class MessageInputView : ConstraintLayout {
    private val logger = StreamLog.getLogger("Chat:MessageInputView")

    public var inputMode: InputMode by Delegates.observable(InputMode.Normal) { _, previousValue, newValue ->
        configSendAlsoToChannelCheckbox()
        configInputMode(previousValue, newValue)
        messageInputViewModeListener.inputModeChanged(inputMode = newValue)
    }

    public var chatMode: ChatMode by Delegates.observable(ChatMode.GROUP_CHAT) { _, _, _ ->
        configSendAlsoToChannelCheckbox()
    }

    private lateinit var binding: StreamUiMessageInputBinding
    private lateinit var messageInputViewStyle: MessageInputViewStyle
    private lateinit var suggestionListViewStyle: SuggestionListViewStyle
    private lateinit var suggestionListView: SuggestionListView

    private var currentAnimatorSet: AnimatorSet? = null
    private var sendMessageHandler: MessageSendHandler = EMPTY_MESSAGE_SEND_HANDLER
    private var suggestionListController: SuggestionListController? = null
    private var isSendButtonEnabled: Boolean = true
    private var mentionsEnabled: Boolean = true
    private var commandsEnabled: Boolean = true

    private var onSendButtonClickListener: OnMessageSendButtonClickListener? = null

    /**
     * Used to buffer typing updates in order to conserve API calls.
     */
    private var typingUpdatesBuffer: TypingUpdatesBuffer? = null
    private var keyboardListener: Unregistrar? = null

    private var maxMessageLength: Int = Integer.MAX_VALUE

    private var cooldownInterval: Int = 0
    private var cooldownTimerJob: Job? = null

    private var currentlyActiveSnackBar: Snackbar? = null

    /**
     * Used to enable or disable parts of the UI depending
     * on which abilities the user has in the given channel.
     */
    private var ownCapabilities: Set<String> = setOf()

    // used to regulate UI according to ownCapabilities
    private var canSendAttachments = false
    private var canUseCommands = false
    private var canSendLinks = false
    private var canSendTypingUpdates = false
    private var hasCommands: Boolean = false

    /**
     * Changes value only when the new value
     * is not the same as the current one.
     *
     * Disables or enables the send button and
     * displays a snackbar when necessary.
     *
     * Note: This value is updated only if
     * the user is not allowed to send links.
     */
    private var inputContainsLinks = false
        set(value) {
            if (value != inputContainsLinks) {
                field = value
                if (field) {
                    disableSendButton()
                    alertInputContainsLinkWhenNotAllowed()
                } else {
                    enableSendButton()
                }
            }
        }

    private val attachmentSelectionListener = AttachmentSelectionListener { attachments, attachmentSource ->
        if (attachments.isNotEmpty()) {
            when (attachmentSource) {
                AttachmentSource.MEDIA,
                AttachmentSource.CAMERA,
                -> {
                    binding.messageInputFieldView.mode =
                        MessageInputFieldView.Mode.MediaAttachmentMode(attachments.toList())
                }
                AttachmentSource.FILE -> {
                    binding.messageInputFieldView.mode =
                        MessageInputFieldView.Mode.FileAttachmentMode(attachments.toList())
                }
            }
        }
    }

    private val customAttachmentsSelectionListener =
        { attachments: Collection<Attachment>, viewHolderFactory: SelectedCustomAttachmentViewHolderFactory ->
            binding.messageInputFieldView.mode =
                MessageInputFieldView.Mode.CustomAttachmentMode(attachments.toList(), viewHolderFactory)
        }

    /**
     * The default implementation of [MaxMessageLengthHandler] which uses the default [EditText] error
     * popup to display errors when the maximum message length is exceeded.
     */
    private var maxMessageLengthHandler: MaxMessageLengthHandler =
        MaxMessageLengthHandler { _, _, maxMessageLength, maxMessageLengthExceeded ->
            binding.messageInputFieldView.binding.messageEditText.error = if (maxMessageLengthExceeded) {
                context.getString(R.string.stream_ui_message_input_error_max_length, maxMessageLength)
            } else {
                null
            }
        }

    private var userLookupHandler: UserLookupHandler = DefaultUserLookupHandler(emptyList())
    private var messageInputDebouncer: Debouncer? = null

    private var scope: CoroutineScope? = null
    private var bigFileSelectionListener: BigFileSelectionListener? = null

    /**
     * Listener used for reacting to changes in the number of selected attachments
     */
    private var selectedAttachmentsCountListener: SelectedAttachmentsCountListener =
        SelectedAttachmentsCountListener { attachmentsCount, maxAttachmentsCount ->

            suggestionListController?.commandsEnabled = commandsEnabled && attachmentsCount == 0

            if (attachmentsCount > maxAttachmentsCount) {
                alertMaxAttachmentsCountExceeded()
            }
        }

    private var messageInputViewModeListener: MessageInputViewModeListener = MessageInputViewModeListener { }

    /**
     * Listener that handles selected mentions.
     */
    private var messageInputMentionListener: MessageInputMentionListener = MessageInputMentionListener { }

    public constructor (context: Context) : this(context, null)

    public constructor (context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor (context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    /*
    * Sets direction of MessageInputTextDirection. Default is TEXT_DIRECTION_FIRST_STRONG_LTR
    */
    public fun setInputTextDirection(direction: Int) {
        binding.messageInputFieldView.binding.messageEditText.textDirection = direction
    }

    private fun configInputMode(previousValue: InputMode, newValue: InputMode) {
        when (newValue) {
            is InputMode.Reply -> {
                suggestionListController?.commandsEnabled = commandsEnabled
                binding.inputModeHeader.isVisible = true
                binding.headerLabel.text = context.getString(R.string.stream_ui_message_input_reply)
                binding.inputModeIcon.setImageDrawable(messageInputViewStyle.replyInputModeIcon)
                binding.messageInputFieldView.onReply(newValue.repliedMessage)
                binding.messageInputFieldView.binding.messageEditText.focusAndShowKeyboard()
            }

            is InputMode.Edit -> {
                suggestionListController?.commandsEnabled = false
                binding.inputModeHeader.isVisible = true
                binding.headerLabel.text = context.getString(R.string.stream_ui_message_list_edit_message)
                binding.inputModeIcon.setImageDrawable(messageInputViewStyle.editInputModeIcon)
                binding.messageInputFieldView.onEdit(newValue.oldMessage)
                binding.commandsButton.isEnabled = false
                binding.messageInputFieldView.binding.messageEditText.focusAndShowKeyboard()
            }

            else -> {
                suggestionListController?.commandsEnabled = commandsEnabled
                binding.inputModeHeader.isVisible = false
                if (previousValue is InputMode.Reply) {
                    binding.messageInputFieldView.onReplyDismissed()
                } else if (previousValue is InputMode.Edit) {
                    binding.messageInputFieldView.onEditMessageDismissed()
                }
            }
        }
    }

    public fun setOnSendButtonClickListener(listener: OnMessageSendButtonClickListener?) {
        this.onSendButtonClickListener = listener
    }

    /**
     * Sets the typing updates buffer.
     */
    public fun setTypingUpdatesBuffer(buffer: TypingUpdatesBuffer) {
        typingUpdatesBuffer = buffer
    }

    /**
     * Sets up [MessageSendHandler] implementation. [MessageInputView] will delegate all the message sending operations
     * to this object.
     */
    public fun setSendMessageHandler(handler: MessageSendHandler) {
        this.sendMessageHandler = handler
    }

    public fun setCommands(commands: List<Command>) {
        suggestionListController?.commands = commands
        hasCommands = commands.isNotEmpty()
        refreshControlsState()
    }

    public fun enableSendButton() {
        isSendButtonEnabled = true
        refreshControlsState()
    }

    public fun disableSendButton() {
        isSendButtonEnabled = false
        refreshControlsState()
    }

    /**
     * Enables or disables the handling of mentions in the message input view.
     *
     * @param enabled True if handling of mentions in the message input view is enabled, false otherwise.
     */
    public fun setMentionsEnabled(enabled: Boolean) {
        this.mentionsEnabled = enabled
        suggestionListController?.mentionsEnabled = mentionsEnabled
    }

    /**
     * Enables or disables the commands feature.
     *
     * @param enabled True if commands are enabled, false otherwise.
     */
    public fun setCommandsEnabled(enabled: Boolean) {
        commandsEnabled = enabled
        suggestionListController?.commandsEnabled = commandsEnabled
        refreshControlsState()
    }

    public fun setSuggestionListViewHolderFactory(viewHolderFactory: SuggestionListItemViewHolderFactory) {
        suggestionListView.setSuggestionListViewHolderFactory(viewHolderFactory)
    }

    /**
     * Hides the suggestion list popup.
     */
    public fun hideSuggestionList() {
        suggestionListController?.hideSuggestionList()
    }

    private fun setSuggestionListViewInternal(suggestionListView: SuggestionListView, popupWindow: Boolean = true) {
        this.suggestionListView = suggestionListView

        suggestionListView.setSuggestionListViewStyle(suggestionListViewStyle)
        suggestionListView.setOnSuggestionClickListener(
            object : SuggestionListView.OnSuggestionClickListener {
                override fun onMentionClick(user: User) {
                    binding.messageInputFieldView.autoCompleteUser(user)
                    messageInputMentionListener.onMentionSelected(user)
                }

                override fun onCommandClick(command: Command) {
                    binding.messageInputFieldView.autoCompleteCommand(command)
                }
            }
        )

        val dismissListener = PopupWindow.OnDismissListener {
            binding.commandsButton.postDelayed(CLICK_DELAY) {
                binding.commandsButton.isSelected = false
                hideSuggestionList()
            }
        }
        val suggestionListUi = if (popupWindow) {
            SuggestionListPopupWindow(suggestionListView, this, dismissListener)
        } else {
            suggestionListView
        }
        suggestionListController = SuggestionListController(
            suggestionListUi = suggestionListUi,
            suggestionListControllerListener = createSuggestionsListControllerListener()
        ).also {
            it.mentionsEnabled = mentionsEnabled
            it.commandsEnabled = commandsEnabled
            it.userLookupHandler = userLookupHandler
        }
        refreshControlsState()
    }

    public fun setUserLookupHandler(handler: UserLookupHandler) {
        this.userLookupHandler = handler
        suggestionListController?.userLookupHandler = handler
    }

    /**
     * Sets the maximum message length. When a message exceeds this limit, the send button becomes
     * disabled and a error message is shown.
     *
     * @param maxMessageLength the maximum message length in characters.
     *
     * @see [setMaxMessageLengthHandler] for more information on how to provide a custom error message.
     */
    public fun setMaxMessageLength(maxMessageLength: Int) {
        this.maxMessageLength = maxMessageLength
    }

    /**
     * Sets the cooldown interval. When slow mode is enabled, users can only send messages every
     * [cooldownInterval] time interval.
     *
     * @param cooldownInterval The cooldown interval in seconds.
     */
    public fun setCooldownInterval(cooldownInterval: Int) {
        this.cooldownInterval = cooldownInterval
    }

    /**
     * Sets a custom [MaxMessageLengthHandler] which is responsible to handling max-length errors.
     */
    public fun setMaxMessageLengthHandler(maxMessageLengthHandler: MaxMessageLengthHandler) {
        this.maxMessageLengthHandler = maxMessageLengthHandler
    }

    private fun isMessageTooLong(): Boolean {
        return binding.messageInputFieldView.messageText.length > maxMessageLength
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        messageInputDebouncer = Debouncer(TYPING_DEBOUNCE_MS)
        scope = CoroutineScope(DispatcherProvider.Main).apply {
            launch { binding.messageInputFieldView.hasBigAttachment.collect(::consumeHasBigFile) }
            launch { binding.messageInputFieldView.selectedAttachmentsCount.collect(::consumeSelectedAttachmentsCount) }
        }
    }

    override fun onDetachedFromWindow() {
        currentlyActiveSnackBar?.dismiss()
        messageInputDebouncer?.shutdown()
        messageInputDebouncer = null
        cooldownTimerJob?.cancel()
        cooldownTimerJob = null
        hideSuggestionList()
        scope?.cancel()
        scope = null
        keyboardListener?.unregister()
        keyboardListener = null
        super.onDetachedFromWindow()
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(attr: AttributeSet? = null) {
        binding = StreamUiMessageInputBinding.inflate(streamThemeInflater, this)
        messageInputViewStyle = MessageInputViewStyle(context, attr)
        suggestionListViewStyle = SuggestionListViewStyle(context, attr)
        binding.messageInputFieldView.messageReplyStyle = messageInputViewStyle.toMessageReplyStyle()

        setBackgroundColor(messageInputViewStyle.backgroundColor)
        configAttachmentButton()
        configCommandsButton()
        configTextInput()
        configSendButton()
        configSendAlsoToChannelCheckbox()
        configSendButtonListener()
        binding.dismissInputMode.setOnClickListener { dismissInputMode(inputMode) }
        setMentionsEnabled(messageInputViewStyle.mentionsEnabled)
        setCommandsEnabled(messageInputViewStyle.commandsEnabled)
        setSuggestionListViewInternal(SuggestionListView(context))
        binding.messageInputFieldView.apply {
            setAttachmentMaxFileMb(messageInputViewStyle.attachmentMaxFileSize)
            maxAttachmentsCount = messageInputViewStyle.maxAttachmentsCount
        }

        val horizontalPadding = resources.getDimensionPixelSize(R.dimen.stream_ui_spacing_tiny)
        updatePadding(left = horizontalPadding, right = horizontalPadding)

        refreshControlsState()
    }

    public fun listenForBigAttachments(listener: BigFileSelectionListener) {
        bigFileSelectionListener = listener
    }

    /**
     * Sets [SelectedAttachmentsCountListener] invoked when attachments count changes
     *
     * @param listener The listener to be set
     */
    public fun setSelectedAttachmentsCountListener(listener: SelectedAttachmentsCountListener) {
        selectedAttachmentsCountListener = listener
    }

    private fun dismissInputMode(inputMode: InputMode) {
        if (inputMode is InputMode.Reply) {
            sendMessageHandler.dismissReply()
        }

        this.inputMode = InputMode.Normal
    }

    private fun configSendButtonListener() {
        binding.sendMessageButtonEnabled.setOnClickListener {
            when {
                binding.messageInputFieldView.hasBigAttachment.value -> {
                    consumeHasBigFile(hasBigFile = true)
                }
                binding.messageInputFieldView.selectedAttachmentsCount.value > messageInputViewStyle.maxAttachmentsCount -> {
                    consumeSelectedAttachmentsCount(attachmentsCount = binding.messageInputFieldView.selectedAttachmentsCount.value)
                }
                else -> {
                    onSendButtonClickListener?.onClick()
                    inputMode.let {
                        when (it) {
                            is InputMode.Normal -> sendMessage()
                            is InputMode.Thread -> sendThreadMessage(it.parentMessage)
                            is InputMode.Edit -> editMessage(it.oldMessage)
                            is InputMode.Reply -> sendMessage(it.repliedMessage)
                        }
                    }
                    binding.messageInputFieldView.clearContent()
                    startCooldownTimerIfNecessary()
                }
            }
        }
    }

    private fun consumeHasBigFile(hasBigFile: Boolean) {
        bigFileSelectionListener?.handleBigFileSelected(hasBigFile) ?: let {
            if (hasBigFile) {
                alertBigFileSendAttempt()
            }
        }
    }

    private fun consumeSelectedAttachmentsCount(attachmentsCount: Int) {
        selectedAttachmentsCountListener.attachmentsCountChanged(
            attachmentsCount = attachmentsCount,
            maxAttachmentsCount = messageInputViewStyle.maxAttachmentsCount,
        )
    }

    private fun alertBigFileSendAttempt() {
        Snackbar.make(
            this,
            resources.getString(
                R.string.stream_ui_message_input_error_file_large_size,
                messageInputViewStyle.attachmentMaxFileSize
            ),
            Snackbar.LENGTH_LONG
        )
            .apply {
                currentlyActiveSnackBar = this
                anchorView = this@MessageInputView
            }
            .show()
    }

    private fun alertMaxAttachmentsCountExceeded() {
        Snackbar.make(
            this,
            resources.getString(
                R.string.stream_ui_message_input_error_max_attachments_count_exceeded,
                messageInputViewStyle.maxAttachmentsCount
            ),
            Snackbar.LENGTH_LONG,
        )
            .apply {
                currentlyActiveSnackBar = this
                anchorView = this@MessageInputView
            }
            .show()
    }

    /**
     * Displays a snackbar informing the user that
     * sending links is not allowed in the given channel
     */
    private fun alertInputContainsLinkWhenNotAllowed() {
        Snackbar.make(
            this,
            resources.getString(
                R.string.stream_ui_message_input_error_sending_links_not_allowed,
            ),
            Snackbar.LENGTH_INDEFINITE,
        )
            .apply {
                currentlyActiveSnackBar = this
                anchorView = this@MessageInputView
                setAction(R.string.stream_ui_ok) { dismiss() }
            }
            .show()
    }

    /**
     * Shows cooldown countdown timer instead of send button when slow mode is enabled.
     */
    private fun startCooldownTimerIfNecessary() {
        if (cooldownInterval > 0) {
            cooldownTimerJob?.cancel()
            cooldownTimerJob = findViewTreeLifecycleOwner()?.lifecycleScope?.launch(DispatcherProvider.Main) {
                with(binding) {
                    val previousInputHint = binding.messageInputFieldView.messageHint

                    disableSendButton()
                    cooldownBadgeTextView.isVisible = true
                    messageInputFieldView.messageHint =
                        context.getString(R.string.stream_ui_message_input_slow_mode_hint)

                    for (timeRemaining in cooldownInterval downTo 1) {
                        cooldownBadgeTextView.text = "$timeRemaining"
                        delay(1000)
                    }

                    enableSendButton()
                    cooldownBadgeTextView.isVisible = false
                    messageInputFieldView.messageHint = previousInputHint
                }
            }
        }
    }

    private fun configSendAlsoToChannelCheckbox() {
        val isThreadModeActive = inputMode is InputMode.Thread
        val shouldShowCheckbox = messageInputViewStyle.showSendAlsoToChannelCheckbox && isThreadModeActive
        if (shouldShowCheckbox) {
            val text = when (chatMode) {
                ChatMode.GROUP_CHAT -> {
                    messageInputViewStyle.sendAlsoToChannelCheckboxGroupChatText
                        ?: context.getString(R.string.stream_ui_message_input_send_to_channel)
                }
                ChatMode.DIRECT_CHAT -> {
                    messageInputViewStyle.sendAlsoToChannelCheckboxDirectChatText
                        ?: context.getString(R.string.stream_ui_message_input_send_as_direct_message)
                }
            }
            binding.sendAlsoToChannel.text = text
            messageInputViewStyle.sendAlsoToChannelCheckboxDrawable?.let {
                binding.sendAlsoToChannel.buttonDrawable = it
            }
            messageInputViewStyle.sendAlsoToChannelCheckboxTextStyle.apply(binding.sendAlsoToChannel)
        }
        binding.sendAlsoToChannel.isVisible = shouldShowCheckbox
    }

    private fun configAttachmentButton() {
        binding.attachmentsButton.isVisible = messageInputViewStyle.attachButtonEnabled
        binding.attachmentsButton.setImageDrawable(messageInputViewStyle.attachButtonIcon)
        binding.attachmentsButton.setBorderlessRipple(messageInputViewStyle.attachmentButtonRippleColor)

        setAttachmentButtonClickListener {
            context.getFragmentManager()?.let {
                AttachmentSelectionDialogFragment.newInstance(messageInputViewStyle)
                    .apply { setAttachmentSelectionListener(attachmentSelectionListener) }
                    .show(it, AttachmentSelectionDialogFragment.TAG)
            }
        }
    }

    /**
     * Creates an instance of [SuggestionListControllerListener].
     *
     * Used to disable integration buttons, based on if commands or attachments are added to the message.
     */
    private fun createSuggestionsListControllerListener(): DefaultSuggestionListControllerListener =
        DefaultSuggestionListControllerListener { shouldEnableAttachments ->
            binding.attachmentsButton.isEnabled = shouldEnableAttachments
        }

    /**
     * Sets a click listener for the attachment button. If you want to implement a custom attachment flow do not forget
     * to set selected attachments via the [submitAttachments] method.
     *
     * @param listener Listener that is invoked when the user clicks on the attachment button in [MessageInputView].
     */
    public fun setAttachmentButtonClickListener(listener: AttachmentButtonClickListener) {
        binding.attachmentsButton.setOnClickListener { listener.onAttachmentButtonClicked() }
    }

    /**
     * Sets a click listener for the commands button.
     *
     * @param listener Listener that is invoked when the user clicks on the commands button in [MessageInputView].
     */
    public fun setCommandsButtonClickListener(listener: CommandsButtonClickListener) {
        binding.commandsButton.setOnClickListener { listener.onCommandsButtonClicked() }
    }

    /**
     * Sets a listener for message input view mode changes.
     *
     * @param listener The listener to be set.
     * @see [InputMode]
     */
    public fun setMessageInputModeListener(listener: MessageInputViewModeListener) {
        messageInputViewModeListener = listener
    }

    /**
     * Sets a listener for the mention selection.
     *
     * @param listener The listener to be set.
     */
    public fun setMessageInputMentionListener(listener: MessageInputMentionListener) {
        this.messageInputMentionListener = listener
    }

    /**
     * Sets a send message button enabled drawable.
     * Keep in mind that [MessageInputView] displays two different send message buttons:
     * - sendMessageButtonEnabled - when the user is able to send a message.
     * - sendMessageButtonDisabled - when the user is not able to send a message (send button is disabled).
     *
     * Drawable will override the one provided either by attributes or TransformStyle.messageInputStyleTransformer.
     * @param drawable The drawable to be set.
     */
    public fun setSendMessageButtonEnabledDrawable(drawable: Drawable) {
        binding.sendMessageButtonEnabled.setImageDrawable(drawable)
    }

    /**
     * Sets a send message button disabled drawable.

     * @param drawable The drawable to be set.
     * @see [setSendMessageButtonEnabledDrawable]
     */
    public fun setSendMessageButtonDisabledDrawable(drawable: Drawable) {
        binding.sendMessageButtonDisabled.setImageDrawable(drawable)
    }

    private fun configCommandsButton() {
        binding.commandsButton.setImageDrawable(messageInputViewStyle.commandsButtonIcon)
        binding.commandsButton.setBorderlessRipple(messageInputViewStyle.commandButtonRippleColor)

        binding.commandsButton.run {
            setOnClickListener {
                suggestionListController?.let {
                    if (isSelected || it.isSuggestionListVisible()) {
                        it.hideSuggestionList()
                    } else {
                        isSelected = true
                        it.showAvailableCommands()
                    }
                }
            }
        }
    }

    private fun setSendMessageButtonEnabled(hasValidContent: Boolean) {
        val isSendButtonEnabled = hasValidContent && this.isSendButtonEnabled
        if (binding.sendMessageButtonEnabled.isEnabled == isSendButtonEnabled) return

        currentAnimatorSet?.cancel()

        val (fadeInView, fadeOutView) = if (isSendButtonEnabled) {
            binding.sendMessageButtonEnabled to binding.sendMessageButtonDisabled
        } else {
            binding.sendMessageButtonDisabled to binding.sendMessageButtonEnabled
        }
        currentAnimatorSet = AnimatorSet().apply {
            duration = 300
            playTogether(
                ObjectAnimator.ofFloat(fadeOutView, "alpha", fadeOutView.alpha, 0F),
                ObjectAnimator.ofFloat(fadeInView, "alpha", fadeInView.alpha, 1F)
            )
            start()
        }

        binding.sendMessageButtonEnabled.isEnabled = isSendButtonEnabled
    }

    /**
     * Switches the input to command mode using the provided command.
     *
     * You can use this method to provide a shortcut to a certain command instead of
     * activating it by selecting it from the list of suggestions.
     *
     * @param command The command used to switch the mode.
     * Different commands transform the message input in different ways.
     */
    public fun switchToCommandMode(command: Command) {
        binding.messageInputFieldView.mode = MessageInputFieldView.Mode.CommandMode(command)
    }

    private fun configTextInput() {
        binding.messageInputFieldView.setContentChangeListener(
            object : MessageInputFieldView.ContentChangeListener {
                override fun onMessageTextChanged(messageText: String) {
                    maxMessageLengthHandler.onMessageLengthChanged(
                        messageText = messageText,
                        messageLength = messageText.length,
                        maxMessageLength = maxMessageLength,
                        maxMessageLengthExceeded = isMessageTooLong()
                    )

                    /**
                     * Check for links only if the user is not allowed
                     * to send them, otherwise it is unnecessary overhead.
                     */
                    if (!canSendLinks) {
                        inputContainsLinks = messageText.containsLinks()
                    }

                    refreshControlsState()
                    handleKeyStroke()

                    /** Debouncing when clearing the input will cause the suggestion list
                     popup to appear briefly after clearing the input in certain cases. */
                    if (messageText.isEmpty()) {
                        messageInputDebouncer?.cancelLastDebounce()
                        suggestionListController?.onNewMessageText(messageText)
                    } else {
                        messageInputDebouncer?.submitSuspendable {
                            suggestionListController?.onNewMessageText(messageText)
                        }
                    }
                }

                override fun onSelectedAttachmentsChanged(selectedAttachments: List<AttachmentMetaData>) {
                    refreshControlsState()
                }

                override fun onModeChanged(mode: MessageInputFieldView.Mode) {
                    refreshControlsState()
                }

                override fun onSelectedCustomAttachmentsChanged(selectedCustomAttachments: List<Attachment>) {
                    refreshControlsState()
                }
            }
        )

        binding.messageInputFieldView.binding.messageEditText.onFocusChangeListener =
            OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                if (hasFocus) {
                    Utils.showSoftKeyboard(this)
                } else {
                    Utils.hideSoftKeyboard(this)
                    hideSuggestionList()
                }

                if (keyboardListener == null) {
                    registerKeyboardListener()
                }
            }

        binding.messageInputFieldView.run {
            setTextColor(messageInputViewStyle.messageInputTextStyle.color)
            setHintTextColor(messageInputViewStyle.messageInputTextStyle.hintColor)
            setTextSizePx(messageInputViewStyle.messageInputTextStyle.size.toFloat())
            setInputFieldScrollBarEnabled(messageInputViewStyle.messageInputScrollbarEnabled)
            setInputFieldScrollbarFadingEnabled(messageInputViewStyle.messageInputScrollbarFadingEnabled)
            setCustomBackgroundDrawable(messageInputViewStyle.editTextBackgroundDrawable)
            setInputType(messageInputViewStyle.messageInputInputType)

            messageInputViewStyle.messageInputTextStyle.apply(binding.messageEditText)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                messageInputViewStyle.customCursorDrawable?.let(::setCustomCursor)
            }

            setCommandInputCancelIcon(messageInputViewStyle.commandInputCancelIcon)
            setCommandInputBadgeIcon(messageInputViewStyle.commandInputBadgeIcon)
            setCommandInputBadgeBackgroundDrawable(messageInputViewStyle.commandInputBadgeBackgroundDrawable)
            setCommandInputBadgeTextStyle(messageInputViewStyle.commandInputBadgeTextStyle)
        }

        binding.separator.background = messageInputViewStyle.dividerBackground
        binding.dismissInputMode.setImageDrawable(messageInputViewStyle.dismissIconDrawable)
        binding.cooldownBadgeTextView.setTextStyle(messageInputViewStyle.cooldownTimerTextStyle)
        binding.cooldownBadgeTextView.background = messageInputViewStyle.commandInputBadgeBackgroundDrawable
    }

    /**
     * Registers keyboard visibility change listener. The listener is responsible for hiding the suggestion
     * list popup and clearing the current focus when keyboard is hidden. The listener will not be registered
     * if adjustment option for [WindowManager.LayoutParams.softInputMode] doesn't imply resizing a window,
     * for example [WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING].
     */
    private fun registerKeyboardListener() {
        try {
            keyboardListener = KeyboardVisibilityEvent.registerEventListener(activity) { isOpen: Boolean ->
                if (!isOpen) {
                    binding.messageInputFieldView.clearMessageInputFocus()
                    hideSuggestionList()
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to register keyboard listener" }
        }
    }

    private fun handleKeyStroke() {
        if (canSendTypingUpdates) {
            typingUpdatesBuffer?.onKeystroke(binding.messageInputFieldView.messageText)
        }
    }

    private fun configSendButton() {
        isSendButtonEnabled = messageInputViewStyle.sendButtonEnabled

        binding.sendMessageButtonDisabled.run {
            messageInputViewStyle.sendButtonDisabledIcon.let(this::setImageDrawable)
            alpha = 1F
            isEnabled = false
        }

        binding.sendMessageButtonEnabled.run {
            messageInputViewStyle.sendButtonEnabledIcon.let(this::setImageDrawable)
            alpha = 0F
            isEnabled = false
        }
    }

    private fun refreshControlsState() {
        with(binding) {
            val isCommandMode = messageInputFieldView.mode is MessageInputFieldView.Mode.CommandMode
            val isEditMode = messageInputFieldView.mode is MessageInputFieldView.Mode.EditMessageMode
            val hasContent = messageInputFieldView.hasValidContent()
            val hasValidContent = hasContent && !isMessageTooLong()

            attachmentsButton.isVisible =
                messageInputViewStyle.attachButtonEnabled && !isCommandMode && !isEditMode && canSendAttachments
            commandsButton.isVisible = messageInputViewStyle.commandsButtonEnabled &&
                shouldShowCommandsButton() && !isCommandMode && canUseCommands && hasCommands
            commandsButton.isEnabled = !hasContent && !isEditMode
            setSendMessageButtonEnabled(hasValidContent)
        }
    }

    /**
     * Setter method for own capabilities which dictate which
     * parts of the UI are enabled or disabled for the current user
     * in the given channel.
     *
     * @param ownCapabilities A set of capabilities given to the user
     * in the current channel.
     */
    public fun setOwnCapabilities(ownCapabilities: Set<String>) {
        this.ownCapabilities = ownCapabilities

        val canSendMessage = ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
        val canSendAttachment = ownCapabilities.contains(ChannelCapabilities.UPLOAD_FILE)

        // precedence and boolean logic matter here
        // otherwise you can undo a previous capability
        setCanSendMessages(canSendMessage)
        setCanSendAttachments(canSendAttachment && canSendMessage)
        this.canSendLinks = ownCapabilities.contains(ChannelCapabilities.SEND_LINKS)
        this.canSendTypingUpdates = ownCapabilities.contains(ChannelCapabilities.SEND_TYPING_EVENTS)
    }

    /**
     * Disables or enables entering and sending a message
     * into the [MessageInputView] depending on if the given user
     * can send messages in the given channel.
     *
     * @param canSend If the user is given the ability to send messages.
     */
    private fun setCanSendMessages(canSend: Boolean) {
        binding.commandsButton.isVisible = messageInputViewStyle.commandsEnabled && canSend && hasCommands
        binding.attachmentsButton.isVisible = messageInputViewStyle.attachButtonEnabled && canSend

        canSendAttachments = canSend
        canUseCommands = canSend

        if (canSend) {
            enableSendButton()
            binding.messageInputFieldView
                .binding.messageEditText.hint = messageInputViewStyle.messageInputTextStyle.hint
        } else {
            disableSendButton()
            binding.messageInputFieldView.binding.messageEditText.setHint(
                R.string.stream_ui_message_cannot_send_messages_hint
            )
        }
        binding.messageInputFieldView.binding.messageEditText.isEnabled = canSend
        binding.messageInputFieldView.binding.messageEditText.isFocusable = canSend
        binding.messageInputFieldView.binding.messageEditText.isFocusableInTouchMode = canSend
    }

    /**
     * Disables or enables the integration for sending attachments
     * depending on if the given user can send attachments
     * in the given channel.
     *
     * @param canSend If the user is given the ability to send attachments.
     */
    private fun setCanSendAttachments(canSend: Boolean) {
        binding.attachmentsButton.isVisible = messageInputViewStyle.attachButtonEnabled && canSend
        canSendAttachments = canSend
    }

    private fun shouldShowCommandsButton(): Boolean {
        val isEditMode = binding.messageInputFieldView.mode is MessageInputFieldView.Mode.EditMessageMode

        return hasCommands && messageInputViewStyle.commandsButtonEnabled && commandsEnabled && !isEditMode
    }

    private fun sendMessage(messageReplyTo: Message? = null) {
        val messageText = getTrimmedMessageText()

        doSend(
            attachmentSender = { attachments ->
                sendMessageHandler.sendMessageWithAttachments(
                    messageText,
                    attachments,
                    messageReplyTo
                )
            },
            simpleSender = {
                sendMessageHandler.sendMessage(
                    messageText,
                    messageReplyTo
                )
            },
            customAttachmentsSender = { customAttachments ->
                sendMessageHandler.sendMessageWithCustomAttachments(
                    messageText,
                    customAttachments,
                    messageReplyTo
                )
            }
        )
    }

    private fun sendThreadMessage(parentMessage: Message) {
        val sendAlsoToChannel = binding.sendAlsoToChannel.isChecked
        val messageText = getTrimmedMessageText()
        doSend(
            attachmentSender = { attachments ->
                sendMessageHandler.sendToThreadWithAttachments(
                    parentMessage,
                    messageText,
                    sendAlsoToChannel,
                    attachments
                )
            },
            simpleSender = {
                sendMessageHandler.sendToThread(
                    parentMessage,
                    messageText,
                    sendAlsoToChannel
                )
            },
            customAttachmentsSender = { customAttachments ->
                sendMessageHandler.sendToThreadWithCustomAttachments(
                    parentMessage,
                    messageText,
                    sendAlsoToChannel,
                    customAttachments
                )
            }
        )
    }

    private fun doSend(
        attachmentSender: (List<Pair<File, String?>>) -> Unit,
        simpleSender: () -> Unit,
        customAttachmentsSender: (List<Attachment>) -> Unit,
    ) {
        val attachments = binding.messageInputFieldView.getAttachedFiles()
        val customAttachments = binding.messageInputFieldView.getCustomAttachments()

        when {
            attachments.isNotEmpty() -> {
                attachmentSender(attachments)
            }
            customAttachments.isNotEmpty() -> {
                customAttachmentsSender(customAttachments)
            }
            else -> {
                simpleSender()
            }
        }
    }

    private fun editMessage(oldMessage: Message) {
        sendMessageHandler.editMessage(oldMessage, getTrimmedMessageText())
        inputMode = InputMode.Normal
    }

    /**
     * Set a collection of attachments in [MessageInputView].
     *
     * @param attachments Collection of [AttachmentMetaData] that you are going to send with a message.
     * @param attachmentSource Value from enum [AttachmentSource] that represents source of attachments.
     */
    public fun submitAttachments(attachments: Collection<AttachmentMetaData>, attachmentSource: AttachmentSource) {
        attachmentSelectionListener.onAttachmentsSelected(attachments.toSet(), attachmentSource)
    }

    /**
     * Set a collection of custom attachments in [MessageInputView].
     *
     * @param attachments Collection of [Attachment] that you are going to send with a message.
     */
    @ExperimentalStreamChatApi
    public fun submitCustomAttachments(
        attachments: Collection<Attachment>,
        viewHolderFactory: SelectedCustomAttachmentViewHolderFactory,
    ) {
        customAttachmentsSelectionListener(attachments, viewHolderFactory)
    }

    /**
     * Returns trimmed text from the message input.
     *
     * @return Trimmed text from the message input.
     */
    private fun getTrimmedMessageText(): String {
        return binding.messageInputFieldView.messageText.trim()
    }

    private companion object {
        private const val CLICK_DELAY = 100L
        private const val TYPING_DEBOUNCE_MS = 300L
        val EMPTY_MESSAGE_SEND_HANDLER = object : MessageSendHandler {
            override fun sendMessage(messageText: String, messageReplyTo: Message?) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun sendMessageWithAttachments(
                message: String,
                attachmentsWithMimeTypes: List<Pair<File, String?>>,
                messageReplyTo: Message?,
            ) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun sendMessageWithCustomAttachments(
                message: String,
                attachments: List<Attachment>,
                messageReplyTo: Message?,
            ) {
                throw IllegalStateException("MessageInputView#sendMessageWithCustomAttachments needs to be configured to send messages")
            }

            override fun sendToThread(
                parentMessage: Message,
                messageText: String,
                alsoSendToChannel: Boolean,
            ) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun sendToThreadWithAttachments(
                parentMessage: Message,
                message: String,
                alsoSendToChannel: Boolean,
                attachmentsWithMimeTypes: List<Pair<File, String?>>,
            ) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun sendToThreadWithCustomAttachments(
                parentMessage: Message,
                message: String,
                alsoSendToChannel: Boolean,
                attachmentsWithMimeTypes: List<Attachment>,
            ) {
                throw IllegalStateException("MessageInputView#sendToThreadWithCustomAttachments needs to be configured to send messages")
            }

            override fun editMessage(oldMessage: Message, newMessageText: String) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun dismissReply() {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }
        }
    }

    /**
     * Class representing [MessageInputView] mode
     */
    public sealed class InputMode {
        /**
         * A mode when the user can send a message
         */
        public object Normal : InputMode() {
            override fun toString(): String = "Normal"
        }

        /**
         * A mode when the user can reply to a thread
         */
        public data class Thread(val parentMessage: Message) : InputMode()

        /**
         * A mode when the user can edit the message
         */
        public data class Edit(val oldMessage: Message) : InputMode()

        /**
         * A mode when the user can reply to the message
         */
        public data class Reply(val repliedMessage: Message) : InputMode()
    }

    public enum class ChatMode {
        DIRECT_CHAT,
        GROUP_CHAT,
    }

    public interface MessageSendHandler {
        public fun sendMessage(
            messageText: String,
            messageReplyTo: Message? = null,
        )

        public fun sendMessageWithAttachments(
            message: String,
            attachmentsWithMimeTypes: List<Pair<File, String?>>,
            messageReplyTo: Message? = null,
        )

        public fun sendMessageWithCustomAttachments(
            message: String,
            attachments: List<Attachment>,
            messageReplyTo: Message? = null,
        )

        public fun sendToThread(
            parentMessage: Message,
            messageText: String,
            alsoSendToChannel: Boolean,
        )

        public fun sendToThreadWithAttachments(
            parentMessage: Message,
            message: String,
            alsoSendToChannel: Boolean,
            attachmentsWithMimeTypes: List<Pair<File, String?>>,
        )

        public fun sendToThreadWithCustomAttachments(
            parentMessage: Message,
            message: String,
            alsoSendToChannel: Boolean,
            attachmentsWithMimeTypes: List<Attachment>,
        )

        public fun editMessage(oldMessage: Message, newMessageText: String)

        public fun dismissReply()
    }

    public fun interface OnMessageSendButtonClickListener {
        public fun onClick()
    }

    /**
     * A handler which can be used to display a custom error message when the maximum
     * message length has been exceeded.
     */
    public fun interface MaxMessageLengthHandler {

        /**
         * Called when message text length has changed.
         *
         * @param messageText The updated message text.
         * @param messageLength The updated message length.
         * @param maxMessageLength The maximum allowed message length.
         * @param maxMessageLengthExceeded True if the length of the text is greater than the maximum length.
         */
        public fun onMessageLengthChanged(
            messageText: String,
            messageLength: Int,
            maxMessageLength: Int,
            maxMessageLengthExceeded: Boolean,
        )
    }

    @FunctionalInterface
    public interface BigFileSelectionListener {
        public fun handleBigFileSelected(hasBigFile: Boolean)
    }

    /**
     * Listener invoked when selected attachments count changes. Can be used to perform actions such as showing an alert when max attachments count is exceeded.
     * By default, shows a [Snackbar] with error message when attachments count is exceeded
     */
    public fun interface SelectedAttachmentsCountListener {
        /**
         * Called when attachments count changes
         *
         * @param attachmentsCount Current attachments count
         * @param maxAttachmentsCount Maximum attachments count
         */
        public fun attachmentsCountChanged(attachmentsCount: Int, maxAttachmentsCount: Int)
    }

    /**
     * Users lookup functional interface. Used to create custom users lookup algorithm.
     */
    public interface UserLookupHandler {
        /**
         * Performs users lookup by given [query] in suspend way. It's executed on background, so it can perform heavy operations.
         *
         * @param query String as user input for lookup algorithm.
         * @return List of users as result of lookup.
         */
        public suspend fun handleUserLookup(query: String): List<User>
    }

    /**
     * Default implementation for MessageInputView.UserLookupHandler. This class ignores diacritics and upper case.
     * Tt uses levenshtein approximation so typos are included in the search. It is possible to choose a transliteration
     * in the class to conversions between languages are possible. It uses https://unicode-org.github.io/icu/userguide/icu4j/
     * for transliteration
     *
     * @param users The primary list of users used when searching for user metion matches. Usually this is populated
     * by local state data.
     * @param streamTransliterator Handles transliteration.
     * @param queryMembersOnline This method is invoked internally within the body of [handleUserLookup]] if no
     * matches were found within [users]. Use it to query the server for members and return a result.
     */
    public class DefaultUserLookupHandler @JvmOverloads constructor(
        public var users: List<User>,
        private var streamTransliterator: StreamTransliterator = DefaultStreamTransliterator(),
        private val queryMembersOnline: suspend (query: String) -> List<User> = { emptyList() },
    ) : UserLookupHandler {

        override suspend fun handleUserLookup(query: String): List<User> {
            return searchUsers(users, query, streamTransliterator).ifEmpty {
                queryMembersOnline(query)
            }
        }
    }

    /**
     * Functional interface for a listener set on the attachment button.
     */
    public fun interface AttachmentButtonClickListener {
        /**
         * Function to be invoked when a click on the attachment button happens.
         */
        public fun onAttachmentButtonClicked()
    }

    /**
     * Functional interface for a listener set on the commands button.
     */
    public fun interface CommandsButtonClickListener {
        /**
         * Function to be invoked when a click on the commands button happens.
         */
        public fun onCommandsButtonClicked()
    }

    /**
     * Listener invoked when input mode changes.
     * Can be used for changing view's appearance based on the current mode - for example, send message buttons' drawables.
     */
    public fun interface MessageInputViewModeListener {
        /**
         * Called when input mode changes.
         *
         * @param inputMode Current input mode.
         */
        public fun inputModeChanged(inputMode: InputMode)
    }

    /**
     * Listener invoked whenever a user selects a mention from the suggestion popup list.
     *
     * Used to store the mention before sending the message.
     */
    public fun interface MessageInputMentionListener {

        /**
         * Called when the user is selected in the mention suggestion list.
         */
        public fun onMentionSelected(user: User)
    }
}
