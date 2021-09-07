package io.getstream.chat.android.ui.message.input

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
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
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.utils.extensions.activity
import com.getstream.sdk.chat.utils.extensions.focusAndShowKeyboard
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.Debouncer
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.getFragmentManager
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiMessageInputBinding
import io.getstream.chat.android.ui.message.input.MessageInputView.MaxMessageLengthHandler
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogFragment
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionListener
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSource
import io.getstream.chat.android.ui.message.input.internal.MessageInputFieldView
import io.getstream.chat.android.ui.suggestion.list.SuggestionListController
import io.getstream.chat.android.ui.suggestion.list.SuggestionListView
import io.getstream.chat.android.ui.suggestion.list.SuggestionListViewStyle
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItemViewHolderFactory
import io.getstream.chat.android.ui.suggestion.list.internal.SuggestionListPopupWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import java.io.File
import kotlin.math.roundToInt
import kotlin.properties.Delegates

public class MessageInputView : ConstraintLayout {
    private val logger = ChatLogger.get("MessageInputView")

    public var inputMode: InputMode by Delegates.observable(InputMode.Normal) { _, previousValue, newValue ->
        configSendAlsoToChannelCheckbox()
        configInputMode(previousValue, newValue)
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
    private var typingListener: TypingListener? = null
    private var isKeyboardListenerRegistered: Boolean = false

    private var maxMessageLength: Int = Integer.MAX_VALUE

    private var cooldownInterval: Int = 0
    private var cooldownTimer: CountDownTimer? = null
    private var previousInputHint: String? = null

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

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun configInputMode(previousValue: InputMode, newValue: InputMode) {
        when (newValue) {
            is InputMode.Reply -> {
                binding.inputModeHeader.isVisible = true
                binding.headerLabel.text = context.getString(R.string.stream_ui_message_input_reply)
                binding.inputModeIcon.setImageResource(R.drawable.stream_ui_ic_arrow_curve_left)
                binding.messageInputFieldView.onReply(newValue.repliedMessage)
                binding.messageInputFieldView.binding.messageEditText.focusAndShowKeyboard()
            }

            is InputMode.Edit -> {
                binding.inputModeHeader.isVisible = true
                binding.headerLabel.text = context.getString(R.string.stream_ui_message_list_edit_message)
                binding.inputModeIcon.setImageResource(R.drawable.stream_ui_ic_edit)
                binding.messageInputFieldView.onEdit(newValue.oldMessage)
                binding.messageInputFieldView.binding.messageEditText.focusAndShowKeyboard()
            }

            else -> {
                binding.inputModeHeader.isVisible = false
                if (previousValue is InputMode.Reply) {
                    binding.messageInputFieldView.onReplyDismissed()
                }
            }
        }
    }

    public fun setOnSendButtonClickListener(listener: OnMessageSendButtonClickListener?) {
        this.onSendButtonClickListener = listener
    }

    public fun setTypingListener(listener: TypingListener?) {
        this.typingListener = listener
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
                }

                override fun onCommandClick(command: Command) {
                    binding.messageInputFieldView.autoCompleteCommand(command)
                }
            }
        )

        val dismissListener = PopupWindow.OnDismissListener {
            binding.commandsButton.postDelayed(CLICK_DELAY) { binding.commandsButton.isSelected = false }
        }
        val suggestionListUi = if (popupWindow) {
            SuggestionListPopupWindow(suggestionListView, this, dismissListener)
        } else {
            suggestionListView
        }
        suggestionListController = SuggestionListController(suggestionListUi).also {
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
     * @see [setMaxMessageLengthHandler] for more information on how to provide a custom error message
     */
    public fun setMaxMessageLength(maxMessageLength: Int) {
        this.maxMessageLength = maxMessageLength
    }

    /**
     * Sets the cooldown interval. When slow mode is enabled, users can only send messages every
     * [cooldownInterval] time interval.
     *
     * @param cooldownInterval the cooldown interval in seconds
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
        scope = CoroutineScope(DispatcherProvider.Main)
        scope?.launch {
            binding.messageInputFieldView.hasBigAttachment.collect(::consumeHasBigFile)
        }
    }

    override fun onDetachedFromWindow() {
        messageInputDebouncer?.shutdown()
        messageInputDebouncer = null
        cooldownTimer?.cancel()
        cooldownTimer = null
        hideSuggestionList()
        scope?.cancel()
        scope = null
        super.onDetachedFromWindow()
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(attr: AttributeSet? = null) {
        binding = StreamUiMessageInputBinding.inflate(streamThemeInflater, this)
        messageInputViewStyle = MessageInputViewStyle(context, attr)
        suggestionListViewStyle = SuggestionListViewStyle(context, attr)

        setBackgroundColor(messageInputViewStyle.backgroundColor)
        configAttachmentButton()
        configLightningButton()
        configTextInput()
        configSendButton()
        configSendAlsoToChannelCheckbox()
        configSendButtonListener()
        binding.dismissInputMode.setOnClickListener { dismissInputMode(inputMode) }
        setMentionsEnabled(messageInputViewStyle.mentionsEnabled)
        setCommandsEnabled(messageInputViewStyle.commandsEnabled)
        setSuggestionListViewInternal(SuggestionListView(context))
        binding.messageInputFieldView.setAttachmentMaxFileMb(messageInputViewStyle.attachmentMaxFileSize)
        val horizontalPadding = resources.getDimensionPixelSize(R.dimen.stream_ui_spacing_tiny)
        updatePadding(left = horizontalPadding, right = horizontalPadding)

        refreshControlsState()
    }

    public fun listenForBigAttachments(listener: BigFileSelectionListener) {
        bigFileSelectionListener = listener
    }

    private fun dismissInputMode(inputMode: InputMode) {
        if (inputMode is InputMode.Reply) {
            sendMessageHandler.dismissReply()
        }

        this.inputMode = InputMode.Normal
    }

    private fun configSendButtonListener() {
        binding.sendMessageButtonEnabled.setOnClickListener {
            if (binding.messageInputFieldView.hasBigAttachment.value) {
                consumeHasBigFile(true)
            } else {
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

    private fun consumeHasBigFile(hasBigFile: Boolean) {
        bigFileSelectionListener?.handleBigFileSelected(hasBigFile) ?: if (hasBigFile) {
            alertBigFileSendAttempt()
        }
    }

    private fun alertBigFileSendAttempt() {
        Snackbar.make(this,
            resources.getString(R.string.stream_ui_message_input_error_file_large_size,
                messageInputViewStyle.attachmentMaxFileSize),
            Snackbar.LENGTH_LONG)
            .apply { anchorView = binding.sendButtonContainer }
            .show()
    }

    /**
     * Shows cooldown countdown timer instead of send button when slow mode is enabled.
     */
    private fun startCooldownTimerIfNecessary() {
        if (cooldownInterval > 0) {
            // store the current message input hint
            previousInputHint = binding.messageInputFieldView.messageHint

            with(binding) {
                cooldownBadgeTextView.isVisible = true
                messageInputFieldView.messageHint = context.getString(R.string.stream_ui_message_input_slow_mode_hint)

                cooldownTimer = object : CountDownTimer(cooldownInterval * 1000L, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        val secondsRemaining = (millisUntilFinished.toFloat() / 1000).roundToInt()
                        cooldownBadgeTextView.text = "$secondsRemaining"
                    }

                    override fun onFinish() {
                        cooldownBadgeTextView.isVisible = false
                        // restore the last input hint
                        messageInputFieldView.messageHint = previousInputHint ?: ""
                    }
                }.start()
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
        binding.attachmentsButton.run {
            messageInputViewStyle.attachButtonIcon.let(this::setImageDrawable)
            setOnClickListener {
                context.getFragmentManager()?.let {
                    AttachmentSelectionDialogFragment.newInstance(messageInputViewStyle)
                        .apply { setAttachmentSelectionListener(attachmentSelectionListener) }
                        .show(it, AttachmentSelectionDialogFragment.TAG)
                }
            }
        }
    }

    private fun configLightningButton() {
        binding.commandsButton.run {
            messageInputViewStyle.commandsButtonIcon.let(this::setImageDrawable)
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

                    refreshControlsState()
                    handleKeyStroke()
                    messageInputDebouncer?.submitSuspendable { suggestionListController?.onNewMessageText(messageText) }
                }

                override fun onSelectedAttachmentsChanged(selectedAttachments: List<AttachmentMetaData>) {
                    refreshControlsState()
                }

                override fun onModeChanged(mode: MessageInputFieldView.Mode) {
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

                if (!isKeyboardListenerRegistered) {
                    isKeyboardListenerRegistered = true
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
            KeyboardVisibilityEvent.setEventListener(activity) { isOpen: Boolean ->
                if (!isOpen) {
                    binding.messageInputFieldView.clearMessageInputFocus()
                    hideSuggestionList()
                }
            }
        } catch (e: Exception) {
            logger.logE("Failed to register keyboard listener", e)
        }
    }

    private fun handleKeyStroke() {
        if (binding.messageInputFieldView.messageText.isNotEmpty()) {
            typingListener?.onKeystroke()
        } else {
            typingListener?.onStopTyping()
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
            val commandMode = messageInputFieldView.mode is MessageInputFieldView.Mode.CommandMode
            val hasContent = messageInputFieldView.hasContent()
            val hasValidContent = hasContent && !isMessageTooLong()

            attachmentsButton.isVisible = messageInputViewStyle.attachButtonEnabled && !commandMode
            commandsButton.isVisible = shouldShowCommandsButton() && !commandMode
            commandsButton.isEnabled = !hasContent
            setSendMessageButtonEnabled(hasValidContent)
        }
    }

    private fun shouldShowCommandsButton(): Boolean {
        val hasCommands = suggestionListController?.commands?.isNotEmpty() ?: false
        return hasCommands && messageInputViewStyle.commandsButtonEnabled && commandsEnabled
    }

    private fun sendMessage(messageReplyTo: Message? = null) {
        doSend(
            { attachments ->
                sendMessageHandler.sendMessageWithAttachments(
                    binding.messageInputFieldView.messageText,
                    attachments,
                    messageReplyTo
                )
            },
            { sendMessageHandler.sendMessage(binding.messageInputFieldView.messageText, messageReplyTo) }
        )
    }

    private fun sendThreadMessage(parentMessage: Message) {
        val sendAlsoToChannel = binding.sendAlsoToChannel.isChecked
        doSend({ attachments ->
            sendMessageHandler.sendToThreadWithAttachments(
                parentMessage,
                binding.messageInputFieldView.messageText,
                sendAlsoToChannel,
                attachments
            )
        },
            {
                sendMessageHandler.sendToThread(parentMessage,
                    binding.messageInputFieldView.messageText,
                    sendAlsoToChannel)
            }
        )
    }

    private fun doSend(attachmentSender: (List<Pair<File, String?>>) -> Unit, simpleSender: () -> Unit) {
        val attachments = binding.messageInputFieldView.getAttachedFiles()

        if (attachments.isNotEmpty()) {
            attachmentSender(attachments)
        } else {
            simpleSender()
        }
    }

    private fun editMessage(oldMessage: Message) {
        sendMessageHandler.editMessage(oldMessage, binding.messageInputFieldView.messageText)
        inputMode = InputMode.Normal
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

            override fun editMessage(oldMessage: Message, newMessageText: String) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun dismissReply() {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }
        }
    }

    public sealed class InputMode {
        public object Normal : InputMode()
        public data class Thread(val parentMessage: Message) : InputMode()
        public data class Edit(val oldMessage: Message) : InputMode()
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
         * Called when message text length has changed
         *
         * @param messageText the updated message text
         * @param messageLength the updated message length
         * @param maxMessageLength the maximum allowed message length
         * @param maxMessageLengthExceeded true if the length of the text is greater than the maximum length.
         */
        public fun onMessageLengthChanged(
            messageText: String,
            messageLength: Int,
            maxMessageLength: Int,
            maxMessageLengthExceeded: Boolean,
        )
    }

    public interface TypingListener {
        public fun onKeystroke()
        public fun onStopTyping()
    }

    @FunctionalInterface
    public interface BigFileSelectionListener {
        public fun handleBigFileSelected(hasBigFile: Boolean)
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

    public class DefaultUserLookupHandler(public var users: List<User>) : UserLookupHandler {
        override suspend fun handleUserLookup(query: String): List<User> {
            return users.filter { it.name.contains(query, true) }
        }
    }
}
